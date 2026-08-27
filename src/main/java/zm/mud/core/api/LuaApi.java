package zm.mud.core.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


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


    public void sendMsg(String msg){
        logger.info(msg);
    }

    public void sendCommand(String command){
        oubMsgService.send(command);
           
    }

    public void sleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
    }
}
