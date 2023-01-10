package com.telecomyt.plat.cmac.report.util;

/**
 * cron表达式生成
 *
 * @author zhangyingqi
 * @date 2020-12-15
 */
public class CronUtil {

    /**
     * 方法摘要：构建Cron表达式
     *
     * @param rate  频率 0秒；1分；2小时；3日；4月
     * @param cycle 周期
     * @return String
     */
    public static String createLoopCronExpression(int rate, int cycle) {
        String cron = "";
        switch (rate) {
            case 0:
                cron = "0/" + cycle + " * * * * ?";
                break;
            case 1:
                cron = "0 0/" + cycle + " * * * ?";
                break;
            case 2:
                cron = "0 0 0/" + cycle + " * * ?";
                break;
            case 3:
                cron = "0 0 0 1/" + cycle + " * ?";
                break;
            case 4:
                cron = "0 0 0 1 1/" + cycle + " ?";
                break;
            default:
                cron = "0 0 0/1 * * ?";
                break;
        }
        return cron;
    }

}