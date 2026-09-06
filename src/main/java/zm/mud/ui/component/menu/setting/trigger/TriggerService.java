package zm.mud.ui.component.menu.setting.trigger;

import java.util.ArrayList;
import java.util.List;

import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerType;
import zm.mud.ui.component.menu.KeyValuePair;
import zm.mud.utils.SpringBeanUtil;

public class TriggerService {

    public static final List<KeyValuePair<String, String>> matcherTypes;
    public static final List<KeyValuePair<String, String>> triggerTypes;
    public static final List<KeyValuePair<String, String>> actionTypes;

    static {
        matcherTypes = new ArrayList<>();
        matcherTypes.add(new KeyValuePair<String, String>("EndWith", "以...结尾"));
        matcherTypes.add(new KeyValuePair<String, String>("Equals", "等于"));
        matcherTypes.add(new KeyValuePair<String, String>("Include", "包含"));
        matcherTypes.add(new KeyValuePair<String, String>("Regex", "正则表达式"));
        matcherTypes.add(new KeyValuePair<String, String>("StartWith", "以...开头"));

        triggerTypes = new ArrayList<>();
        for (TriggerType type : TriggerType.values()) {
            triggerTypes.add(new KeyValuePair<String, String>(type.getType(), type.getDesc()));
        }

        actionTypes = new ArrayList<>();
        actionTypes.add(new KeyValuePair<String, String>("RegisterAction", "动态激活触发器"));
        actionTypes.add(new KeyValuePair<String, String>("SendCommand", "发送指令"));
        actionTypes.add(new KeyValuePair<String, String>("LuaScriptAction", "调用Lua脚本"));
        actionTypes.add(new KeyValuePair<String, String>("FullmeShowAction", "北侠Fullme动作器"));

    }

    public static List<KeyValuePair<String, String>> getMathers() {
        return matcherTypes;
    }

    public static KeyValuePair<String, String> getMatcherType(String typeKey) {
        for (KeyValuePair<String, String> type : matcherTypes) {
            if (type.getKey().equals(typeKey)) {
                return type;
            }
        }

        return new KeyValuePair<String, String>("null", "未知");
    }

    public static List<KeyValuePair<String, String>> getActionTypes() {
        return actionTypes;
    }

    public static KeyValuePair<String, String> getActionType(String actionTypeKey) {
        for (KeyValuePair<String, String> type : actionTypes) {
            if (type.getKey().equals(actionTypeKey)) {
                return type;
            }
        }

        return new KeyValuePair<String, String>("null", "未知");
    }

    public static List<KeyValuePair<String, String>> getTriggerTypes() {
        return triggerTypes;
    }

    public static KeyValuePair<String, String> getTriggerType(String triggerKey) {
        for (KeyValuePair<String, String> type : triggerTypes) {
            if (type.getKey().equals(triggerKey)) {
                return type;
            }
        }

        return new KeyValuePair<String, String>("null", "未知");
    }

    public static List<TriggerConfigEntry> getTriggerConfigEntries(String mudWorldCode) {
        TriggerFactory triggerFactory = SpringBeanUtil.getBean(TriggerFactory.class);
        return triggerFactory.getWorldTriggers("pkuxkx");
    }

    public static List<KeyValuePair<String, String>> getMudWorlds() {
        List<KeyValuePair<String, String>> mudWorlds  = new ArrayList<>();
        mudWorlds.add(new KeyValuePair<String, String>("pkuxkx", "北大侠客行"));
        return mudWorlds;
    }
}
