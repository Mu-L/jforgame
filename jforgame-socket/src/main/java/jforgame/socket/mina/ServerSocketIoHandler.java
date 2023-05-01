package jforgame.socket.mina;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jforgame.socket.IdSession;
import jforgame.socket.share.message.IMessageDispatcher;

/**
 * @author kinson
 */
public class ServerSocketIoHandler extends IoHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(ServerSocketIoHandler.class);

	/** 消息分发器 */
	private IMessageDispatcher messageDispatcher;

	private final AttributeKey USER_SESSION = new AttributeKey(MinaSessionProperties.class, "GameSession");


	public ServerSocketIoHandler(IMessageDispatcher messageDispatcher) {
		this.messageDispatcher = messageDispatcher;
	}

	@Override
	public void sessionCreated(IoSession session) {
		IdSession userSession = new MSession(session);
		session.setAttribute(USER_SESSION,
				userSession);
		messageDispatcher.onSessionCreated(userSession);
	}

	@Override
	public void messageReceived(IoSession session, Object data) throws Exception {
		Object message = data;
		IdSession userSession = (IdSession) session.getAttribute(USER_SESSION);
		//交由消息分发器处理
		messageDispatcher.dispatch(userSession, message);
	}
	
	@Override
	public void sessionClosed(IoSession session) throws Exception {
		IdSession userSession = (IdSession) session.getAttribute(USER_SESSION);
		messageDispatcher.onSessionClosed(userSession);
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		logger.error("server exception", cause);
	}
}