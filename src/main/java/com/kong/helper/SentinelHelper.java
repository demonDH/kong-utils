package com.kong.helper;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO;
import static com.alibaba.csp.sentinel.slots.block.RuleConstant.DEGRADE_GRADE_RT;
import static com.kong.constant.KongConst.DD_WEBHOOK;
import static com.kong.constant.KongConst.DEGRADE_FST_ALARM_TPL;
import static com.kong.constant.KongConst.DEGRADE_SND_ALARM_TPL;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.kong.util.DateUtil;
import com.kong.util.DingUtil;
import com.kong.util.IpUtil;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kong
 */
@Slf4j
public class SentinelHelper {

    private SentinelHelper() {}

    private volatile static SentinelHelper instance = null;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static Map<String, DegradeRule> degradeRuleMap = new ConcurrentHashMap<>();

    private static ScheduledExecutorService pool = new ScheduledThreadPoolExecutor(
        Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("degrade-alarm-reset-task", true));

    public static SentinelHelper getInstance() {
        if (instance == null) {
            synchronized (SentinelHelper.class) {
                if (instance == null) {
                    instance = new SentinelHelper();
                }
            }
        }
        return instance;
    }

    public void degradeAlarm(Throwable cause) {
        if (Objects.isNull(cause)) {
            return;
        }
        if (cause instanceof DegradeException) {
            DegradeException ex = (DegradeException)cause;
            DegradeRule rule = ex.getRule();
            DegradeRule alarmedRule = degradeRuleMap.putIfAbsent(rule.getResource(), rule);

            if (alarmedRule != null) {
                return;
            }

            String pushMsg = pushFstAlarmMsg(rule);
            CompletableFuture.runAsync(() -> DingUtil.push(DD_WEBHOOK, pushMsg));
            log.info("Fst AlarmMsg, msg:{}", pushMsg);

            ResetTask resetTask = new ResetTask(rule);
            pool.schedule(resetTask, rule.getTimeWindow(), TimeUnit.SECONDS);
        }
    }

    private String pushFstAlarmMsg(DegradeRule rule) {

        // Degrade strategy (0: average RT, 1: exception ratio)
        int grade = rule.getGrade();
        String metricDesc = "";
        if (grade == DEGRADE_GRADE_RT) {
            metricDesc = "RT大于【" + rule.getCount() + "ms】";
        } else if (grade == DEGRADE_GRADE_EXCEPTION_RATIO) {
            metricDesc = "异常比例大于【" + rule.getCount() + "】";
        }

        return String.format(DEGRADE_FST_ALARM_TPL, rule.getResource(), rule.getPassCount().get(),
            metricDesc, rule.getTimeWindow(), IpUtil.currHostIp(), DateUtil.getCurrentTime(FORMATTER));
    }

    private static final class ResetTask implements Runnable {

        private DegradeRule degradeRule;

        ResetTask(DegradeRule rule) {
            this.degradeRule = rule;
        }

        private Map<String, DegradeRule> getDegradeRuleMap() {
            return degradeRuleMap;
        }

        @Override
        public void run() {
            getDegradeRuleMap().remove(degradeRule.getResource());
            log.info("Snd AlarmMsg, msg:{}", pushSndAlarmMsg(degradeRule));
            DingUtil.push(DD_WEBHOOK, pushSndAlarmMsg(degradeRule));
        }

        private String pushSndAlarmMsg(DegradeRule rule) {
            ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(rule.getResource());
            long blockReq = clusterNode.blockRequest();
            long totalReq = clusterNode.totalRequest();
            String failRate = String.format("%.2f", (float) blockReq / totalReq);

            return String.format(DEGRADE_SND_ALARM_TPL, rule.getResource(), blockReq, totalReq,
                failRate, IpUtil.currHostIp(), DateUtil.getCurrentTime(FORMATTER));
        }
    }
}