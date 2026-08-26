package zm.mud.core.protocol.iac.sbhandler;

import java.util.List;

public interface IIACSBCommandHandler {
    
     List<byte[]> handle(byte[] iacSubCommand);
}

