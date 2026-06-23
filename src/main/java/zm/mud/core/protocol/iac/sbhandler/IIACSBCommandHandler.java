package zm.mud.core.protocol.iac.sbhandler;

public interface IIACSBCommandHandler {
    
    byte[] handle(byte[] iacSubCommand);
}

