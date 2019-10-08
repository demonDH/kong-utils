package com.kong.constant;

/**
 * @author kong
 */
public interface KongConst {

    String DD_WEBHOOK = "https://oapi.dingtalk.com/robot/send?access_token=0d5c48b33ebc38d8355f3f5838b579a9c7f4a4c5c167a12bc90d481b18f16";

    String DEGRADE_FST_ALARM_TPL = "接口%s, 在1s内连续%d个请求%s, 接下来【%ds】对该接口的请求将被熔断, 主机地址:%s, 报警时间:%s, 请尽快处理...";

    String DEGRADE_SND_ALARM_TPL = "接口%s, 在过去1分钟共熔断请求数:【%d】, 总请求数:【%d】, 失败率:【%s】, 熔断时间窗结束, 主机地址:%s, 报警时间:%s";
}