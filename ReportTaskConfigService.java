package com.telecomyt.plat.cmac.report.service;

import com.github.pagehelper.PageInfo;
import com.telecomyt.plat.cmac.report.dto.query.ReportRecordQuery;
import com.telecomyt.plat.cmac.report.dto.query.TaskConfigPageQuery;
import com.telecomyt.plat.cmac.report.dto.request.*;
import com.telecomyt.plat.cmac.report.dto.response.ReportTaskConfigResp;
import com.telecomyt.plat.cmac.report.entity.ReportTaskConfig;

import java.util.List;

/**
 * (ReportTaskConfig)表服务接口
 *
 * @author zhangyingqi
 * @since 2020-12-11 11:46:35
 */
public interface ReportTaskConfigService {

    /**
     * 批量新增
     *
     * @param addTaskConfigParamList 实例对象的集合
     * @return 影响行数
     */
    int addReportTask(List<AddTaskConfigParam> addTaskConfigParamList);

    /**
     * 根据任务配置id更新修改配置信息
     *
     * @param updateTaskConfigParam 实体类
     * @return Response对象
     */
    int updateReportTaskConfig(UpdateTaskConfigParam updateTaskConfigParam);

    /**
     * 所有任务列表
     */
    PageInfo<ReportTaskConfig> taskList(TaskConfigPageQuery taskConfigPageQuery);

    /**
     * 根据任务key 启动任务
     */
    Boolean start(TaskConfigStartParam taskConfigStartParam);

    /**
     * 根据任务key 停止任务
     */
    Boolean stop(String taskName);

    /**
     * 根据任务key 重启任务
     */
    Boolean restart(String taskName);


    /**
     * 程序启动时初始化  ==> 启动所有正常状态的任务
     */
    void initAllTask(List<ReportTaskConfig> reportTaskConfigRespList);

    /**
     * 上报
     *
     * @param reportTaskParam 参数封装
     */
    void reportTask(ReportTaskParam reportTaskParam);

    /**
     * 根据任务id获取任务详情
     */
    ReportTaskConfigResp reportTaskDetail(SelectTaskDetailParam detailParam);

    /**
     * 批量操作 (批量开关 & 批量修改间隔)
     */
    boolean batchReportTask(ReportBatchParam reportBatchParam);

    /**
     * 根据taskKey获取上报记录
     *
     * @param reportRecordQuery 参数封装
     */
    PageInfo<?> reportRecord(ReportRecordQuery reportRecordQuery);

    /**
     * 重报
     */
    Boolean anewReportTask(ReportAnewParam reportAnewParam);
}