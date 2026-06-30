## 触发器框架通用配置说明书
该框架采用 JSON 格式定义，通过“匹配器（Matcher）”拦截进出站文本，并触发指定的“动作器（Action）”执行业务逻辑。
------------------------------
## 一、 根节点结构
配置文件根节点为一个 JSON 对象，包含唯一的核心数组 triggers：

* triggers (Array): 触发器对象列表。系统启动时会遍历此数组进行初始化。

------------------------------
## 二、 触发器通用属性 (Trigger Object)
每个触发器对象包含以下基础控制字段：

| 字段名 | 类型 | 可选值 | 说明 |
|---|---|---|---|
| name | String | 任意唯一字符串 | 触发器的唯一标识符（ID），作为唯一键用于动态管理或链式调用。 |
| type | String | "inbound", "outbound" | 数据流向： • "inbound"：监听接收自远端服务器的数据。 • "outbound"：监听用户发往远端服务器的数据（出站指令）。 |
| sync | Boolean | true ， false | 线程阻塞模式： • true：在主线程中阻塞式进行，后续的文本处理或指令发送会在此触发器执行完毕前被挂起。 • false：异步非阻塞模式，在后台独立运行。 |
| unique | Boolean | true ， false | 唯一性生命周期控制： • true：同一时间只能有一个该触发器被注册（以 name 为唯一键）。若系统中已存在同名激活的触发器，则拒绝重复注册。 • false：允许同时存在多个同名触发器。 |
| autoRegister | Boolean | true ， false | 启动注册机制： • true：系统启动的时候自动注册并开始监听，属于常驻或初始触发器。 • false：启动时不自动监听，默认处于休眠状态，需要由其他动作器动态唤醒。 |
| remainningCount | Integer | -1 或者 正整数 | 触发器存活次数： • -1：永久有效，只要符合匹配条件就会无限次触发，不自动注销。 • N (N>0)：可以被成功匹配几次。触发器每成功匹配并执行一次，计数减 1，减至 0 后该触发器自动从框架中注销销毁。 |
| matcher | Object | 匹配器配置对象 | 定义触发器在何种文本条件下被激活（详见第三节）。 |
| action | Object | 动作器配置对象 | 定义触发器激活后执行的具体业务逻辑（详见第四节）。 |

------------------------------
## 三、 匹配器组件 (matcher)
用于指定文本过滤规则。包含 type（匹配类型）和 expression（表达式）两个核心字段。

## 可用匹配类型 (type)

* Equals
* 语义：字符串完全匹配。
   * 说明：输入文本必须与 expression 完全一致（区分大小写）。
* StartWith
* 语义：头部匹配。
   * 说明：输入文本必须以 expression 指定的字符串开头。
* EndWith
* 语义：尾部匹配。
   * 说明：输入文本必须以 expression 指定的字符串结尾。
* Include
* 语义：包含匹配。
   * 说明：输入文本中只要含有 expression 字符串即可触发。
* Regex / RegexMatcher
* 语义：正则表达式匹配。
   * 匹配到的group，会按正则中的顺序，通过 MatchResult.matchedRet 传给Action
> 注意:所有匹配器的BeanID的格式都必须是： MATCHER_{matcher.type} 
> 例如，MATCHER_Equals,就可以在json配置文件中使用,如下:
```json
  "matcher":{
            "type":"Equals",
             "expression":"fullme"
    }
```
------------------------------
## 四、 动作器组件 (action)
用于定义触发成功后的行为。除通用的 type 和 expression 外，还支持通过 params 传递自定义参数。
## 可用动作类型 (type)

* RegisterAction
* 动作：动态激活触发器。
   * expression 含义：目标触发器的 name。
   * 业务场景：实现链式联动。拦截到 A 指令后，才临时注册开启 B 触发器去捕获接下来的特定返回结果。
* SendCommand
* 动作：指令回发。
   * expression 含义：要发送的文本指令。
   * 业务场景：实现自动响应。例如收到特定提示后，自动向服务器发送预设指令。
* LuaScriptAction
* 动作：调用外部脚本。
   * expression 含义：本地 .lua 脚本文件的绝对或相对路径。
   * params 含义：传入 Lua 脚本的自定义参数字典。【目前并没有使用】
   * 业务场景：处理高级复杂逻辑。系统会将匹配到的文本和正则分组变量传递给 Lua 脚本进行二次解析。
* FullmeShowAction (特定业务扩展)
    * 北侠特有的动作器。专用于拦截 URL 链接，并在客户端内嵌组件中直接弹窗渲染对应的验证码图片。
> 注意:所有动作其的BeanID的格式都必须是： ACTION_{matcher.type} 
> 例如，ACTION_LuaScriptAction,就可以在json配置文件中使用,如下:
```json
  "action":{
            "type":"LuaScriptAction",
            "expression":"C:\\aaa\\bbb\\hp_handler.lua"
    }
```
## 五、 关于Lua脚本


* Java中实现了ILuaApi.java 接口的所有Spring bean，都可以在lua中直接使用
    * 例如以下例子中的 LuaApi:sendMsg(ssinfo)
* Lua例子
```Lua
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

    -- java中实现了ILuaApi.java 接口的所有Spring bean，都可以在lua中直接使用
    LuaApi:sendMsg(ssinfo)

    -- 你可以在这里写你核心的 MUD 机器人逻辑（比如血量过低自动吃药等）
    -- if tonumber(num1) < 100 then
    --     print("血量过低，执行吃药！")
    -- end
```


* 配置样例
## 触发器配置说明 (Triggers Configuration)

本配置包含两组**链式触发器**（Outbound 发送流激活 Inbound 接收流）。配置在设计上实现了**“同步防漏”**与**“唯一防重”**：
1. **同步防漏 (`sync: true`)**: 外发触发器必须同步执行，确保在 MUD 服务器后续数据包返回前，对应的接收触发器已提前注册就绪。
2. **唯一防重 (`unique: true`)**: 所有触发器的 `unique` 均设为 `true`。保证同一时间，同名的触发器在系统中最多只有一个在监听，彻底避免因高频调用导致触发器堆积或重复执行。

### 1. Fullme 验证码显示组
*   **fullme (外发触发)**: 
    *   **关键属性**: `sync: true` (同步注册), `unique: true` (防重注册), `autoRegister: true` (全局常驻:启动时就自动注册，并长期监听)。
    *   **触发条件**: 精确匹配文本 `"fullme"`。
    *   **核心动作**: 立即同步注册 `fullme-url` 触发器。可无限次触发 (`-1`)。
*   **fullme-url (接收触发)**: 
    *   **关键属性**: `sync: false` (异步执行), `unique: true` (防重), `autoRegister: false` (由外发触发器动态激活)。
    *   **触发条件**: 匹配以 `http://pkuxkx.net` 开头的 URL。
    *   **核心动作**: 调用 `FullmeShowAction` 模块在客户端渲染并显示验证码图片。
    *   **生命周期**: 触发 `1` 次后自动销毁，防止历史链接干扰。

### 2. HPBrief 状态解析组
*   **hpbrief-oub-trigger (外发触发)**: 
    *   **关键属性**: `sync: true` (同步注册), `unique: true` (防重注册), `autoRegister: true` (全局常驻:启动时就自动注册，并长期监听)。
    *   **触发条件**: 精确匹配文本 `"hpbrief"`。
    *   **核心动作**: 立即同步注册 `hpbrief-inb-trigger` 触发器。可无限次触发 (`-1`)。
*   **hpbrief-inb-trigger (接收触发)**: 
    *   **关键属性**: `sync: false` (异步执行), `unique: true` (防重), `autoRegister: false` (由外发触发器动态激活)。
    *   **触发条件**: 正则匹配形如 `#数字,数字,数字,数字,数字,数字` 的血量数据流。
    *   **核心动作**: 调用本地 Lua 脚本 `hp_handler.lua` 进行数据解析与状态刷新。
    *   **生命周期**: 连续接收 `3` 行数据后自动销毁，防止过期监听（北侠的hpbrief，返回3行数据）。

```json
{
    "triggers":[
                {
                    "name":"fullme",
                    "sync":true,  
                    "unique":true,
                    "autoRegister":true,
                    "matcher":{
                        "type":"Equals",
                        "expression":"fullme"
                    },
                    "action":{
                        "type":"RegisterAction",
                        "expression":"fullme-url"
                    },
                    "type":"outbound",
                    "remainningCount":-1
                },
                {
                    "name":"fullme-url",
                    "sync":false,  
                    "unique":true,
                    "autoRegister":false,
                    "matcher":{
                        "type":"StartWith",
                        "expression":"http://fullme.pkuxkx.net/"
                    },
                    "action":{
                        "type":"FullmeShowAction",
                        "expression":"l",
                        "params":{
                            "baseUrl":"http://fullme.pkuxkx.net/"
                        }
                    },
                    "type":"inbound",
                    "remainningCount":1
                },
                {
                    "name":"hpbrief-oub-trigger",
                    "sync":true,  
                    "unique":true,
                    "autoRegister":true,
                    "matcher":{
                        "type":"Equals",
                        "expression":"hpbrief"
                    },
                    "action":{
                        "type":"RegisterAction",
                        "expression":"hpbrief-inb-trigger"
                    },
                    "type":"outbound",
                    "remainningCount":-1
                },
                {
                    "name":"hpbrief-inb-trigger",
                    "sync":false,  
                    "unique":true,
                    "autoRegister":false,
                    "matcher":{
                        "type":"Regex",
                        "expression":"^#(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+)$"
                    },
                    "action":{
                        "type":"LuaScriptAction",
                        "expression":"C:\\aaa\\bbb\\hp_handler.lua",
                        "params":{
                            "baseUrl":"test"
                        }
                    },
                    "type":"inbound",
                    "remainningCount":3
                }
    ]
}
```