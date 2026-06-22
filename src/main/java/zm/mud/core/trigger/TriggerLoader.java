package zm.mud.core.trigger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import zm.mud.core.trigger.cfg.TriggerConfig;
import zm.mud.core.trigger.cfg.TriggerConfigEntry;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TriggerLoader {
    private static final Logger logger = LogManager.getLogger(TriggerLoader.class);

    public List<TriggerConfigEntry> loadTriggers() {
        try {
            // 1. 获取 resources 下的文件输入流
            logger.info("Start to load trigger info from trigger.json");
            ClassPathResource resource = new ClassPathResource("trigger.json");

            String jsonStr;
            try (InputStream is = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // 将文件流拼接转换为 String 字符串
                jsonStr = reader.lines().collect(Collectors.joining("\n"));
            }

            TriggerConfig config = JSON.parseObject(jsonStr, TriggerConfig.class);

            logger.info("Load trgger info end. Trigger size is " + config.getTriggers().size());

            return config.getTriggers();
        } catch (Exception e) {
            logger.error("Load trigger error!", e);
        }
        return Collections.emptyList();
    }
}
