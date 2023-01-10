package com.telecomyt.plat.cmac.report.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * (ReportTaskConfig)实体类
 *
 * @author zhangyingqi
 * @since 2020-12-11 11:46:37
 */
@Getter
@Setter
public class ReportTaskConfig implements Serializable {
    private static final long serialVersionUID = -23153981703695923L;
    /**
     * 主键
     */
    private Integer id;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务key值，任务标识
     */
    private String taskKey;
    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 频率 0秒；1分；2小时；3日；4月
     */
    private Integer rate;

    /**
     * cycle 周期，和rate搭配使用，比如rate = 2, cycle = 5,代表每隔5小时执行一次
     */
    private Integer cycle;
    /**
     * 任务表达式
     */
    private String taskCron;
    /**
     * 程序初始化是否启动 1 是 0 否
     */
    private Integer initStartFlag;
    /**
     * 当前是否启动，默认0 未启动 1 启动
     */
    private Integer startFlag;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

}