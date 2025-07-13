package jforgame.demo;

import jforgame.commons.FileUtils;
import jforgame.demo.utils.JsScriptEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * 防火墙配置
 *
 */
public class FireWallConfig {

	/** 每秒最大收包数量 */
	private int maxPackagePerSecond;

	/** XX秒为洪水窗口期 */
	private int floodWindowSeconds;

	/** 窗口期超过多少次即判定有效 */
	private int maxFloodTimes;

	private static volatile FireWallConfig self;

	public static FireWallConfig getInstance() {
		if (self != null) {
			return self;
		}
		synchronized (FireWallConfig.class) {
			if (self == null) {
				self = new FireWallConfig();
				self.init();
			}
		}
		return self;
	}

	private void init() {
		try {
			String content = FileUtils.readFullText("configs/firewall.cfg.js");

			self = new FireWallConfig();
			Map<String, Object> params = new HashMap<>();
			params.put("config", self);

			JsScriptEngine.runCode(content, params);
		} catch (Exception e) {
			throw new RuntimeException("读取防火墙配置出错");
		}
	}

	public int getMaxPackagePerSecond() {
		return maxPackagePerSecond;
	}

	public void setMaxPackagePerSecond(int maxPackagePerSecond) {
		this.maxPackagePerSecond = maxPackagePerSecond;
	}

	public int getFloodWindowSeconds() {
		return floodWindowSeconds;
	}

	public void setFloodWindowSeconds(int floodWindowSeconds) {
		this.floodWindowSeconds = floodWindowSeconds;
	}

	public int getMaxFloodTimes() {
		return maxFloodTimes;
	}

	public void setMaxFloodTimes(int maxFloodTimes) {
		this.maxFloodTimes = maxFloodTimes;
	}

	@Override
	public String toString() {
		return "FireWallConfig [maxPackagePerSecond=" + maxPackagePerSecond + ", floodWindowSeconds="
				+ floodWindowSeconds + ", maxFloodTimes=" + maxFloodTimes + "]";
	}
}
