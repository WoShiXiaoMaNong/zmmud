package zm.mud.core.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.ui.ZmMudUI;

import zm.mud.ui.component.ImageInfo;
/**
 * <pre>
 * 开放给Lua脚本的所有接口
 * </pre>
 * LuaApi
 */

@Service
public class LuaApi implements ILuaApi{
    private static final Logger logger = LogManager.getLogger(LuaApi.class);
    
    @Autowired
    private OubMsgService oubMsgService;

    @Autowired
    private ZmMudUI ui;

    public void sendMsg(String msg){
        logger.info(msg);
    }

    public void sendCommand(String command){
       // oubMsgService.send(command);
           
        List<ImageInfo> imgUrls = new ArrayList<>();
        int fetchTimes = 4;
        for(int i = 0 ; i < fetchTimes; i ++){
              boolean insertMode = false;
            boolean needBeforeNewLine = false;
            if( i == 0){
                insertMode = false;  //第一张图片不使用insert模式，用来覆盖北侠默认预留的空行；
                needBeforeNewLine = true; //第一张图片显示前换行
            }
             String imgUrl = "https://gips2.baidu.com/it/u=195724436,3554684702&fm=3028&app=3028&f=JPEG&fmt=auto?w=1280&h=960";
              ImageInfo imageInfo = new ImageInfo(imgUrl, insertMode);
              imageInfo.setMaxWidth(200);
              imageInfo.setNeedBeforeNewLine(needBeforeNewLine);
            imgUrls.add(imageInfo);
        }
       
     
        ui.printImg(imgUrls);
        logger.info(">>>>>>>>>> url:" + imgUrls);
    }

    public void sleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
    }
}
