package zm.mud.core.trigger.service;

import java.util.List;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.aop.framework.AopProxyUtils;
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
    if(this.luaApi == null){
        return;
    }

    for(ILuaApi api : this.luaApi){
        // 1. 使用 AopProxyUtils 获取真实的原始类，避免获取到代理类的名字（如 LuaApi$$EnhancerBySpring...）
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(api);
        String apiClassName = targetClass.getSimpleName();

        if(globals.get(apiClassName).isnil()){
            // 2. 获取真实的原始对象，确保 Luaj 反射时能找到正确的方法和注入的 Service
            Object singletonTarget = AopProxyUtils.getSingletonTarget(api);
            if (singletonTarget == null) {
                singletonTarget = api; // 如果不是单例代理，降级使用原对象
            }
            
            LuaValue luaService = CoerceJavaToLua.coerce(singletonTarget);
            globals.set(apiClassName, luaService);
        }
    }
}
}