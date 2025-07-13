package jforgame.demo.game.gm.message;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import jforgame.demo.game.Modules;
import jforgame.demo.game.gm.GmConstant;
import jforgame.socket.share.annotation.MessageMeta;
import jforgame.socket.share.message.Message;

/**
 * gm执行结果
 */
@MessageMeta(module=Modules.GM, cmd=GmConstant.RES_GM_RESULT)
public class ResGmResult implements Message {
	
	/** 执行失败 */
	public static final byte FAIL = 0;
	/** 执行成功 */
	public static final byte SUCC = 1;
	
	@Protobuf(order = 1)
	private int result;
	@Protobuf(order = 2)
	private String message;
	
	public ResGmResult() {
		super();
	}

	private ResGmResult(byte result, String message) {
		this.result  = result;
		this.message = message;
	}
	
	public static ResGmResult buildSuccResult(String msg) {
		return new ResGmResult(SUCC, msg);
	}
	
	public static ResGmResult buildFailResult(String msg) {
		return new ResGmResult(FAIL, msg);
	}

	public int getResult() {
		return result;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "ResGmResultMessage [result=" + result + ", message="
						+ message + "]";
	}
	
}
