package jforgame.demo.game.database.config.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 玩家等级配置表
 * 
 */
@Entity()
public class ConfigPlayerLevel {

	/**
	 * 等级
	 */
	@Id
	@Column
	private Integer level;
	
	/**
	 * 升到下一级别需要的经验
	 */
	@Column
	private long needExp;
	
	/**
	 * 最大体力
	 */
	@Column
	private int vitality;

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public long getNeedExp() {
		return needExp;
	}

	public void setNeedExp(long needExp) {
		this.needExp = needExp;
	}

	public int getVitality() {
		return vitality;
	}

	public void setVitality(int vitality) {
		this.vitality = vitality;
	}
	
}
