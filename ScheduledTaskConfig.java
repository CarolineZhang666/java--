package com.telecomyt.plat.cmac.report.config;

import com.telecomyt.plat.cmac.report.enums.ScheduledTaskEnum;
import com.telecomyt.plat.cmac.report.service.ScheduledTaskJob;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定时任务配置
 *
 * @author zhangyingqi
 * @date 2020-12-11
 */
@Configuration
public class ScheduledTaskConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(20);
        threadPoolTaskScheduler.setThreadNamePrefix("taskExecutor-");
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(60);
        return threadPoolTaskScheduler;
    }

    /**
     * 初始化定时任务Map
     * key :任务key
     * value : 执行接口实现
     */
    @Bean(name = "scheduledTaskJobMap")
    public Map<String, ScheduledTaskJob> scheduledTaskJobMap(ApplicationContextHolder applicationContextHolder) {
        Map<String, ScheduledTaskJob> scheduledTaskJobMap = new ConcurrentHashMap<>(16);
        for (ScheduledTaskEnum taskEnum : ScheduledTaskEnum.values()) {
            scheduledTaskJobMap.put(taskEnum.getTaskKey(), taskEnum.getScheduledTaskJob());
        }
        return scheduledTaskJobMap;
    }
}