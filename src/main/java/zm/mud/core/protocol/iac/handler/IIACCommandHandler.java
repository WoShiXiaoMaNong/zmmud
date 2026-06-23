package zm.mud.core.protocol.iac.handler;

public interface IIACCommandHandler {
    
    byte[] handle(byte[] iacCommand);
}

