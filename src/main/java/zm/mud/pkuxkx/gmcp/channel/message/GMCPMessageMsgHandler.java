package zm.mud.pkuxkx.gmcp.channel.message;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import zm.mud.core.session.MudSession;
import zm.mud.pkuxkx.gmcp.channel.IGMCPMsgHandler;
import zm.mud.ui.ZmMudUI;
import zm.mud.ui.component.ImageInfo;

@Component("GMCP.Message")
public class GMCPMessageMsgHandler implements IGMCPMsgHandler {
    private static final Logger logger = LogManager.getLogger(GMCPMessageMsgHandler.class);
    private static final String MESSAGE_TYPE_PIC = "pic";

    @Autowired
    private ZmMudUI ui;

   
    @Override
    public void parse(MudSession session,String packageName, String jsonPayload) {
        // 解析 GMCP 消息
        PkuxkxMessage message = JSON.parseObject(jsonPayload, PkuxkxMessage.class);

        if( MESSAGE_TYPE_PIC.equalsIgnoreCase(message.getType()) && message.getUrl() != null && !message.getUrl().isEmpty()) {
            
            ImageInfo imageInfo = new ImageInfo(this.getUrl(session,message.getUrl()), true,false);

            //这里可以只显示缩略图，用户看不清的时候，可以通过双击图片来查看原图
            ui.printImg(session,java.util.Collections.singletonList(imageInfo));

        }
    
    }

    private String getUrl(MudSession session,String url){
        String[] urlInfos = url.split(",");
        return urlInfos[0];
    }

}
