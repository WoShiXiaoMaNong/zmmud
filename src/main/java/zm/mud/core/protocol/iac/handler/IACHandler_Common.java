package zm.mud.core.protocol.iac.handler;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.session.MudSession;
import zm.mud.utils.HexUtil;


/**
 * 作为默认操作，回复拒绝
 */

@Component(IACConsts.IAC_HANDLER_COMMON)
public class IACHandler_Common extends AbsIACHandler{
    private static final Logger logger = LogManager.getLogger(IACHandler_Common.class);


    @Autowired
    private HexUtil hexUtil;
    @Override
    protected List<byte[]> handle_WILL(MudSession session,byte[] iacCommand) {
        return this.handle_DO(session,iacCommand);
    }


    @Override
    protected List<byte[]> handle_DO(MudSession session,byte[] iacCommand) {
        int opsCode = iacCommand[2] & 0xFF;

        if( IACConsts.enabledOpsSet.contains(opsCode)){
            return this.doEnable(iacCommand);
        }else{
            return this.doReject(iacCommand);
        }
    }

    private List<byte[]> doEnable(byte[] iacCommand){
        int commandCode = iacCommand[1] & 0xFF;
         if( !IACConsts.enableCmdMap.containsKey(commandCode)){
            logger.error("Unsupport cmd:" + Arrays.toString(hexUtil.toHex(iacCommand)));
            return null;  
        }
        byte ret[] = new byte[3];
        ret[0] = iacCommand[0];
        ret[1] = IACConsts.enableCmdMap.get(commandCode).byteValue();
        ret[2] = iacCommand[2];

        return Arrays.asList(ret);
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
