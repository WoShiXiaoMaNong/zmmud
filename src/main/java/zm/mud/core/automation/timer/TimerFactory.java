package zm.mud.core.automation.timer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.TypeReference;

import zm.mud.core.automation.timer.cfg.TimerConfigEntry;
import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.cfg.CustomCfgLoader;
import zm.mud.core.consts.ConfigConsts;
import zm.mud.core.session.MudSession;

@Service
public class TimerFactory {
    private static final Logger logger = LogManager.getLogger(TriggerFactory.class);

    private Map<String/* Mud World Code */, List<TimerConfigEntry>> worldTimers;

    private Map<String/* Session ID */, List<TimerConfigEntry>> sessionTimers;
    private Map<String/* Session ID */, Map<String, TimerConfigEntry>> sessionTimerMap;

    public TimerFactory(){
        this.worldTimers = new ConcurrentHashMap<>();
        this.sessionTimers = new ConcurrentHashMap<>();
        this.sessionTimerMap = new ConcurrentHashMap<>();
    }


    public synchronized void load(String mudWorldCode,boolean forceLoad/* 强制从配置文件读取 */){
        logger.info("Timer init start....");
        if (this.worldTimers.containsKey(mudWorldCode)){
            return;
        }
        List<TimerConfigEntry> timersForCurrentWorld = (List<TimerConfigEntry>) CustomCfgLoader.loadUIConfig(
                ConfigConsts.TIMER_CONFG_PATH, mudWorldCode, "timers",
                new TypeReference<List<TimerConfigEntry>>() {
                });
        this.worldTimers.put(mudWorldCode, timersForCurrentWorld);
    }

    public synchronized void reload(MudSession session) {

        logger.info("Trigger init start....");
        this.load(session.getMudWorldCode(),true);

        this.sessionTimers.remove(session.getSessionId());
        this.sessionTimerMap.remove(session.getSessionId());

        List<TimerConfigEntry> timersForCurrentWorld = this.getWorldTriggers(session.getMudWorldCode());

        this.sessionTimers.put(session.getSessionId(), timersForCurrentWorld);

        for (TimerConfigEntry cfgEntry : timersForCurrentWorld) {
            Map<String, TimerConfigEntry> triggerMapForCurentSession = this.sessionTimerMap.get(session.getSessionId());
            if (triggerMapForCurentSession == null) {
                triggerMapForCurentSession = new HashMap<>();
                this.sessionTimerMap.put(session.getSessionId(), triggerMapForCurentSession);
            }
            triggerMapForCurentSession.put(cfgEntry.getName(), cfgEntry);
        }
        logger.info("Trigger init finished");

        logger.info("Clean trgger!");

    }

    public List<TimerConfigEntry> getTriggers(MudSession session) {
        return this.sessionTimers.get(session.getSessionId());
    }

    public List<TimerConfigEntry> getWorldTriggers(String mudWorldCode) {
        return this.worldTimers.get(mudWorldCode);
    }

    public synchronized void save(String mudWorldCode, List<TimerConfigEntry> configs) {

        // 1. 保存配置文件

        CustomCfgLoader.saveConfig(
                ConfigConsts.TIMER_CONFG_PATH,
                mudWorldCode,
                "timers",
                configs);

        // 2. 重新加载

        for (String sessionId : sessionTimers.keySet()) {
            MudSession session = MudSession.getSession(sessionId);
            if (session == null) {
                continue;
            }
            reload(session);
        }

    }

}
