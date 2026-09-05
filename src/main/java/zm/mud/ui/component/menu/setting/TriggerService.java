package zm.mud.ui.component.menu.setting;

import java.util.ArrayList;
import java.util.List;


import zm.mud.ui.component.menu.KeyValuePair;


public class TriggerService {

    public static List<KeyValuePair<String, String>> getMathers() {
        List<KeyValuePair<String, String>> keyValuePairs = new ArrayList<>();

        keyValuePairs.add(new KeyValuePair<String, String>("EndWith", "以...结尾"));
        keyValuePairs.add(new KeyValuePair<String, String>("Equals", "等于"));
        keyValuePairs.add(new KeyValuePair<String, String>("Include", "包含")); // 注：代码中拼写为 Inclucexx，如果是业务中的“包含”，对应字段常为
                                                                              // Include
        keyValuePairs.add(new KeyValuePair<String, String>("Regex", "正则表达式"));
        keyValuePairs.add(new KeyValuePair<String, String>("StartWith", "以...开头"));

        return keyValuePairs;
    }
}
