package zm.mud.pkuxkx.gmcp.channel.message;



import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;

import zm.mud.pkuxkx.gmcp.GMCPContext;
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
    public void parse(String packageName, String jsonPayload, GMCPContext gmcpContext) {
        // 解析 GMCP 消息
        PkuxkxMessage message = JSON.parseObject(jsonPayload, PkuxkxMessage.class);

        if( MESSAGE_TYPE_PIC.equalsIgnoreCase(message.getType()) && message.getUrl() != null && !message.getUrl().isEmpty()) {
            ImageInfo imageInfo = new ImageInfo(message.getUrl(), true,false);
            imageInfo.setMaxWidth(200);
            ui.printImg(java.util.Collections.singletonList(imageInfo));

        }
    
    }

}
