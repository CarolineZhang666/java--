package com.telecomyt.plat.cmac.report.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zhangyingqi
 * @date 2020-12-11
 */
@Setter
@Getter
public class TaskConfigStartParam implements Serializable {

    /**
     * 任务标识，唯一key值
     */
    @NotBlank(message = "taskKey不能为空")
    private String taskKey;

    /**
     * 频率 0秒；1分；2小时；3日；4月
     */
    @Range(min = 0, max = 4)
    private Integer rate = 2;

    /**
     * cycle 周期，和rate搭配使用，比如rate = 2, cycle = 5,代表每隔5小时执行一次
     */
    private Integer cycle;
}
