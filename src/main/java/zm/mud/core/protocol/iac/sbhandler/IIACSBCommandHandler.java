package zm.mud.core.protocol.iac.sbhandler;

import java.util.List;

import zm.mud.core.session.MudSession;

public interface IIACSBCommandHandler {
    
     List<byte[]> handle(MudSession session,byte[] iacSubCommand);
}

