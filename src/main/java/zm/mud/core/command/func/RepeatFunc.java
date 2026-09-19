package zm.mud.core.command.func;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.command.OubCommandParser;
import zm.mud.core.session.MudSession;

@Component(value = IFuncCommand.FUNC_CMD_SPRING_BEAN_PREFIX + "Repeat")
@Scope("prototype")
public class RepeatFunc implements IFuncCommand {

    private FuncInfo funcInfo;

    @Autowired 
    private OubCommandParser parser;

    public RepeatFunc(FuncInfo funcInfo) {
        this.funcInfo = funcInfo;
    }

    @Override
    public List<IOubCommand> doFunc(MudSession session,IOubCommand cmd) {
        List<IOubCommand> msgs = new ArrayList<>();
        int count = Integer.parseInt(funcInfo.getFuncCode());
        String realCmd = String.join(" ", funcInfo.getParams()); // 把切碎的参数重新用空格拼起来

        for (int i = 0; i < count; i++) {
            msgs.addAll(parser.parse(session, realCmd));
            msgs.addAll(parser.parse(session,"#wa 1000")); //避免输出太快，插入一个1秒延迟
        }
        if(count > 0){
            msgs.remove(msgs.size() - 1); // 移除最后一个延迟
        }
        msgs.forEach((m->m.setRevertMode(cmd.revertMode())));

        return msgs;
    }

}
