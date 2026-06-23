package zm.mud.core.network.iac.sbhandler;

public interface IIACSBCommandHandler {
    
    byte[] handle(byte[] iacSubCommand);
}

