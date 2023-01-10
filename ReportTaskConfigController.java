package com.telecomyt.plat.cmac.report.controller;

import com.github.pagehelper.PageInfo;
import com.telecomyt.plat.cmac.common.resp.BaseResp;
import com.telecomyt.plat.cmac.common.resp.ResultUtil;
import com.telecomyt.plat.cmac.report.dto.query.ReportRecordQuery;
import com.telecomyt.plat.cmac.report.dto.query.TaskConfigPageQuery;
import com.telecomyt.plat.cmac.report.dto.request.*;
import com.telecomyt.plat.cmac.report.dto.response.ReportTaskConfigResp;
import com.telecomyt.plat.cmac.report.entity.ReportTaskConfig;
import com.telecomyt.plat.cmac.report.service.ReportTaskConfigService;
import com.telecomyt.plat.cmac.report.util.PageResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * (ReportTaskConfig)控制层
 *
 * @author zhangyingqi
 * @since 2020-12-11 11:46:34
 */
@Validated
@RestController
@RequestMapping("/reportTask")
public class ReportTaskConfigController {
    /**
     * 服务对象
     */
    @Autowired
    private ReportTaskConfigService reportTaskConfigService;


    /**
     * 添加任务
     *
     * @param addTaskConfigParamList 参数对象
     */
    @PostMapping(value = "/add", produces = "application/json")
    public BaseResp<?> addReportTask(@Valid @RequestBody List<AddTaskConfigParam> addTaskConfigParamList) {
        int result = reportTaskConfigService.addReportTask(addTaskConfigParamList);
        return result > 0 ? ResultUtil.ok() : ResultUtil.error();
    }

    /**
     * 根据任务配置id更新修改配置信息
     *
     * @param updateTaskConfigParam 实体类
     * @return Response对象
     */
    @PostMapping(value = "/update", produces = "application/json")
    public BaseResp<?> update(@Valid @RequestBody UpdateTaskConfigParam updateTaskConfigParam) {
        int result = reportTaskConfigService.updateReportTaskConfig(updateTaskConfigParam);
        return result > 0 ? ResultUtil.ok() : ResultUtil.error();
    }

    /**
     * 所有任务列表
     */
    @PostMapping(value = "/list", produces = "application/json")
    public BaseResp<?> taskList(@RequestBody TaskConfigPageQuery taskConfigPageQuery) {
        PageInfo<ReportTaskConfig> reportTaskConfigRespPageInfo = reportTaskConfigService.taskList(taskConfigPageQuery);
        return ResultUtil.ok(PageResultUtil.requestPage(reportTaskConfigRespPageInfo));
    }

    /**
     * 根据任务key => 启动任务
     */
    @PostMapping(value = "/start", produces = "application/json")
    public BaseResp<?> start(@Valid @RequestBody TaskConfigStartParam taskConfigStartParam) {
        reportTaskConfigService.start(taskConfigStartParam);
        return ResultUtil.ok();
    }

    /**
     * 根据任务key => 停止任务
     */
    @PostMapping(value = "/stop", produces = "application/json")
    public BaseResp<?> stop(@Valid @RequestBody TaskConfigParam taskConfigParam) {
        reportTaskConfigService.stop(taskConfigParam.getTaskKey());
        return ResultUtil.ok();
    }

    /**
     * 根据任务key => 重启任务
     */
    @PostMapping(value = "/restart", produces = "application/json")
    public BaseResp<?> restart(@Valid @RequestBody TaskConfigParam taskConfigParam) {
        reportTaskConfigService.restart(taskConfigParam.getTaskKey());
        return ResultUtil.ok();
    }

    /**
     * 上报  : 立即执行 一次定时器
     *
     * @param reportTaskParam 参数封装
     */
    @PostMapping(value = "/report", produces = "application/json")
    public BaseResp<?> reportTask(@Valid @RequestBody ReportTaskParam reportTaskParam) {
        reportTaskConfigService.reportTask(reportTaskParam);
        return ResultUtil.ok();
    }

    /**
     * 根据任务id获取任务详情
     */
    @PostMapping(value = "/detail", produces = "application/json")
    public BaseResp<?> reportTaskDetail(@Valid @RequestBody SelectTaskDetailParam detailParam) {
        ReportTaskConfigResp reportTaskConfigResp = reportTaskConfigService.reportTaskDetail(detailParam);
        return ResultUtil.ok(reportTaskConfigResp);
    }

    /**
     * 批量操作 (批量开关 & 批量修改间隔)
     */
    @PostMapping(value = "/batch", produces = "application/json")
    public BaseResp<?> batchReportTask(@Valid @RequestBody ReportBatchParam reportBatchParam) {
        boolean result = reportTaskConfigService.batchReportTask(reportBatchParam);
        return result ? ResultUtil.ok() : ResultUtil.error();
    }

    /**
     * 根据taskKey获取上报记录
     *
     * @param reportRecordQuery 参数封装
     */
    @PostMapping(value = "/record", produces = "application/json")
    public BaseResp<?> reportRecord(@Valid @RequestBody ReportRecordQuery reportRecordQuery) {
        PageInfo<?> pageList = reportTaskConfigService.reportRecord(reportRecordQuery);
        return ResultUtil.ok(PageResultUtil.requestPage(pageList));
    }

    /**
     * 重报
     */
    @PostMapping(value = "/anew", produces = "application/json")
    public BaseResp<?> anewReportTask(@Valid @RequestBody ReportAnewParam reportAnewParam) {
        Boolean result = reportTaskConfigService.anewReportTask(reportAnewParam);
        return result ? ResultUtil.ok() : ResultUtil.error("上报数据格式有误");
    }
}