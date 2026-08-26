package zm.mud.core.trigger.service;

import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.core.api.ILuaApi;

@Service
public class LuaService {
    

    @Autowired
    private List<ILuaApi> luaApi;

    // 创建一个全局的 Lua 运行环境
    private final Globals globals = JsePlatform.standardGlobals();


    /**
     * 执行指定的 Lua 脚本，并传入 Java 对象参数
     */
    public void runScript(String scriptPath, Object trigger, Object matchResult) {

        this.registerLuaApi();

        // 1. 加载 Lua 脚本文件
        LuaValue chunk = globals.loadfile(scriptPath);
        
        // 2. 将 Java 的对象转换为 Lua 能够识别的变量 (Coerce 转换)
        LuaValue luaTrigger = CoerceJavaToLua.coerce(trigger);
        LuaValue luaRet = CoerceJavaToLua.coerce(matchResult);
        
        // 3. 执行 Lua 脚本，并将两个对象作为参数传入
        chunk.call(luaTrigger, luaRet);
    }

    private void registerLuaApi(){
        if( this.luaApi == null){
            return;
        }

        for(ILuaApi api: this.luaApi){
            String apiClassName = api.getClass().getSimpleName();

            if(  globals.get(apiClassName).isnil()){
                LuaValue luaService = CoerceJavaToLua.coerce(api);
                globals.set(apiClassName, luaService);
            }
        }
    }
}