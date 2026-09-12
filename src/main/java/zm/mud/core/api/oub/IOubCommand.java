package zm.mud.core.api.oub;


public interface IOubCommand {
    public static final String FUNCTION_CMD_PREFIX = "#";
    
    void exec();

    String getCommandStr();

    public static boolean isFunctionCommand(String commandStr){
        if( commandStr == null || commandStr.trim().length() == 0){
            return false;
        }
        return commandStr.trim().startsWith(FUNCTION_CMD_PREFIX);
    
    }
}
