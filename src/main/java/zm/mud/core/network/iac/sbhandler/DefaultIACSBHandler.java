package zm.mud.core.network.iac.sbhandler;


import org.springframework.stereotype.Component;

import zm.mud.core.network.inbound.consts.IACConsts;

@Component(IACConsts.IAC_SB_HANDLER_DEFAULT)
public class DefaultIACSBHandler implements IIACSBCommandHandler{

    @Override
    public byte[] handle(byte[] iacSubCommand) {
        return null;
    }
    
}
