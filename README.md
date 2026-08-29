# ZmMud - MUD Client (Java)

## 项目简介

**ZmMud** 是一个基于 Java 实现的 MUD游戏客户端项目（目前正对[北大侠客行](https://www.pkuxkx.net/#/)进行开发），当前处于早期开发阶段。


项目目标：

* 构建一个结构清晰、可扩展的 MUD 客户端
* 支持 Telnet 协议（包含 IAC 控制指令处理）
* 实现完整的消息收发、解析、处理流程
* 为后续功能（触发器、自动化、UI 等）打基础

---
## 作者
zhongming139@126.com

---

## 北大侠客行相关说明
* [点击查看 北大侠客行相关说明](Pkuxkx.md)

## 触发器说明
* [点击查看 触发器说明](Trigger.md)

## 界面截图
<img width="1012" height="1256" alt="pkuxkx" src="https://github.com/user-attachments/assets/c10d49c7-4bde-466f-a6fc-06821c4b58cb" />

## ⚙️ 配置说明

项目支持 YAML 配置：

```yaml
mud:
  server:
    host: xxx.xxx.xxx.xxx
    port: 4000
```

通过：

```java
@Value("${mud.server.port}")
```

进行注入。

---

## 运行方式

```bash
# 编译
mvn clean install

# 运行
java -jar zm-mud.jar
```

或直接运行：ZmMud.java中的main方法

---

## 当前进度

* [x] 基础项目结构搭建
* [x] 网络连接能力
* [x] 入站/出站分层
* [x] IAC 协议初步支持
* [x] 多线程处理模型（初版）
* [x] 线程模型优化
* [x] 消息模型优化以及收敛
* [x] Trigger
* [x] Alias 系统（进行中）
* [x] Timer 系统（进行中）
* [ ] 地图房间绘制
* [x] UI 支持
* [x] UI 人物信息（基于GMCP）
* [x] ANSI 渲染

---


## 注意事项

当前版本：

* 属于早期开发阶段
* 线程模型仍在优化
* API 可能频繁变动

---

## 项目结构简要说明

```
└─zm
    └─mud
        │  ZmMud.java                          # 项目入口，MUD客户端启动类
        │  
        ├─core                                 # 核心逻辑模块
        │  │  IShutdownFunc.java               # 停机/关闭功能接口
        │  │  ShutdownWorld.java               # 游戏世界关闭逻辑实现
        │  │  
        │  ├─api                               # 外部/脚本调用接口
        │  │      ClientService.java           # 客户端核心服务接口
        │  │      InbMsgService.java           # 输入消息服务接口
        │  │      LuaApi.java                  # 提供给 Lua 脚本调用的 API 实现
        │  │      OubMsgService.java           # 输出消息服务接口
        │  │      
        │  ├─automation                        # 自动化模块（别名、触发器、脚本）
        │  │  ├─action                         # 自动化动作执行器
        │  │  │      IAction.java              # 动作通用接口
        │  │  │      LuaScriptAction.java      # Lua 脚本动作实现
        │  │  │      RegisterAction.java       # 动作注册器
        │  │  │      SendCommand.java          # 发送游戏命令动作
        │  │  │      
        │  │  ├─alias                          # 别名系统（快捷键/命令别名）
        │  │  │      Alias.java                # 别名实体类
        │  │  │      AliasLoader.java          # 别名加载器
        │  │  │      AliasService.java         # 别名管理服务
        │  │  │      
        │  │  ├─script                         # 脚本引擎生态
        │  │  │  └─lua
        │  │  │          ILuaApi.java          # Lua API 标准接口
        │  │  │          LuaService.java       # Lua 脚本执行与管理服务
        │  │  │          
        │  │  └─trigger                        # 触发器系统（文本匹配与响应）
        │  │      │  Trigger.java              # 触发器核心类
        │  │      │  TriggerFactory.java       # 触发器工厂
        │  │      │  TriggerLoader.java        # 触发器配置加载器
        │  │      │  TriggerRegister.java      # 触发器注册中心
        │  │      │  
        │  │      ├─cfg                        # 触发器配置项
        │  │      │      MatcherAndActionConfigEntry.java # 匹配与动作映射配置
        │  │      │      MatchResult.java      # 触发器匹配结果包装
        │  │      │      TriggerConfig.java    # 触发器全局配置
        │  │      │      TriggerConfigEntry.java # 单条触发器配置条目
        │  │      │      TriggerType.java      # 触发器类型枚举
        │  │      │      
        │  │      └─matcher                    # 触发器文本匹配算法
        │  │              EndWith.java         # 后缀匹配
        │  │              Equals.java          # 全字匹配
        │  │              IMatcher.java        # 匹配器通用接口
        │  │              Include.java         # 包含匹配
        │  │              RegexMatcher.java    # 正则表达式匹配
        │  │              StartWith.java       # 前缀匹配
        │  │              
        │  ├─cfg                               # 全局核心配置
        │  │      ApplicationConfig.java       # 系统应用配置
        │  │      YamlConfig.java              # YAML 配置文件解析
        │  │      YamlPropertySourceFactory.java # Spring 环境下的 YAML 工厂类
        │  │      
        │  ├─client                            # 客户端实例
        │  │      MudClient.java               # MUD 连接客户端主体
        │  │      
        │  ├─network                           # 网络通信模块
        │  │  │  ConnectionManager.java        # 网络连接管理器（Socket 管理）
        │  │  │  
        │  │  ├─inbound                        # 输入流处理（从服务器接收数据）
        │  │  │  ├─message                     # 输入消息实体
        │  │  │  │      IACConfirmInbMsg.java  # Telnet IAC 确认消息
        │  │  │  │      InbMsg.java            # 输入消息基类
        │  │  │  │      NormalInbMsg.java      # 普通文本输入消息
        │  │  │  │      
        │  │  │  ├─processor                   # 输入消息管道处理器
        │  │  │  │      IACConfirmProcessor.java # IAC 协议确认处理器
        │  │  │  │      IInbMsgProcessor.java  # 输入处理器接口
        │  │  │  │      InbTriggerProcessor.java # 输入触发器流处理器
        │  │  │  │      MsgHandlerProcessor.java # 消息分发处理器
        │  │  │  │      
        │  │  │  └─reader                      # 网络数据读取器
        │  │  │          InbMsgReader.java     # 输入流读取实现
        │  │  │          
        │  │  ├─outbound                       # 输出流处理（发送数据到服务器）
        │  │  │  ├─message                     # 输出消息实体
        │  │  │  │      NrmOubMsg.java         # 普通输出命令
        │  │  │  │      OubMsg.java            # 输出消息基类
        │  │  │  │      
        │  │  │  ├─processor                   # 输出消息管道处理器
        │  │  │  │      AliasProcessor.java    # 输出命令别名解析处理器
        │  │  │  │      IOubMsgProcessor.java  # 输出处理器接口
        │  │  │  │      OubSendProcessor.java  # 最终网络发送处理器
        │  │  │  │      OubTriggerProcessor.java # 输出命令触发处理器
        │  │  │  │      
        │  │  │  └─sender                      # 网络数据发送器
        │  │  │          OubMsgSender.java     # 输出流写入实现
        │  │  │          
        │  │  ├─queue                          # 消息缓冲队列
        │  │  │      InbMsgQueue.java          # 输入消息阻塞队列
        │  │  │      IZmmudQueue.java          # 队列通用接口
        │  │  │      OubMsgQueue.java          # 输出消息阻塞队列
        │  │  │          
        │  │  └─threads                        # 网络层专属线程
        │  │          InbMsgProcessThread.java # 输入消息异步处理线程
        │  │          InbReadThread.java       # 网络 Socket 循环读取线程
        │  │          IZmmudThread.java        # 项目线程基类接口
        │  │          OubMsgProcessThread.java # 输出消息异步处理线程
        │  │          ThreadPoolService.java   # 线程池管理服务
        │  │          
        │  ├─protocol                          # 标准网络协议支持
        │  │  ├─gmcp                           # GMCP (Generic Mud Communication Protocol)
        │  │  │      IGMCPOnMessage.java       # GMCP 消息到达回调接口
        │  │  │      
        │  │  └─iac                            # Telnet IAC (Interpret As Command) 协议
        │  │      ├─consts                     # Telnet 协议常量
        │  │      │      IACConsts.java        # 协议指令及协商代码常量
        │  │      │      
        │  │      ├─handler                    # IAC 主动指令协商处理
        │  │      │      AbsIACHandler.java    # IAC 处理器抽象基类
        │  │      │      IACHandler_C9.java    # GMCP 协议通道(0xC9)协商处理器
        │  │      │      IACHandler_Common.java # 通用 IAC 指令处理器
        │  │      │      IIACCommandHandler.java # IAC 处理器标准接口
        │  │      │      
        │  │      └─sbhandler                  # IAC SB (Subnegotiation) 子协商处理
        │  │              DefaultIACSBHandler.java # 默认子协商处理器
        │  │              IACSBHandler_18.java # 终端类型(0x18/TTYPE)子协商处理器
        │  │              IACSBHandler_C9.java # GMCP数据子协商处理器
        │  │              IIACSBCommandHandler.java # 子协商处理接口
        │  │              
        │  └─thread                            # 全局线程管理
        │          ZmmudThreadPools.java       # 全局业务线程池定义
        │          
        ├─pkuxkx                               # 北大侠客行 MUD 专属定制业务层
        │  ├─gmcp                              # 侠客行 GMCP 协议特定解析
        │  │  │  GMCPContext.java              # 侠客行 GMCP 数据上下文
        │  │  │  GMCPMsglisener.java           # 侠客行 GMCP 原始消息监听器
        │  │  │  
        │  │  └─channel                        # 侠客行各数据频道分发处理
        │  │      │  IGMCPMsgHandler.java      # 侠客行 GMCP 频道处理通用接口
        │  │      │  
        │  │      ├─message                    # 游戏内聊天/谣言/系统消息频道
        │  │      │      GMCPMessageMsgHandler.java # 消息频道解析器
        │  │      │      PkuxkxMessage.java    # 消息数据实体
        │  │      │      
        │  │      ├─move                       # 游戏内移动/房间/地图信息频道
        │  │      │      GMCPMoveMsgHandler.java # 移动频道解析器
        │  │      │      PkuxkxRoom.java       # 房间/环境数据实体
        │  │      │      
        │  │      └─status                     # 玩家角色状态/气血/精神频道
        │  │              GMCPStatusMsgHandler.java # 状态频道解析器
        │  │              
        │  └─trigger                           # 侠客行专属特化触发器动作
        │      └─action
        │              FullmeShowAction.java   # 针对 fullme 验证码弹窗或提示的专用处理动作
        │              
        ├─ui                                   # 图形界面模块 (基于 Java Swing / AWT)
        │  │  UiConfigLoader.java              # UI 布局及皮肤加载器
        │  │  ZmMudUI.java                     # 客户端主窗口主框架
        │  │  
        │  ├─cfg                               # UI 独立配置
        │  │      GlobleCfg.java               # 全局 UI 样式配置（字体、间距等）
        │  │      ThemeType.java               # 主题类型枚举
        │  │      
        │  ├─component                         # 自定义 UI 渲染组件
        │  │  │  ImageInfo.java                # 图片元数据封装
        │  │  │  MudInputField.java            # 自定义命令输入框组件
        │  │  │  MudMainScreen.java            # 游戏主输出屏幕组件
        │  │  │  MudScrollPane.java            # 带有自动滚动的面板组件
        │  │  │  MudTextAare.java              # 支持富文本/样式的游戏文本显示区域
        │  │  │  
        │  │  └─statusBar                      # 状态栏组件（展示血量、内力等）
        │  │          MudStatusBar.java        # 游戏底部状态栏主体
        │  │          StatusBarLabelInfo.java  # 状态栏标签信息格式化
        │  │          
        │  ├─processor                         # 界面渲染流处理器
        │  │      MsgPrintProcessor.java       # 将接收到的游戏消息排版打印到主屏幕的处理器
        │  │      
        │  ├─theme                             # UI 配色主题
        │  │      Basic.java                   # 经典 MUD 默认主题
        │  │      Dark.java                    # 暗黑模式主题
        │  │      ITheme.java                  # 主题通用规范接口
        │  │      Light.java                   # 明亮模式主题
        │  │      
        │  └─util                              # 界面渲染及着色工具类
        │          AnsiTextUtil.java           # ANSI 颜色转义字符过滤工具
        │          AnsiToStyleDocUtil.java     # 将带有颜色代码的 ANSI 文本转换为 Swing 文档样式控制器的工具
        │          ImageUtil.java              # 验证码/游戏图片处理工具
        │          
        └─utils                                # 基础底层工具包
                CloseUtil.java                 # IO 流与网络连接静默关闭工具
                FontUtil.java                  # 游戏等宽字体渲染与度量工具
                HexUtil.java                   # 16进制与字节数组转换工具（常用于调试协议）
                HttpUtil.java                  # HTTP 网络请求工具（用于远程资源或验证码外部识别）
                SpringBeanUtil.java            # Spring 上下文容器手动获取 Bean 的工具类

```
---


## 📄 License

暂未定义（建议后续补充 MIT / Apache 2.0）
