package zm.mud.core.protocol.iac.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import zm.mud.core.cfg.ApplicationConfig;
import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.session.MudSession;
import zm.mud.utils.HexUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Component(IACConsts.IAC_HANDLER_BEAN_PREFIX + "C9")
public class IACHandler_C9 extends AbsIACHandler{
    private static final Logger logger = LogManager.getLogger(IACHandler_C9.class);

    @Autowired
    private HexUtil hexUtil;

    @Autowired
    private ApplicationConfig appCfg;


     @Override
    protected List<byte[]> handle_WILL(MudSession session,byte[] iacCommand) {
        return this.handle_DO(session,iacCommand);
    }


    @Override
    protected List<byte[]> handle_DO(MudSession session,byte[] iacCommand) {
        int opsCode = iacCommand[2] & 0xFF;

        if( IACConsts.enabledOpsSet.contains(opsCode) && appCfg.isGMCPEnabled()){
            return this.doEnable(iacCommand);
        }else{
            return this.doReject(iacCommand);
        }
    }

    private List<byte[]> doEnable(byte[] iacCommand){
        List<byte[]> rets = new ArrayList<>();
        int commandCode = iacCommand[1] & 0xFF;
         if( !IACConsts.enableCmdMap.containsKey(commandCode)){
            logger.error("Unsupport cmd:" + Arrays.toString(hexUtil.toHex(iacCommand)));
            return null;  
        }

        // 生成响应指令 
        byte ret[] = new byte[3];
        ret[0] = iacCommand[0];
        ret[1] = IACConsts.enableCmdMap.get(commandCode).byteValue();
        ret[2] = iacCommand[2];
        rets.add(ret); // 

        logger.info("【IAC】启用: " + Arrays.toString(hexUtil.toHex(ret)));
        return rets;
    }

   
    private List<byte[]> doReject(byte[] iacCommand){
        int commandCode = iacCommand[1] & 0xFF;
        if( !IACConsts.rejectCmdMap.containsKey(commandCode)){
            return null;  // 例如 DONT 和 WONT， 不应该回复
        }

        byte ret[] = new byte[3];
        ret[0] = iacCommand[0];
        ret[1] = IACConsts.rejectCmdMap.get(commandCode).byteValue();
        ret[2] = iacCommand[2];

        return Arrays.asList(ret);
    }



    @Override
    protected List<byte[]> handle_DONT(MudSession session,byte[] iacCommand) {
        return null;
    }

        @Override
    protected List<byte[]> handle_WONT(MudSession session,byte[] iacCommand) {
        return null;
    }
    
}
