# ZmMud - MUD Client (Java)

## 📌 项目简介

**ZmMud** 是一个基于 Java 实现的 MUD（Multi-User Dungeon）客户端项目，当前处于早期开发阶段。

项目目标：

* 构建一个结构清晰、可扩展的 MUD 客户端
* 支持 Telnet 协议（包含 IAC 控制指令处理）
* 实现完整的消息收发、解析、处理流程
* 为后续功能（触发器、自动化、UI 等）打基础

---

## 触发器说明
* [点击查看 触发器说明](Trigger.md)

## 🏗️ 项目结构

```
└─ zm
   └─ mud
      ├─ ZmMud.java                  # 程序主入口（Spring Boot 引导类）
      │  
      ├─ core                        # 🛠️ 核心引擎层
      │  ├─ IShutdownFunc.java       # 关闭钩子接口
      │  ├─ ShutdownWorld.java       # 游戏世界关闭逻辑实现
      │  │  
      │  ├─ api                      # 内部/外部服务接口
      │  │  ├─ ClientService.java    # 客户端管理服务
      │  │  ├─ ILuaApi.java          # Lua 脚本暴露的 API 接口
      │  │  ├─ InbMsgService.java    # 入站消息服务
      │  │  ├─ LuaApi.java           # Lua API 具体实现
      │  │  └─ OubMsgService.java    # 出站消息服务
      │  │  
      │  ├─ cfg                      # 配置加载模块
      │  │  ├─ ApplicationConfig.java
      │  │  ├─ YamlConfig.java       # YAML 配置解析
      │  │  └─ YamlPropertySourceFactory.java
      │  │  
      │  ├─ client                   # 客户端核心实例
      │  │  └─ MudClient.java        # MUD 核心连接会话管理
      │  │  
      │  ├─ network                  # 🌐 网络通信模块（生产/消费者模型）
      │  │  ├─ ConnectionManager.java# 网络连接管理器
      │  │  │  
      │  │  ├─ inbound               # 入站（接收）数据处理
      │  │  │  ├─ message            # 入站消息实体定义（如：普通消息、IAC确认）
      │  │  │  ├─ processor          # 入站消息处理器（包含触发器、普通消息分发）
      │  │  │  └─ reader             # 原始字节流解析器
      │  │  │  
      │  │  ├─ outbound              # 出站（发送）数据处理
      │  │  │  ├─ message            # 出站消息实体定义
      │  │  │  ├─ processor          # 出站消息处理器（包含发送控制、发送触发）
      │  │  │  └─ sender             # 消息底层发送器
      │  │  │  
      │  │  ├─ queue                 # 队列系统（数据缓冲区）
      │  │  │  └─ *Queue.java        # 包含入站/出站阻塞队列，用于线程间异步通信
      │  │  │  
      │  │  └─ threads               # 网络专用线程
      │  │     └─ *Thread.java       # 包含读线程、入站/出站消息独立处理线程
      │  │  
      │  ├─ protocol                 # 📜 协议解析层
      │  │  └─ iac                   # Telnet IAC (Is An Command) 机制处理
      │  │     ├─ consts             # Telnet 协议常量定义
      │  │     ├─ handler            # 通用 IAC 命令处理器
      │  │     └─ sbhandler          # IAC SB 子协商处理器（用于特殊指令/编码）
      │  │  
      │  ├─ thread                   # 🧵 线程池管理
      │  │  └─ ZmmudThreadPools.java # 系统全局线程池配置
      │  │  
      │  └─ trigger                  # ⚡ 触发器引擎（核心自动化模块）
      │     ├─ Trigger*.java         # 触发器定义、加载、注册与工厂模式实现
      │     ├─ action                # 触发动作（发送命令、执行 Lua 脚本等）
      │     ├─ cfg                   # 触发器配置实体及匹配结果封装
      │     ├─ matcher               # 文本匹配算法（正则、全包含、开头/结尾匹配）
      │     └─ service               # 触发器、匹配及 Lua 执行的底层服务
      │  
      ├─ pkuxkx                      # 🎯 游戏定制层（针对知名MUD“北大侠客行”）
      │  └─ trigger                  
      │     └─ action                # 定制化触发动作（如：Fullme验证码显示、血量提示解析）
      │  
      ├─ ui                          # 🖥️ 界面展示层
      │  ├─ ZmMudUI.java             # UI 模块主入口
      │  ├─ cfg                      # UI 全局配置与主题类型定义
      │  ├─ component                # 自定义 Swing 控件（输入框、主屏幕、文本域）
      │  ├─ processor                # 文本渲染处理器（将游戏文本输出至屏幕）
      │  ├─ theme                    # 界面主题管理（支持 Basic、Dark、Light 主题）
      │  └─ util                     # 颜色解析工具（将 MUD 的 ANSI 颜色码转为 UI 样式）
      │  
      └─ utils                       # 🛠️ 通用工具层
         └─ *Util.java               # 包含流关闭、字体、十六进制、HTTP及SpringBean获取工具

```

---

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

## 🚀 运行方式

```bash
# 编译
mvn clean install

# 运行
java -jar zm-mud.jar
```

或直接运行：ZmMud.java中的main方法

---

## 📅 当前进度

* [x] 基础项目结构搭建
* [x] 网络连接能力
* [x] 入站/出站分层
* [x] IAC 协议初步支持
* [x] 多线程处理模型（初版）
* [x] 线程模型优化
* [x] 消息模型优化以及收敛
* [x] Trigger（进行中）
* [ ] Alias 系统
* [ ] 人物信息 系统
* [ ] 地图房间绘制
* [x] UI 支持
* [x] ANSI 渲染

---

## 相关IAC的支持开关
* [x]         FF FD 18    IAC DO TERMINAL-TYPE (服务器要求通报终端类型)
* [ ]         FF FB 5A    IAC WILL START-TLS (服务器声明支持安全传输 TLS 加密)
* [ ]         FF FD 1F    IAC DO NAWS (服务器要求通报窗口大小)
* [ ]         FF FB C9    IAC WILL SUPDUP (服务器声明它支持 SUPDUP 协议，C9即201)
* [ ]         FF FB 56    IAC WILL TN3270E (服务器声明它支持 TN3270 增强模式)
* [ ]         FF FB 46    IAC WILL VT320-REGIME (服务器声明支持 VT320 模式)
* [ ]         FF FD 27    IAC DO NEW-ENVIRONMENT (服务器要求协商环境变量)
* [ ]         FF FB 2A    IAC WILL CHARSET (服务器声明支持字符集协商)


---

## ⚠️ 注意事项

当前版本：

* 属于早期开发阶段
* 线程模型仍在优化
* API 可能频繁变动

---

## 📌 后续规划

* 精简线程模型
* 引入更高效的 IO 方案（如 NIO）
* 实现脚本系统（自动化）
* 支持插件扩展
* 构建图形界面（Swing / JavaFX / Web）

---

## 👨‍💻 作者

zhongming139@126.com

---

## 📄 License

暂未定义（建议后续补充 MIT / Apache 2.0）
