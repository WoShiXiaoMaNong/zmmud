package zm.mud.core.protocol.iac.sbhandler;


import java.util.List;

import org.springframework.stereotype.Component;

import zm.mud.core.protocol.iac.consts.IACConsts;

@Component(IACConsts.IAC_SB_HANDLER_DEFAULT)
public class DefaultIACSBHandler implements IIACSBCommandHandler{

    @Override
    public List<byte[]> handle(byte[] iacSubCommand) {
        return null;
    }
    
}
