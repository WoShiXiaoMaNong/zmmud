package zm.mud.core.automation.action;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.core.automation.script.lua.LuaService;
import zm.mud.core.automation.trigger.Trigger;
import zm.mud.core.automation.trigger.cfg.MatchResult;
import zm.mud.core.session.MudSession;

@Component("ACTION_LuaScriptAction")
@Scope("prototype")
public class LuaScriptAction implements IAction {
    private static final Logger logger = LogManager.getLogger(LuaScriptAction.class);
    

    @Autowired
    private LuaService luaService;

    // 这里的 expression 用来存放要启动的 Lua 脚本路径或名称
    private String luaScriptPath;

    @Override
    public void setExpression(String expression) {
        this.luaScriptPath = expression;
    }

    @Override
    public String getExpression() {
        return this.luaScriptPath;
    }

    @Override
    public void execute(MudSession session,Trigger trigger, MatchResult ret) {
        String script = this.getExpression();
        if (script == null || script.trim().isEmpty()) {
            logger.error("Lua 脚本路径为空，无法执行！来自触发器：" + trigger.getTriggerName());
            return;
        }
        try {
            if (luaService == null) {
                logger.error("未找到 LuaService 实例，无法启动 Lua 脚本！");
                return;
            }

            // 2. 调用 Lua 引擎执行脚本，并将当前的 trigger 和正则匹配结果 ret 传进去
            logger.debug("开始启动 Lua 脚本: {}, 触发器: {}", script, trigger.getTriggerName());
            luaService.runScript(script, trigger, ret);

        } catch (Exception e) {
            logger.error("启动 Lua 脚本失败: " + script, e);
        }
    }
}