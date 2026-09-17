-- scripts/hp_handler.lua

-- 接收 Java 传过来的参数（... 代表入参列表）
local actionContext, session = ...
session:printToUI("定时器测试：climb tree")
Sys:tick(session,"l",5000)
