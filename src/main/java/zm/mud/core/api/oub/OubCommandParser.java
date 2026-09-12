package zm.mud.core.api.oub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import zm.mud.core.api.oub.cmd.FunctionalOubCommand;
import zm.mud.core.api.oub.cmd.NormalOubCommand;
import zm.mud.core.session.MudSession;


@Component 
public class OubCommandParser {
    public static final String SPLIT_CHAR = ";";
    

    /**
     * <pre>
     *  以英文输入法下的分号";"作为分割的命令字符串，例如
     *  1. e;#2 w  : 连续执行 一次e 和 2次w
     *  2. e;#wa 3000;w : 执行e后，停顿3000毫秒后，再执行一次w
     * </pre>
     * @param oubMsg
     * @return
     */
    public List<IOubCommand> parse(MudSession session,String oubMsg){
        if( oubMsg == null || oubMsg.trim().length() == 0){
            return Collections.emptyList();
        }
        List<IOubCommand> oubCommands = new ArrayList<>();
        String[] commandStrs = oubMsg.split(SPLIT_CHAR);
        for(String commandStr : commandStrs){
            if( IOubCommand.isFunctionCommand(commandStr)){
                oubCommands.add(new FunctionalOubCommand(session,commandStr));     
            }else{
                oubCommands.add(new NormalOubCommand(session,commandStr));
            }
            
        }


        return oubCommands;
    }


}
