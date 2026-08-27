package zm.mud.pkuxkx.trigger.action;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.trigger.Trigger;
import zm.mud.core.trigger.action.IAction;
import zm.mud.core.trigger.cfg.MatchResult;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.component.ImageInfo;
import zm.mud.utils.HttpUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


@Component("ACTION_FullmeShowAction")
@Scope("prototype")
public class FullmeShowAction implements IAction {
    private static final Logger logger = LogManager.getLogger(FullmeShowAction.class);
    private String actionCfgJsonStr;


    @Autowired
    private ZmMudUI ui;

    @Autowired
    private HttpUtil httpUtil;

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
       
        
        
        List<ImageInfo> imgUrls = new ArrayList<>();

        int fetchTimes = 4;
        for(int i = 0 ; i < fetchTimes; i ++){
            boolean insertMode = false;
            boolean needBeforeNewLine = false;
            if( i == 0){
                insertMode = false;  //第一张图片不使用insert模式，用来覆盖北侠默认预留的空行；
                needBeforeNewLine = true; //第一张图片显示前换行
            }
           
            String imgUrl = this.fetchImgUrl(fullmeUrl);
            ImageInfo imageInfo = new ImageInfo(imgUrl, insertMode);
            imageInfo.setNeedBeforeNewLine(needBeforeNewLine);
            imgUrls.add(imageInfo);
        }
        int fullmeUrlOffset = ui.getMsgOffset(fullmeUrl);
        logger.debug("Fullme URL offset:" + fullmeUrlOffset);
        ui.printImg(imgUrls,fullmeUrlOffset);
        logger.info(">>>>>>>>>> url:" + imgUrls);
    }

    private String fetchImgUrl(String fullmeUrl){
        return httpUtil.download(fullmeUrl, new Function<InputStream,String>(){
            String imgUrl = null;
            @Override
            public String apply(InputStream inputStream) {
                java.io.InputStreamReader reader = null;
                StringBuilder htmlBuilder = new StringBuilder();

                try {
                    reader = new java.io.InputStreamReader(inputStream,
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
                    java.util.regex.Matcher matcher = pattern.matcher(htmlBuilder.toString());

                    if (matcher.find()) {
                        String relativeSrc = matcher.group(1); // 拿到 "./b2evo_captcha_tmp/xxxx.jpg"
                        // 使用 URL 上下文构造，自动洗掉 "./" 并剔除 robot.php?filename=xxx
                        java.net.URL pageUrl = new java.net.URL(fullmeUrl);
                        imgUrl = new java.net.URL(pageUrl, relativeSrc).toString();
                    } else {
                        logger.error("解析 fullme 验证码 URL 失败: " + fullmeUrl);
                    }

                } catch (Exception e) {
                    logger.error("解析 fullme 验证码 URL 失败: " + e.getMessage());
                } finally {
                    try {
                        if (reader != null)
                            reader.close();
                    } catch(Exception e){
                    }
                }
                return imgUrl;
            }

        }, String.class);
    }


}
