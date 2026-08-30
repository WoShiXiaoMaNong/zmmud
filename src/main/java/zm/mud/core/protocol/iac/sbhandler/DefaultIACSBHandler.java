package zm.mud.core.protocol.iac.sbhandler;


import java.util.List;

import org.springframework.stereotype.Component;

import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.session.MudSession;

@Component(IACConsts.IAC_SB_HANDLER_DEFAULT)
public class DefaultIACSBHandler implements IIACSBCommandHandler{

    @Override
    public List<byte[]> handle(MudSession session,byte[] iacSubCommand) {
        return null;
    }
    
}
