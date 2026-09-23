package zm.mud.ui.component.menu.setting.timer;

import java.util.ArrayList;
import java.util.List;
import zm.mud.ui.component.menu.KeyValuePair;
import zm.mud.utils.SpringBeanUtil;
import zm.mud.core.automation.timer.TimerFactory;
import zm.mud.core.automation.timer.cfg.TimerConfigEntry;

/**
 * 定时器业务逻辑持久化层（数据中心）
 */
public class TimerService {

 
    public static List<KeyValuePair<String, String>> getMudWorlds() {
        List<KeyValuePair<String, String>> mudWorlds  = new ArrayList<>();
        mudWorlds.add(new KeyValuePair<String, String>("pkuxkx", "北大侠客行"));
        return mudWorlds;
    }

    public static List<TimerConfigEntry> getTimersByWorld(String worldKey) {
        TimerFactory timerFactory = SpringBeanUtil.getBean(TimerFactory.class);
        timerFactory.load(worldKey,false);
        
        return timerFactory.getWorldTriggers(worldKey);
    }

    public static void saveTimersToWorld(String worldKey, List<TimerConfigEntry> timers) {
        TimerFactory timerFactory = SpringBeanUtil.getBean(TimerFactory.class);
        timerFactory.save(worldKey, timers);
    }
}
