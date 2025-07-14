package jforgame.demo.game.database.config.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 系统公告
 * 
 */
@Entity
public class ConfigNotice {

	@Column
	@Id
	private Integer id;
	@Column
	/** 所属模块 */
	private String module;
	@Column
	/** 输出频道 0为无频道1为世界2为公会3为队伍等 */
	private short channel;
	@Column
	private String content;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public short getChannel() {
		return channel;
	}

	public void setChannel(short channel) {
		this.channel = channel;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
