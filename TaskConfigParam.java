package com.telecomyt.plat.cmac.report.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author zhangyingqi
 * @date 2020-12-11
 */
@Setter
@Getter
public class TaskConfigParam implements Serializable {

    @NotBlank(message = "taskKey不能为空")
    private String taskKey;
}
