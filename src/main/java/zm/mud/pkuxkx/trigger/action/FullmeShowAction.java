package zm.mud.pkuxkx.trigger.action;

import java.net.HttpURLConnection;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.action.IAction;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.ui.ZmMudUI;

@Component("ACTION_FullmeShowAction")
@Scope("prototype")
public class FullmeShowAction implements IAction {
    private static final Logger logger = LogManager.getLogger(FullmeShowAction.class);
    private String actionCfgJsonStr;


    @Autowired
    private ZmMudUI ui;

    @Override
    public void setExpression(String expression) {
        actionCfgJsonStr = expression;
    }



    @Override
    public String getExpression() {
        return this.actionCfgJsonStr;
    }

    @Override
    public void execute(Trigger trigger, MatchResult ret) {
        String fullmeUrl = ret.getOriginMsg();
        String imgUrl = this.fetchImgUrl(fullmeUrl);
        logger.info(">>>>>>>>>> url:" + imgUrl);
    }

    private String fetchImgUrl(String fullmeUrl) {
        String imgUrl = null;
        HttpURLConnection connection = null;
        java.io.InputStreamReader reader = null;
        StringBuilder htmlBuilder = new StringBuilder();

        try {
            java.net.URL pageUrl = new java.net.URL(fullmeUrl);
            connection = (HttpURLConnection) pageUrl.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            connection.setConnectTimeout(3000); // 建立连接超时 3 秒
            connection.setReadTimeout(4000); // 满 4 秒未读到数据才超时

            reader = new java.io.InputStreamReader(connection.getInputStream(),
                    java.nio.charset.StandardCharsets.UTF_8);

            char[] buffer = new char[64];
            int charsRead;

            // 绝对不要用 readLine()！用 read(buffer) 块读取
            while ((charsRead = reader.read(buffer)) != -1) {
                htmlBuilder.append(buffer, 0, charsRead);
                String currentContent = htmlBuilder.toString();

                // 发现包含 .jpg" 或 <br>，说明图片地址已经成功进入内存
                if (currentContent.contains(".jpg\"") || currentContent.contains("<br>")) {
                    break; // 强行阻断、立即退出循环，不给服务器挂起超时的机会！
                }
            }

            // 使用正则表达式从已经截获的 HTML 片段中提取相对路径
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<img\\s+[^>]*src=\"([^\"]+)\"");
            java.util.regex.Pattern.compile("<img\\s+[^>]*src=\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(htmlBuilder.toString());

            if (matcher.find()) {
                String relativeSrc = matcher.group(1); // 拿到 "./b2evo_captcha_tmp/xxxx.jpg"
                // 使用 URL 上下文构造，自动洗掉 "./" 并剔除 robot.php?filename=xxx
                imgUrl = new java.net.URL(pageUrl, relativeSrc).toString();
                int fullmeUrlOffset = ui.getMsgOffset(fullmeUrl);
                ui.printImg(imgUrl,fullmeUrlOffset,false); //不使用insert模式，直接覆盖
            } else {
                logger.error("解析 fullme 验证码 URL 失败: " + fullmeUrl);
            }

        } catch (Exception e) {
            logger.error("解析 fullme 验证码 URL 失败: " + e.getMessage());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception e) {
            }
            if (connection != null)
                connection.disconnect();
        }
        return imgUrl;
    }

}
