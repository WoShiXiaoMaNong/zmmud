package zm.mud.core.command.cmd;

import java.util.List;

import zm.mud.core.command.IOubCommand;
import zm.mud.core.session.MudSession;

public abstract class AbsOubCommand implements IOubCommand {

    private MudSession session;
    private String originCommandStr;

    private boolean revertMode;
    private boolean isOirigalCmd;

    public AbsOubCommand(MudSession session, String originCommandStr) {
        this.session = session;
        this.originCommandStr = originCommandStr;
        this.isOirigalCmd = true;
        this.revertMode = false;
    }

    @Override
    public final void exec() {
        List<IOubCommand> transferedMsgs = this.transferCommand(originCommandStr);
        if (transferedMsgs == null || transferedMsgs.isEmpty()) {
            return;
        }

        // 特别注意，这里是把转换过的指令插入到 待发送队列的头部
        // 因为这一堆转换过的指令是当前待发送队列头部这个指令转换而来的，
        // 也就是用来替换当前的队列头的指令
        session.pushCommand(transferedMsgs);

    }

    /**
     * 当返回消息是null 或者 空字符串时，不会触发发送操作
     * 
     * @param originCommandStr
     * @return
     */
    protected abstract List<IOubCommand> transferCommand(String originCommandStr);

    @Override
    public String getCommandStr() {
        return this.originCommandStr;
    }

    public MudSession getSession() {
        return this.session;
    }

    @Override
    public boolean revertMode() {
        return this.revertMode;
    }

    @Override
    public void setRevertMode(boolean isRevertMode) {
        this.revertMode = isRevertMode;
    }

    @Override
    public boolean isOiriginalCmd() {
        return this.isOirigalCmd;
    }

    @Override
    public void setOriginalCmd(boolean isOriginal) {
        this.isOirigalCmd = isOriginal;
    }
    
}
