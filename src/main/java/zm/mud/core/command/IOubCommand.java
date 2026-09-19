package zm.mud.core.command;


public interface IOubCommand {
    public static final String FUNCTION_CMD_PREFIX = "#";
    
    void exec();

    String getCommandStr();
    /**
     * 是否需要反模式，如果是反模式：
     * 1. 输入 w 则执行 e
     * 2. wa 1500 还是执行 1500
     * @return
     */
    boolean revertMode();
    void setRevertMode(boolean isRevertMode);
    
    /**
     * 只有原始从命令行输入的才是true
     * 由原始指令派生出来的为false，
     * 例如：
     * 输入 #3 w 后，由repeat生成了 3个 w的 NormalOubCommand.
     * 其中 #3 w就是originalCmd， 而派生出来的这3个Command就不是originalCmd
     * @return
     */
    boolean isOiriginalCmd();
    void setOriginalCmd(boolean isOriginal);
    


    public static boolean isFunctionCommand(String commandStr){
        if( commandStr == null || commandStr.trim().length() == 0){
            return false;
        }
        return commandStr.trim().startsWith(FUNCTION_CMD_PREFIX);
    
    }
}
