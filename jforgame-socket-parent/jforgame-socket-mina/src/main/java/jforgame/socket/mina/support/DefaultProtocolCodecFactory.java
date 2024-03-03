package jforgame.socket.mina.support;

import jforgame.codec.MessageCodec;
import jforgame.socket.share.message.MessageFactory;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

/**
 * @author kinson
 */
public class DefaultProtocolCodecFactory implements ProtocolCodecFactory {

    private DefaultProtocolDecoder decoder;

    private DefaultProtocolEncoder encoder;

    public DefaultProtocolCodecFactory(MessageFactory messageFactory, MessageCodec messageCodec) {
        this.decoder = new DefaultProtocolDecoder(messageFactory, messageCodec);
        this.encoder = new DefaultProtocolEncoder(messageFactory, messageCodec);
    }

    @Override
    public ProtocolEncoder getEncoder(IoSession session) throws Exception {
        return encoder;
    }

    @Override
    public ProtocolDecoder getDecoder(IoSession session) throws Exception {
        return decoder;
    }

}