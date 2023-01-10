package com.telecomyt.plat.cmac.report.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.telecomyt.plat.cmac.common.exception.BusinessException;
import com.telecomyt.plat.cmac.common.utils.BeanCopierUtil;
import com.telecomyt.plat.cmac.report.config.ReportInfoConfig;
import com.telecomyt.plat.cmac.report.dto.query.ReportRecordQuery;
import com.telecomyt.plat.cmac.report.dto.query.TaskConfigPageQuery;
import com.telecomyt.plat.cmac.report.dto.request.*;
import com.telecomyt.plat.cmac.report.dto.response.ReportTaskConfigResp;
import com.telecomyt.plat.cmac.report.entity.ReportTaskConfig;
import com.telecomyt.plat.cmac.report.enums.AnewReportEnum;
import com.telecomyt.plat.cmac.report.enums.LockEnum;
import com.telecomyt.plat.cmac.report.enums.ScheduledTaskEnum;
import com.telecomyt.plat.cmac.report.mapper.*;
import com.telecomyt.plat.cmac.report.service.ReportRecordService;
import com.telecomyt.plat.cmac.report.service.ReportTaskConfigService;
import com.telecomyt.plat.cmac.report.service.ScheduledTaskJob;
import com.telecomyt.plat.cmac.report.util.CronUtil;
import com.telecomyt.plat.cmac.report.util.DataCleanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;


/**
 * (ReportTaskConfig表)服务实现类
 *
 * @author zhangyingqi
 * @since 2020-12-11 11:46:35
 */
@Slf4j
@Service
public class ReportTaskConfigServiceImpl implements ReportTaskConfigService {

    /**
     * 可重入锁
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 存放已经启动的任务map
     */
    private final Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();
    @Resource
    private ReportTaskConfigMapper reportTaskConfigMapper;
    @Autowired
    private ReportInfoConfig reportInfoConfig;
    @Autowired
    private ReportRecordService reportRecordService;
    /**
     * 定时任务线程池
     */
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Resource
    private ReportRecordMapper reportRecordMapper;
    /**
     * 所有定时任务存放Map
     * key :任务 key
     * value:任务实现
     */
    @Autowired
    @Qualifier(value = "scheduledTaskJobMap")
    private Map<String, ScheduledTaskJob> scheduledTaskJobMap;

    /**
     * 所有任务列表
     */
    @Override
    public PageInfo<ReportTaskConfig> taskList(TaskConfigPageQuery taskConfigPageQuery) {
        //数据库查询所有任务
        ReportTaskConfig reportTaskConfig = BeanCopierUtil.copyBean(taskConfigPageQuery, ReportTaskConfig.class);
        PageHelper.startPage(taskConfigPageQuery.getPageNum(), taskConfigPageQuery.getPageSize());
        List<ReportTaskConfig> taskBeanList = reportTaskConfigMapper.getAllTask(reportTaskConfig);
        if (CollectionUtils.isEmpty(taskBeanList)) {
            return new PageInfo<>();
        }
        for (ReportTaskConfig taskBean : taskBeanList) {
            String taskKey = taskBean.getTaskKey();
            //是否启动标记处理
            Boolean start = this.isStart(taskKey);
            if (start != null && start && taskBean.getStartFlag() == 1) {
                taskBean.setStartFlag(1);
            }
            if (start != null && start && taskBean.getStartFlag() == 0) {
                taskBean.setStartFlag(1);
                reportTaskConfigMapper.updateByTaskKey(taskKey, 1);
            }
        }
        return new PageInfo<>(taskBeanList);
    }

    /**
     * 根据任务key 启动任务
     */
    @Override
    public Boolean start(TaskConfigStartParam taskConfigStartParam) {
        String taskKey = taskConfigStartParam.getTaskKey();
        String cron = null;
        if (taskConfigStartParam.getRate() != null && taskConfigStartParam.getCycle() != null) {
            cron = CronUtil.createLoopCronExpression(taskConfigStartParam.getRate(), taskConfigStartParam.getCycle());
        }
        //添加锁放一个线程启动，防止多人启动多次
        log.info(">>>>>> 添加任务启动锁完毕");
        lock.lock();
        try {
            //校验任务是否存在
            if (!scheduledTaskJobMap.containsKey(taskKey)) {
                log.info(">>>>>> 当前任务不存在！");
                return false;
            }
            //校验是否已经启动
            if (this.isStart(taskKey)) {
                log.info(">>>>>> 当前任务已经启动，无需重复启动！");
                return false;
            }
            //根据key数据库获取任务配置信息
            ReportTaskConfig scheduledTask = reportTaskConfigMapper.getReportTaskConfigByKey(taskKey);
            if (cron != null) {
                scheduledTask.setTaskCron(cron);
                scheduledTask.setCycle(taskConfigStartParam.getCycle());
                scheduledTask.setRate(taskConfigStartParam.getRate());
                reportTaskConfigMapper.update(scheduledTask);
            }
            //启动任务
            this.doStartTask(scheduledTask);
        } finally {
            // 释放锁
            lock.unlock();
            log.info(">>>>>> 释放任务启动锁完毕");
        }
        return true;
    }

    /**
     * 根据 key 停止任务
     */
    @Override
    public Boolean stop(String taskKey) {
        log.info(">>>>>> 进入停止任务 {}  >>>>>>", taskKey);
        //当前任务实例是否存在
        boolean taskStartFlag = scheduledFutureMap.containsKey(taskKey);
        log.info(">>>>>> 当前任务实例是否存在 {}", taskStartFlag);
        if (taskStartFlag) {
            //获取任务实例
            ScheduledFuture scheduledFuture = scheduledFutureMap.get(taskKey);
            //关闭实例
            scheduledFuture.cancel(true);
            reportTaskConfigMapper.updateByTaskKey(taskKey, 0);
        }
        log.info(">>>>>> 结束停止任务 {}  >>>>>>", taskKey);
        return taskStartFlag;
    }

    /**
     * 根据任务key 重启任务
     */
    @Override
    public Boolean restart(String taskKey) {
        log.info(">>>>>> 进入重启任务 {}  >>>>>>", taskKey);
        //先停止
        this.stop(taskKey);
        //再启动
        TaskConfigStartParam taskConfigStartParam = new TaskConfigStartParam();
        taskConfigStartParam.setTaskKey(taskKey);
        return this.start(taskConfigStartParam);
    }

    /**
     * 程序启动时初始化  ==> 启动所有正常状态的任务
     */
    @Override
    public void initAllTask(List<ReportTaskConfig> reportTaskConfigList) {
        log.info("程序启动 ==> 初始化所有任务开始 ！size={}", reportTaskConfigList.size());
        for (ReportTaskConfig scheduledTask : reportTaskConfigList) {
            //任务 key
            String taskKey = scheduledTask.getTaskKey();
            //校验是否已经启动
            if (this.isStart(taskKey)) {
                continue;
            }
            //启动任务
            this.doStartTask(scheduledTask);
        }
        log.info("程序启动 ==> 初始化所有任务结束 ！size={}", reportTaskConfigList.size());
    }

    /**
     * 执行启动任务
     */
    private void doStartTask(ReportTaskConfig scheduledTask) {
        //任务key
        String taskKey = scheduledTask.getTaskKey();
        //定时表达式
        String taskCron = scheduledTask.getTaskCron();
        //获取需要定时调度的接口
        ScheduledTaskJob scheduledTaskJob = scheduledTaskJobMap.get(taskKey);
        log.info(">>>>>> 任务 [ {} ] ,cron={}", scheduledTask.getTaskName(), taskCron);
        ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler.schedule(scheduledTaskJob,
                triggerContext -> {
                    CronTrigger cronTrigger = new CronTrigger(taskCron);
                    return cronTrigger.nextExecutionTime(triggerContext);
                });
        //将启动的任务放入 map
        scheduledFutureMap.put(taskKey, scheduledFuture);
        reportTaskConfigMapper.updateByTaskKey(taskKey, 1);
    }

    /**
     * 任务是否已经启动
     */
    private Boolean isStart(String taskKey) {
        //校验是否已经启动
        if (scheduledFutureMap.containsKey(taskKey)) {
            return !scheduledFutureMap.get(taskKey).isCancelled();
        }
        return false;
    }

    /**
     * 批量新增
     *
     * @param addTaskConfigParamList 实例对象的集合
     * @return 生效的条数
     */
    @Override
    public int addReportTask(List<AddTaskConfigParam> addTaskConfigParamList) {
        List<ReportTaskConfig> reportTaskConfigs = new ArrayList<>();
        LocalDateTime date = LocalDateTime.now();
        for (AddTaskConfigParam addTaskConfigParam : addTaskConfigParamList) {
            ReportTaskConfig reportTaskConfig = BeanCopierUtil.copyBean(addTaskConfigParam, ReportTaskConfig.class);
            reportTaskConfig.setTaskCron(CronUtil.createLoopCronExpression(addTaskConfigParam.getRate(), addTaskConfigParam.getCycle()));
            reportTaskConfig.setStartFlag(0);
            reportTaskConfig.setCreateTime(date);
            reportTaskConfig.setUpdateTime(date);
            reportTaskConfigs.add(reportTaskConfig);
        }
        return reportTaskConfigMapper.batchInsert(reportTaskConfigs);
    }

    /**
     * 根据任务配置id更新修改配置信息
     *
     * @param updateTaskConfigParam 实体类
     * @return Response对象
     */
    @Override
    public int updateReportTaskConfig(UpdateTaskConfigParam updateTaskConfigParam) {
        ReportTaskConfig reportTaskConfig = BeanCopierUtil.copyBean(updateTaskConfigParam, ReportTaskConfig.class);
        if (updateTaskConfigParam.getRate() != null && updateTaskConfigParam.getCycle() != null) {
            reportTaskConfig.setTaskCron(CronUtil.createLoopCronExpression(updateTaskConfigParam.getRate(), updateTaskConfigParam.getCycle()));
        }
        reportTaskConfig.setUpdateTime(LocalDateTime.now());
        return this.reportTaskConfigMapper.update(reportTaskConfig);
    }

    /**
     * 上报
     *
     * @param reportTaskParam 参数封装
     */
    @Override
    public void reportTask(ReportTaskParam reportTaskParam) {
        List<String> taskKeys = reportTaskParam.getTaskKeys();
        for (String taskKey : taskKeys) {
            ScheduledTaskJob scheduledTaskJob = ScheduledTaskEnum.getScheduledTaskJob(taskKey);
            if (scheduledTaskJob == null) {
                continue;
            }
            ReentrantLock reentrantLock = LockEnum.getReentrantLock(taskKey);
            if (reentrantLock != null && reentrantLock.tryLock()) {
                try {
//                    threadPoolTaskScheduler.execute(scheduledTaskJob);
                    scheduledTaskJob.run();
                } finally {
                    reentrantLock.unlock();
                }
            } else {
                throw new BusinessException(400, "上报中,请稍后再试");
            }
        }

    }

    /**
     * 根据任务id获取任务详情
     */
    @Override
    public ReportTaskConfigResp reportTaskDetail(SelectTaskDetailParam detailParam) {
        Integer taskId = detailParam.getTaskId();
        //查询详情
        ReportTaskConfig taskConfig = reportTaskConfigMapper.getReportTaskConfigById(taskId);
        ReportTaskConfigResp reportTaskConfigResp = BeanCopierUtil.copyBean(taskConfig, ReportTaskConfigResp.class);
        //查询记录统计
        if (Objects.nonNull(taskConfig)) {
            ReportTaskConfigResp.Count count = reportRecordMapper.statisticsCount(taskConfig.getTaskKey());
            reportTaskConfigResp.setCount(count);
        }
        return reportTaskConfigResp;
    }

    /**
     * 根据taskKey获取上报记录
     *
     * @param reportRecordQuery 参数封装
     */
    @Override
    public PageInfo<?> reportRecord(ReportRecordQuery reportRecordQuery) {
        String taskKey = reportRecordQuery.getTaskKey();
        List<Integer> reportStatus = reportRecordQuery.getReportStatus();
        PageHelper.startPage(reportRecordQuery.getPageNum(), reportRecordQuery.getPageSize());
        List<Map<String, Object>> mapList = reportRecordMapper.selectReportRecord(taskKey, reportStatus);
        for (Map<String, Object> map : mapList) {
            map.put("cmcid", ReportInfoConfig.cid);
        }
        return new PageInfo<>(mapList);
    }

    /**
     * 批量操作 (批量开关 & 批量修改间隔)
     */
    @Override
    public boolean batchReportTask(ReportBatchParam reportBatchParam) {
        Boolean switchType = reportBatchParam.getSwitchType();
        List<String> taskKeys = reportBatchParam.getTaskKeys();
        Integer cycle = reportBatchParam.getCycle();
        Integer rate = reportBatchParam.getRate();
        if (switchType) {
            taskKeys.forEach(
                    taskKey -> {
                        TaskConfigStartParam taskConfigStartParam = new TaskConfigStartParam();
                        taskConfigStartParam.setTaskKey(taskKey);
                        this.start(taskConfigStartParam);
                    }
            );
        } else {
            //关闭
            taskKeys.forEach(this::stop);
        }
        if (cycle != null && rate != null) {
            String cron = CronUtil.createLoopCronExpression(rate, cycle);
            reportTaskConfigMapper.updateConfigByTaskKey(taskKeys, cycle, rate, cron);
        }
        return true;
    }

    /**
     * 重报
     */
    @Override
    public Boolean anewReportTask(ReportAnewParam reportAnewParam) {
        //任务key 和表名相同
        String taskKey = reportAnewParam.getTaskKey();
        List<Integer> reportIds = reportAnewParam.getReportIds();

        AnewReportEnum reportEnum = AnewReportEnum.getInstance(taskKey);
        if (Objects.isNull(reportEnum)) {
            return false;
        }
        boolean successFlag;
        synchronized (this) {
            //获取上报数据
            List<Map<String, Object>> reportMap = reportTaskConfigMapper.getReportDataById(taskKey, reportIds);
            if (Objects.isNull(reportMap)) {
                return false;
            }
            //数据转换
            String jsonStr = JSONUtil.toJsonPrettyStr(reportMap);
            JSONArray reportData = JSONUtil.parseArray(jsonStr);
            // 数据上报
            LocalDateTime startTime = LocalDateTime.now();
            successFlag = DataCleanUtil.reportData(reportInfoConfig.getServerPath(), reportEnum.getUri(),
                    reportInfoConfig.getCmcId(), reportData);
            // 添加上报记录
            reportRecordService.addReportRecord(startTime, reportData.size(), taskKey, successFlag, true);
            //修改上报状态
            reportTaskConfigMapper.updateReportDataById(taskKey, reportIds, successFlag, System.currentTimeMillis());
        }
        return successFlag;
    }
}