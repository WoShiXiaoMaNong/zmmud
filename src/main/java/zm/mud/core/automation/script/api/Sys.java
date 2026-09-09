package zm.mud.core.automation.script.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.api.OubMsgService;
import zm.mud.core.automation.script.lua.ILuaApi;
import zm.mud.core.session.MudSession;


/**
 * <pre>
 * 开放给Lua脚本的所有接口
 * </pre>
 * LuaApi
 */

@Service
public class Sys implements ILuaApi{
    private static final Logger logger = LogManager.getLogger(Sys.class);
    
    @Autowired
    private OubMsgService oubMsgService;


    public void print(MudSession session,String msg){
        logger.info(msg);
    }

    public void send(MudSession session,String command){
        oubMsgService.send(session,command);
           
    }

    public void sleep(long milliseconds){
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            logger.error("Sleep interrupted", e);
        }
    }
}
