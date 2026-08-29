package zm.mud.core.automation.alias;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

@Component
public class AliasLoader {
 private static final Logger logger = LogManager.getLogger(AliasLoader.class);

    public List<Alias> loadAlias() {
        try {
            // 1. 获取 resources 下的文件输入流
            logger.info("Start to load Alias info from alias.json");
            ClassPathResource resource = new ClassPathResource("alias.json");

            String jsonStr;
            try (InputStream is = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // 将文件流拼接转换为 String 字符串
                jsonStr = reader.lines().collect(Collectors.joining("\n"));
            }

            Map<String, List<Alias>> aliasMap = JSON.parseObject(jsonStr, new com.alibaba.fastjson2.TypeReference<Map<String, List<Alias>>>() {});
            List<Alias> alias = aliasMap.get("alias");  
            logger.info("Load alias info end. Alias size is " + alias.size());

            return alias;
        } catch (Exception e) {
            logger.error("Load alias error!", e);
        }
        return Collections.emptyList();
    }
}
