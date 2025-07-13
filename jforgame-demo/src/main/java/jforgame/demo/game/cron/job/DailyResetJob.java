package jforgame.demo.game.cron.job;

import jforgame.demo.game.GameContext;
import jforgame.demo.game.core.SystemParameters;
import jforgame.demo.game.database.user.PlayerEnt;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * 每日５点定时job
 */
@DisallowConcurrentExecution
public class DailyResetJob implements Job {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		logger.info("每日５点定时任务开始");

		long now = System.currentTimeMillis();

		SystemParameters.update("dailyResetTimestamp", now);
        Collection<PlayerEnt> onlines = GameContext.playerManager.getOnlinePlayers().values();
		for (PlayerEnt player:onlines) {
			//将事件封装成任务，丢回业务线程处理

		}
	}

}
