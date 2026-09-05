package zm.mud.core.cfg;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ClassPathResource;

import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.TypeReference;

public class CustomCfgLoader {
    private static final Logger logger = LogManager.getLogger(CustomCfgLoader.class);

    public static Object loadUIConfig(String jsonFileName /* {mudName}.json */, String key, TypeReference<?> type) {
        try {
            // 1. 获取 resources 下的文件输入流
            String configFileName = jsonFileName + ".json";
            logger.info("Start to load UI config info from " + configFileName);
            ClassPathResource resource = new ClassPathResource(configFileName);

            String jsonStr;
            try (InputStream is = resource.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // 将文件流拼接转换为 String 字符串
                jsonStr = reader.lines().collect(Collectors.joining("\n"));
            }

            // ================== Fastjson2 修正后的补全代码 ==================
            if (jsonStr != null && !jsonStr.trim().isEmpty()) {
                // 1. 拼接成 Fastjson2 格式的 JSONPath 路径，例如 "$.pkuxkx.status_bar"
                String path = "$." + key;

                // 2. 在创建 JSONPath 时指定具体的 Type（通过 type.getType() 传入真实泛型类型）
                JSONPath jsonPath = JSONPath.of(path, type.getType());

                // 3. 直接通过 jsonStr 提取出强类型的数据
                Object result = jsonPath.extract(jsonStr);

                if (result != null) {
                    return result;
                }
            }
            // ================== =========================== ==================

        } catch (Exception e) {
            logger.error("Load trigger error!", e);
        }
        return null;
    }
}
