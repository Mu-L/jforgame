package jforgame.socket.codec.struct;

import jforgame.socket.CodecProperties;
import jforgame.socket.share.message.MessageEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class StructMessageEncoder implements MessageEncoder {

    private static Logger logger = LoggerFactory.getLogger(StructMessageEncoder.class);

    private ThreadLocal<ByteBuffer> localBuff = ThreadLocal.withInitial(() -> ByteBuffer.allocate(CodecProperties.WRITE_CAPACITY));

    @Override
    public byte[] writeMessageBody(Object message) {
        ByteBuffer allocator = localBuff.get();
        allocator.clear();

        try {
            Codec messageCodec = Codec.getSerializer(message.getClass());
            messageCodec.encode(allocator, message, null);
        } catch (Exception e) {
            logger.error("read message {} failed , exception {}",
                    new Object[]{message.getClass(), e});
        }
        allocator.flip();
        byte[] body = new byte[allocator.remaining()];
        allocator.get(body);
        return body;
    }

}