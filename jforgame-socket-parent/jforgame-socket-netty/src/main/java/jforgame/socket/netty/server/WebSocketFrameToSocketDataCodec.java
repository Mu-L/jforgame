package jforgame.socket.netty.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCounted;
import jforgame.codec.MessageCodec;
import jforgame.commons.util.JsonUtil;
import jforgame.commons.util.NumberUtil;
import jforgame.socket.core.net.WebSocketJsonFrame;
import jforgame.socket.core.protocol.message.DefaultMessageHeader;
import jforgame.socket.core.protocol.message.MessageFactory;
import jforgame.socket.core.protocol.message.MessageHeader;
import jforgame.socket.core.protocol.message.RequestDataFrame;
import jforgame.socket.core.protocol.message.SocketDataFrame;
import jforgame.socket.netty.WebSocketFrameType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Codec between websocket message frame and framework data frame.
 * Converts {@link WebSocketFrame} to {@link SocketDataFrame} or other types of messages.
 * <p>Frame selection rules controlled by member field frameType:</p>
 * <ul>
 * <li>frameType = {@link WebSocketFrameType#FRAME_TYPE_AUTO}: Adaptive mode (server).
 * Each client connection independently determines its encoding format based on the first uplink
 * business frame. The detected type is cached in the Channel attribute and locked permanently.
 * </li>
 * <li>frameType = {@link WebSocketFrameType#FRAME_TYPE_TEXT}: Force all outbound messages to TextWebSocketFrame
 * (client fixed text mode).</li>
 * <li>frameType = {@link WebSocketFrameType#FRAME_TYPE_BINARY}: Force all outbound messages to BinaryWebSocketFrame
 * (client fixed binary mode).</li>
 * </ul>
 */
public class WebSocketFrameToSocketDataCodec extends MessageToMessageCodec<WebSocketFrame, Object> {


    Logger logger = LoggerFactory.getLogger(WebSocketFrameToSocketDataCodec.class);
    private final MessageCodec messageCodec;

    private final MessageFactory messageFactory;

    private static final int MIN_BUFF_THRESHOLD = 1024;

    /**
     * Global websocket outbound frame control flag (immutable after construction).
     * 0 = auto adapt (server), each client independently based on first uplink frame
     * 1 = force text frame (client)
     * 2 = force binary frame (client)
     */
    private final int frameType;

    // Encode buffer local cache
    private static final ThreadLocal<ByteBuf> smallBufferCache = ThreadLocal.withInitial(() -> ByteBufAllocator.DEFAULT.buffer(MIN_BUFF_THRESHOLD) // Use default pooled allocator, initial capacity 1KB
    );

    static final AttributeKey<Integer> CLIENT_WS_FRAME_TYPE = AttributeKey.valueOf("WS_CLIENT_FRAME_TYPE");

    public WebSocketFrameToSocketDataCodec(MessageCodec messageCodec, MessageFactory messageFactory) {
        this(WebSocketFrameType.FRAME_TYPE_TEXT, messageCodec, messageFactory);
    }

    public WebSocketFrameToSocketDataCodec(int frameType, MessageCodec messageCodec, MessageFactory messageFactory) {
        this.messageCodec = messageCodec;
        this.frameType = frameType;
        this.messageFactory = messageFactory;
    }

    /**
     * Resolve the actual outbound frame type for the given channel.
     * <ul>
     *   <li>If global frameType is AUTO (server mode): reads per-connection cached type from Channel attribute.
     *       Falls back to TEXT if not yet determined (e.g. first response before any decode).</li>
     *   <li>If global frameType is TEXT or BINARY (client mode): always returns the configured type.</li>
     * </ul>
     */
    private int resolveFrameType(ChannelHandlerContext ctx) {
        if (frameType == WebSocketFrameType.FRAME_TYPE_AUTO) {
            Attribute<Integer> attr = ctx.channel().attr(CLIENT_WS_FRAME_TYPE);
            Integer cached = attr.get();
            return cached != null ? cached : WebSocketFrameType.FRAME_TYPE_TEXT;
        }
        return frameType;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object o, List<Object> list) throws Exception {
        if (o instanceof SocketDataFrame) {
            SocketDataFrame socketDataFrame = (SocketDataFrame) o;
            Object message = socketDataFrame.getMessage();
            int actualFrameType = resolveFrameType(ctx);

            if (actualFrameType == WebSocketFrameType.FRAME_TYPE_TEXT) {
                String json = JsonUtil.object2String(message);
                WebSocketJsonFrame frame = new WebSocketJsonFrame();
                frame.index = socketDataFrame.getIndex();
                frame.cmd = this.messageFactory.getMessageId(message.getClass());
                frame.msg = json;
                list.add(new TextWebSocketFrame(JsonUtil.object2String(frame)));
            } else {
                // Binary format
                byte[] body = this.messageCodec.encode(message);
                MessageHeader header = new DefaultMessageHeader();
                header.setCmd(this.messageFactory.getMessageId(message.getClass()));
                header.setMsgLength(body.length + 12);
                header.setIndex(socketDataFrame.getIndex());
                int requiredSize = header.getMsgLength();
                ByteBuf buffer;
                boolean usedCacheBuff = false;
                if (requiredSize <= MIN_BUFF_THRESHOLD) { // Small messages use cache
                    buffer = smallBufferCache.get();
                    // Check reference count, recreate if already released
                    if (buffer.refCnt() == 0) {
                        buffer = ctx.alloc().buffer(MIN_BUFF_THRESHOLD);
                        smallBufferCache.set(buffer); // Update cache
                    }
                    usedCacheBuff = true;
                    buffer.clear(); // Clear for reuse
                } else {
                    // Large messages allocate on demand
                    buffer = ctx.alloc().buffer(requiredSize);
                }

                if (usedCacheBuff) {
                    // Manually increase reference count to prevent automatic release after converting to BinaryWebSocketFrame
                    buffer.retain();
                }

                buffer.writeBytes(header.write());
                buffer.writeBytes(body);
                list.add(new BinaryWebSocketFrame(buffer));
            }

        } else if (o instanceof ReferenceCounted) {
            ((ReferenceCounted) o).retain();
            list.add(o);
        } else {
            list.add(o);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        // 分片续帧由WebSocketFrameAggregator全权合并，业务拒绝处理，直接跳过
        if (frame instanceof ContinuationWebSocketFrame) {
            return;
        }
        if (frame instanceof CloseWebSocketFrame || frame instanceof PingWebSocketFrame || frame instanceof PongWebSocketFrame) {
            return;
        }

        // Server auto-detect mode: on first business frame, detect and lock the channel's frame type
        if (frameType == WebSocketFrameType.FRAME_TYPE_AUTO) {
            Attribute<Integer> frameTypeAttr = ctx.channel().attr(CLIENT_WS_FRAME_TYPE);
            Integer existType = frameTypeAttr.get();
            // Only initialize frame type when channel attribute is empty, lock permanently after first assignment
            if (existType == null) {
                if (frame instanceof TextWebSocketFrame) {
                    frameTypeAttr.set(WebSocketFrameType.FRAME_TYPE_TEXT);
                } else if (frame instanceof BinaryWebSocketFrame) {
                    frameTypeAttr.set(WebSocketFrameType.FRAME_TYPE_BINARY);
                }
            }
        }

        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
            String json = textWebSocketFrame.text();
            WebSocketJsonFrame textFrame = JsonUtil.string2Object(json, WebSocketJsonFrame.class);
            if (textFrame == null) {
                logger.error("json failed, data [{}]", json);
                return;
            }
            Class<?> clazz = messageFactory.getMessage(NumberUtil.intValue(textFrame.cmd));
            Object realMsg = JsonUtil.string2Object(textFrame.msg, clazz);
            MessageHeader header = new DefaultMessageHeader();
            header.setCmd(textFrame.cmd);
            header.setMsgLength(textWebSocketFrame.content().readableBytes());
            header.setIndex(textFrame.index);
            RequestDataFrame requestDataFrame = new RequestDataFrame(header, realMsg);
            out.add(requestDataFrame);
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            ByteBuf in = binaryFrame.content();
            byte[] headerData = new byte[DefaultMessageHeader.SIZE];
            in.readBytes(headerData);
            MessageHeader headerMeta = new DefaultMessageHeader();
            headerMeta.read(headerData);
            int length = headerMeta.getMsgLength();
            int bodySize = length - DefaultMessageHeader.SIZE;
            int cmd = headerMeta.getCmd();
            byte[] body = new byte[bodySize];
            in.readBytes(body);
            Class<?> clazz = messageFactory.getMessage(NumberUtil.intValue(cmd));
            if (clazz == null) {
                logger.info("未识别的协议号:{}", cmd);
                return;
            }
            Object message = messageCodec.decode(clazz, body);
            RequestDataFrame requestDataFrame = new RequestDataFrame(headerMeta, message);
            out.add(requestDataFrame);
        }
    }
}