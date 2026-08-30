-- scripts/hp_handler.lua

-- 接收 Java 传过来的参数（... 代表入参列表）
local trigger, ret = ...

-- 打印调试信息，确认收到了 Java 对象
--print("Lua 脚本启动成功！触发器名称: " .. trigger:getTriggerName())

-- 调用 MatchResult 的 getMatchedRet() 获取抓取到的 6 组数字 List
local rr = ret:getMatchedRet()
-- 1. 获取 Java List 的大小
local size = rr:size()
--print("数组总长度: " .. size)

local ssinfo = ""

-- 2. 循环输出所有元素 (从 0 开始，到 size - 1 结束)
for i = 0, size - 1 do
    -- 调用 Java List 的 get 方法
    local value = rr:get(i)
    -- print(i .. " : " .. tostring(value))
    ssinfo = ssinfo .. tostring(value) .. ","
end
LuaApi:sendMsg(ssinfo .. "aaa")
local cmd = "id"
--LuaApi:sendCommand(cmd .. " ")
-- 你可以在这里写你核心的 MUD 机器人逻辑（比如血量过低自动吃药等）
-- if tonumber(num1) < 100 then
--     print("血量过低，执行吃药！")
-- end
