package zm.mud.utils;

import java.io.InputStream;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class HttpUtil {
    private static final Logger logger = LogManager.getLogger(HttpUtil.class);

    public <T>T download(String urlStr, Function<InputStream, T> onDownload,Class<T> t) {
        T ret = null;
        java.net.HttpURLConnection connection = null;
        java.io.InputStream inputStream = null;
        try {
            logger.info("开始下载 " + urlStr);

            // 2. 伪装浏览器请求
            java.net.URL url = new java.net.URL(urlStr);
            connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();
            if (responseCode != java.net.HttpURLConnection.HTTP_OK) {
                throw new java.io.IOException("服务器返回错误状态码: " + responseCode);
            }

            inputStream = connection.getInputStream();

            // 3. 回调
            ret = onDownload.apply(inputStream);

        } catch (Exception e) {
            logger.error("下载 失败: " + urlStr, e);
        } finally {
            // 6. 优雅关闭流和连接
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return (T)ret;
    }
}
