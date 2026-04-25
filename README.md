# ZmMud - MUD Client (Java)

## 📌 项目简介

**ZmMud** 是一个基于 Java 实现的 MUD（Multi-User Dungeon）客户端项目，当前处于早期开发阶段。

项目目标：

* 构建一个结构清晰、可扩展的 MUD 客户端
* 支持 Telnet 协议（包含 IAC 控制指令处理）
* 实现完整的消息收发、解析、处理流程
* 为后续功能（触发器、自动化、UI 等）打基础

---

## 🏗️ 项目结构

```
zm.mud
├─ ZmMud.java                # 程序入口
├─ cfg                       # 配置相关（YAML支持）
│  ├─ YamlConfig.java
│  └─ YamlPropertySourceFactory.java
│
├─ client                    # 客户端核心
│  └─ MudClient.java
│
└─ network                   # 网络模块（核心）
   ├─ ConnectionManager.java
   │
   ├─ inbound                # 入站数据处理
   │  ├─ message             # 入站消息定义
   │  ├─ processor           # 入站消息处理器
   │  └─ reader              # 字节流解析器
   │
   ├─ outbound               # 出站数据处理
   │  ├─ message             # 出站消息定义
   │  └─ processor           # 出站处理器
   │
   ├─ queue                  # 队列系统（线程间通信）
   │
   ├─ threads                # 多线程处理模块
   │
   └─ utils                  # 工具类
```

---

## 🔄 数据处理流程

### 入站（服务器 → 客户端）

```
Socket
  ↓
InboundByteDispatcherThread
  ↓
IAC / 普通数据分流
  ↓
Message Reader（解析）
  ↓
Message Queue
  ↓
Processor（处理）
```

---

### 出站（客户端 → 服务器）

```
用户输入 / 系统生成
  ↓
Outbound Message
  ↓
Outbound Queue
  ↓
Send Processor
  ↓
Socket
```

---

## 🧩 核心模块说明

### 1. ConnectionManager

负责：

* Socket 连接建立
* 输入输出流管理
* 生命周期控制

---

### 2. Reader（解析层）

| 类名                 | 作用                |
| ------------------ | ----------------- |
| `IacInbMsgReader`  | 解析 Telnet IAC 控制流 |
| `MudGameMsgReader` | 解析普通游戏文本          |
| `InbMessageReader` | 统一解析入口            |

---

### 3. Message（消息模型）

入站消息：

* `NormalInbMsg`：普通文本
* `IACConfirmInbMsg`：IAC 协议确认
* `ShutdownInbProcessMessage`：系统控制消息

出站消息：

* `NormalOutboundMsg`

---

### 4. Processor（处理器）

| 类名                    | 作用           |
| --------------------- | ------------ |
| `IACConfirmProcessor` | 处理 Telnet 协议 |
| `PrintProcessor`      | 输出文本         |
| `OubSendProcessor`    | 发送数据到服务器     |

---

### 5. Queue（队列）

用于线程之间解耦：

* 字节队列
* 消息队列
* 出站队列

---

### 6. Threads（线程模型）

当前使用多线程流水线处理：

* 字节分发线程
* IAC 收集线程
* MUD 消息收集线程
* 消息处理线程
* 出站处理线程

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

或直接运行：

```java
public static void main(String[] args) {
    ApplicationContext context =
        new AnnotationConfigApplicationContext("zm.mud");

    MudClient client = context.getBean(MudClient.class);
    client.connect();
}
```

---

## 📅 当前进度

* [x] 基础项目结构搭建
* [x] 网络连接能力
* [x] 入站/出站分层
* [x] IAC 协议初步支持
* [x] 多线程处理模型（初版）
* [ ] 线程模型优化（进行中）
* [ ] 消息模型优化以及收敛
* [ ] Trigger / Alias 系统
* [ ] 人物信息 系统
* [ ] 地图房间绘制
* [ ] UI 支持
* [ ] ANSI 渲染

---

## 🧠 设计理念

* 分层清晰（Reader / Message / Processor）
* 解耦（队列 + 多线程）
* 可扩展（协议与业务分离）

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

ZmMud 项目由个人开发，目标是构建一个高可扩展的 MUD 客户端框架。

---

## 📄 License

暂未定义（建议后续补充 MIT / Apache 2.0）
