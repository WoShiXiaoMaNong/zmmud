package zm.mud.core.cfg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.TypeReference;

import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;

public class CustomCfgLoader {
    private static final Logger logger = LogManager.getLogger(CustomCfgLoader.class);

    public static Object loadUIConfig(String folder, String jsonFileName /* {jsonFileName}.json */, String key, TypeReference<?> type) {
        try {
            // 1. 获取 resources 下的文件输入流
            String fileFolder = (folder == null || folder.isEmpty()) ? "./" : folder;
            String configFileName = Paths.get(fileFolder, jsonFileName + ".json").toString();
            logger.info("Start to load UI config info from " + configFileName);
         
            AbstractResource resource = getFileResource(configFileName);
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

    public static <T>void saveConfig(String folder, String jsonFileName /* {mudName}.json */, String key, List<T> configs) {
        try {
            // 1. 拼装标准的硬盘物理路径
            String fileFolder = (folder == null || folder.isEmpty()) ? "./" : folder;
            File targetDir = new File(fileFolder);
            
            // 2. 核心防御：如果外部配置目录（如 ./conf/trigger）不存在，强行递归创建它
            if (!targetDir.exists()) {
                Files.createDirectories(targetDir.toPath());
            }

            File configFile = new File(targetDir, jsonFileName + ".json");
            JSONObject rootObject;

            // 3. 核心机制：检查文件是否已经存在
            if (configFile.exists() && configFile.length() > 0) {
                // 如果文件存在，先将其读取出来，避免直接覆盖抹除掉该文件内的其他 key 配置
                String existingJson;
                try (InputStream is = new java.io.FileInputStream(configFile);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    existingJson = reader.lines().collect(Collectors.joining("\n"));
                }
                
                if (existingJson != null && !existingJson.trim().isEmpty()) {
                    rootObject = JSON.parseObject(existingJson);
                } else {
                    rootObject = new JSONObject();
                }
            } else {
                // 如果是新文件，初始化一个空的根 JSON 对象
                rootObject = new JSONObject();
            }

            // 4. 将传入的触发器列表数据设置/替换到指定的 key 节点中
            rootObject.put(key, configs);

            // 5. 将更新后的完整 JSON 对象以漂亮的格式、UTF-8 编码安全地回写到硬盘中
            logger.info("Saving updated config to: " + configFile.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(configFile);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos, StandardCharsets.UTF_8))) {
                
                // 启用 PrettyFormat（带缩进的漂亮格式），并保留 Null 值输出（可选，有利于玩家手动编辑）
                String prettyJson = JSON.toJSONString(rootObject, 
                    JSONWriter.Feature.PrettyFormat, 
                    JSONWriter.Feature.WriteMapNullValue
                );
                
                writer.write(prettyJson);
                writer.flush();
            }
            
            logger.info("Successfully saved config for key: " + key);

        } catch (Exception e) {
            logger.error("Failed to save config for " + jsonFileName + ", key: " + key, e);
        }
    }

    private static AbstractResource getFileResource(String filePath){
        AbstractResource fr = null;
        try{
            File configFile = new File(filePath);
            fr = new FileSystemResource(configFile); 
        }catch(Exception e){
            fr = new ClassPathResource(filePath);
        }
        return fr;
    }
}
