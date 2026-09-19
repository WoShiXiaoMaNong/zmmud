package zm.mud.world.common.action;




import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.automation.action.ActionContext;
import zm.mud.core.automation.action.ActionContextVariableNames;
import zm.mud.core.automation.action.IAction;
import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.automation.trigger.cfg.MatchResult;
import zm.mud.core.session.MudSession;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.component.image.ImageInfo;
import zm.mud.ui.logger.UiLogger;
import zm.mud.utils.HttpUtil;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


@Component("ACTION_PrintUrlImgAction")
@Scope("prototype")
public class PrintUrlImgAction implements IAction {
    private static final Logger logger = LogManager.getLogger(PrintUrlImgAction.class);

    private String actionCfgJsonStr;
    @Autowired
    private ZmMudUI ui;

    @Autowired 
    private UiLogger uiLogger;

    @Autowired
    private HttpUtil httpUtil;

    private Map<String,Object> params;

    public PrintUrlImgAction(){
        this.params = new HashMap<>();
    }

    @Override
    public void execute(ActionContext context) {
        MatchResult ret = (MatchResult) context.get(ActionContextVariableNames.MATCHER_MATCHRET);
        MudSession session = context.getSession();
        String fullmeUrl = ret.getOriginMsg();
        List<ImageInfo> imgUrls = new ArrayList<>();
        int fetchTimes = this.getFetchTimes(session);
        boolean enableDoubleClickPopup = this.getClickPopup(session);
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
        int fullmeUrlOffset = ui.getMsgOffset(session,fullmeUrl);
        logger.debug("Fullme URL offset:" + fullmeUrlOffset);
        //北侠的fullme验证码图片是通过一个网页来展示的，最多允许刷新四次，都在客户端做掉了，所以这里不传入onDoubleClick事件，避免用户双击图片后又去刷新fullme网页
        
        if(enableDoubleClickPopup){
            ui.printImg(session,imgUrls,fullmeUrlOffset); 
        }else{
            ui.printImg(session,imgUrls,fullmeUrlOffset,null); 
        }
        logger.info(">>>>>>>>>> url:" + imgUrls);
    }

    private boolean getClickPopup(MudSession session) {
         Object enableDoubleClickPopupObj = this.params.get("ClickPopup");
        if (enableDoubleClickPopupObj == null) {
            uiLogger.warn(session, "未获取到 ClickPopup 参数，默认双击不弹框！");
            return false;
        }
        
        try {
            String strVal = enableDoubleClickPopupObj.toString().trim();
            return Boolean.parseBoolean(strVal);
        } catch (Exception e) {
            // 如果用户在 UI 输入了非数字（比如 "abc"），会进到这里
            logger.error("Get ClickPopup error. Invalid format: " + enableDoubleClickPopupObj, e);
        }
        return false;
    }

    private int getFetchTimes(MudSession session) {
        Object fetchTimesObj = this.params.get("FetchTimes");
        if (fetchTimesObj == null) {
            uiLogger.warn(session, "未获取到 FetchTimes 参数，默认打印一次网络图片！");
            return 1;
        }
        
        try {
            String strVal = fetchTimesObj.toString().trim();
            return Integer.parseInt(strVal);
        } catch (Exception e) {
            // 如果用户在 UI 输入了非数字（比如 "abc"），会进到这里
            logger.error("Get Fetch times error. Invalid number format: " + fetchTimesObj, e);
        }
        return 1;
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
                    // 1. 定义最大允许的读取耗时（10 秒）
                    long maxWaitTimeMs = 10000; 
                    long startTime = System.currentTimeMillis();
                    // 绝对不要用 readLine()！用 read(buffer) 块读取
                    while ((charsRead = reader.read(buffer)) != -1) {
                        htmlBuilder.append(buffer, 0, charsRead);
                        String currentContent = htmlBuilder.toString();

                        // 发现包含 .jpg" 或 <br>，说明图片地址已经成功进入内存
                        if (currentContent.contains(".jpg\"") || currentContent.contains("<br>")) {
                            break; // 强行阻断、立即退出循环，不给服务器挂起超时的机会！
                        }
                        // 2. 超时检测：判断当前时间是否超过了设定的最大等待时间
                        if ((System.currentTimeMillis() - startTime) > maxWaitTimeMs) {
                            // 可以选择直接 break，或者抛出异常以便上层捕获处理
                            // throw new java.net.SocketTimeoutException("读取 HTML 内容超时");
                            break; 
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

        @Override
    public void setExpression(String expression) {
        actionCfgJsonStr = expression;
    }



    @Override
    public Object getParam(String paramKey) {
       return this.params.get(paramKey);
    }



    @Override
    public void setParams(Map<String, Object> params) {
        this.params.putAll(params);
    }



    @Override
    public String getExpression() {
        return this.actionCfgJsonStr;
    }

}
