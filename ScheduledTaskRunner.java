package com.telecomyt.plat.cmac.report.config;

import com.telecomyt.plat.cmac.report.constant.SystemConstant;
import com.telecomyt.plat.cmac.report.entity.ReportTaskConfig;
import com.telecomyt.plat.cmac.report.mapper.ReportTaskConfigMapper;
import com.telecomyt.plat.cmac.report.service.ReportTaskConfigService;
import com.telecomyt.plat.cmac.report.util.DataCleanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangyingqi
 * 项目启动完毕后开启需要自启的任务
 */
@Slf4jv
@Component
public class ScheduledTaskRunner implements ApplicationRunner {

    @Resource
    private ReportTaskConfigMapper reportTaskConfigMapper;

    @Autowired
    private ReportTaskConfigService reportTaskConfigService;

    @Autowired
    private ReportInfoConfig reportInfoConfig;

    /**
     * 程序启动完毕后,需要自启的任务
     */
    @Override
    public void run(ApplicationArguments applicationArguments) {
        log.info(" >>>>>> 项目启动完毕, 开启 => 需要自启的任务 开始!");
        List<ReportTaskConfig> reportTaskConfigRespList = reportTaskConfigMapper.getAllNeedStartTask();
        reportTaskConfigService.initAllTask(reportTaskConfigRespList);
        log.info(" >>>>>> 项目启动完毕, 开启 => 需要自启的任务 结束！");
    }

    /**
     * 上报数据清空
     */
    private void dataClean() {
        String serverPath = reportInfoConfig.getServerPath();
        String cmcId = reportInfoConfig.getCmcId();
        // 应用基本信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.APP_INFO_CLEAN_REPORT_URL, cmcId);
        // 应用状态信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.APP_STATUS_INFO_CLEAN_REPORT_URL, cmcId);
        // 终端基本信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.TERMINAL_INFO_CLEAN_REPORT_URL, cmcId);
        // 终端状态信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.TERMINAL_STATUS_CLEAN_INFO_REPORT_URL, cmcId);
        // 用户信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.USER_INFO_CLEAN_REPORT_URL, cmcId);
        // 组织结构信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.ORG_INFO_CLEAN_REPORT_URL, cmcId);
        // 硬件信息
        DataCleanUtil.cleanData(serverPath, SystemConstant.HOST_INFO_CLEAN_REPORT_URL, cmcId);
    }

}