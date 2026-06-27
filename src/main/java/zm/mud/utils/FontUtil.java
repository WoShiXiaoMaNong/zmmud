package zm.mud.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import java.awt.Font;
import java.awt.GraphicsEnvironment;


public class FontUtil {

    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager.getLogger(FontUtil.class);

     public static void registerFont() {
        // 1. 定義 resources/mono/ 下所有的字型檔名清單（精準匹配，避開 Jar 包無法遍歷的問題）
        List<String> fontFiles = Arrays.asList(
                "sarasa-mono.ttf" // 請替換成你實際的檔名
        );

        // 2. 獲取 JVM 本地圖形環境
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // 3. 循環載入並註冊每一個字型
        for (String fileName : fontFiles) {
            String resourcePath = "/fonts/" + fileName; // 注意你的路徑對應 resources/mono/

            try (InputStream is = FontUtil.class.getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("找不到字型資源檔: " + resourcePath);
                    continue;
                }

                // 載入字型
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

                // 註冊字型
                boolean success = ge.registerFont(baseFont);

                if (success) {
                    // 打印邏輯名稱與家族名稱，方便你後續在代碼中調用
                    logger.info("成功註冊字型 -> Name: " + baseFont.getName() + " | Family: " + baseFont.getFamily());
                } else {
                    logger.error("字型已存在或註冊失敗: " + fileName);
                }

            } catch (Exception e) {
                logger.error("載入字型失敗 [" + fileName + "] ",e);
            }
        }
    }

}
