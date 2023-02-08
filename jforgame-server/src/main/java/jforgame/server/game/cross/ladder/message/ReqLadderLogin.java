package jforgame.server.game.cross.ladder.message;

import jforgame.server.game.Modules;
import jforgame.server.game.cross.ladder.service.LadderDataPool;
import jforgame.socket.share.annotation.MessageMeta;
import jforgame.socket.share.message.Message;

@MessageMeta(module = Modules.CROSS_BUSINESS, cmd = LadderDataPool.RES_LADDER_LOGIN)
public class ReqLadderLogin implements Message {
	
	private String sign;

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

}
