package zm.mud.core.network.inbound.reader;

import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import zm.mud.core.network.inbound.message.InbMsg;
import zm.mud.core.network.queue.InbMsgQueue;
import zm.mud.core.protocol.iac.consts.IACConsts;
import zm.mud.core.session.MudSession;

/**
 * 原生 Prototype 作用域
 * InbMsgReader
 */
@Service
@Scope("prototype") 
public class InbMsgReader {
     private static final Logger logger = LogManager.getLogger(InbMsgReader.class);
    
     enum InbReaderState {
          NOT_STARTED,
          NORMAL_READING,
          NORMAL_READING_CR, 
          NORMAL_END,
          IAC_READING,
          IAC_COMMAND_WITH_OPTION,
          IAC_COMMAND_WITHOUT_OPTION,
          IAC_SUBNEGOTIATION,       // 正在读取子协商数据 (SB ...)
          IAC_SUBNEGOTIATION_IAC,   // 在子协商中读到了 IAC，等待紧跟的 SE
          IAC_END
     }

     private int[] buf;
     private int currentIndex;
     private InbReaderState state;
     private int maxLength;

     @Autowired
     private InbMsgQueue inbMsgQueue;

     public InbMsgReader() {
          this.clear();
          this.state = InbReaderState.NOT_STARTED;
     }

     @Value("${mud.inbound.reader.buffer.maxLength:1024}")
     public void setMaxLength(int maxLength) {
          this.maxLength = maxLength;
          this.buf = new int[maxLength];
     }

     public synchronized void handleByte(MudSession session, int currentByte, Charset c) {
          OversizeCallback oversizeCallback = () -> {
               logger.warn("Message exceeds max length of " + maxLength + " bytes. Processing current buffer as a message.");
               this.procesEnd(session, c);
          };

          // ⚡ 修复拦截逻辑：只有在【非子协商】状态下，突然出现的 IAC 才能作为普通文本的强行结束标志
          if (currentByte == IACConsts.IAC && state != InbReaderState.IAC_SUBNEGOTIATION && state != InbReaderState.IAC_SUBNEGOTIATION_IAC) {
               if (state == InbReaderState.NORMAL_READING || state == InbReaderState.NORMAL_READING_CR) {
                    state = InbReaderState.NORMAL_END;
                    this.procesEnd(session, c);
               }
               state = InbReaderState.IAC_READING;
               this.add(currentByte, oversizeCallback);
               return;
          }

          switch (state) {
               case NOT_STARTED:
                    if (currentByte == IACConsts.IAC) {
                         state = InbReaderState.IAC_READING;
                    } else {
                         state = InbReaderState.NORMAL_READING;
                    }
                    this.add(currentByte, oversizeCallback);
                    break;

               case NORMAL_READING:
                    if (currentByte == '\r') {
                         state = InbReaderState.NORMAL_READING_CR;
                    } else {
                         this.add(currentByte, oversizeCallback);
                    }
                    break;

               case NORMAL_READING_CR:
                    if (currentByte == '\n') {
                         state = InbReaderState.NORMAL_END;
                    } else {
                         this.add(currentByte, oversizeCallback);
                         state = InbReaderState.NORMAL_READING;
                    }
                    break;

               case IAC_READING:
                    if (currentByte == IACConsts.CMD_SB) { // 读到 SB (Subnegotiation Begin)
                         state = InbReaderState.IAC_SUBNEGOTIATION;
                         this.add(currentByte, oversizeCallback);
                    } else {
                         if (IACConsts.NON_OPTION_COMMANDS.contains(currentByte)) {
                              state = InbReaderState.IAC_END; 
                         } else {
                              state = InbReaderState.IAC_COMMAND_WITH_OPTION;
                         }
                         this.add(currentByte, oversizeCallback);
                    }
                    break;

               case IAC_SUBNEGOTIATION:
                    if (currentByte == IACConsts.IAC) { // 在子协商中读到 IAC，说明可能要结束了
                         state = InbReaderState.IAC_SUBNEGOTIATION_IAC;
                    }
                    // 不管是不是 IAC，子协商内的数据都要先放进缓冲区（包含结尾的 IAC 和 SE）
                    this.add(currentByte, oversizeCallback);
                    break;

               case IAC_SUBNEGOTIATION_IAC:
                    if (currentByte == IACConsts.CMD_SE) { // 紧跟 IAC 后面读到了 SE，子协商正式合法结束
                         this.add(currentByte, oversizeCallback);
                         state = InbReaderState.IAC_END;
                    } else if (currentByte == IACConsts.IAC) { // Telnet 协议中的 IAC 转移 (两个连续 IAC 代表传输普通字节 255)
                         this.add(currentByte, oversizeCallback);
                         state = InbReaderState.IAC_SUBNEGOTIATION; // 回到普通子协商状态
                    } else {
                         // 其他特殊情况，当做普通子协商内容处理
                         this.add(currentByte, oversizeCallback);
                         state = InbReaderState.IAC_SUBNEGOTIATION;
                    }
                    break;

               case IAC_COMMAND_WITH_OPTION:
                    this.add(currentByte, oversizeCallback);
                    state = InbReaderState.IAC_END; 
                    break;

               case NORMAL_END:
                    break;

               default:
                    logger.warn("Invalid reader state: " + state);
                    state = InbReaderState.NORMAL_READING; 
          }

          if (state == InbReaderState.NORMAL_END || state == InbReaderState.IAC_END) {
               this.procesEnd(session, c);
          }
     }

     private void procesEnd(MudSession session, Charset c) {
          byte[] bytes = new byte[this.currentIndex];
          for (int i = 0; i < this.currentIndex; i++) {
               bytes[i] = (byte) buf[i];
          }
          
          String msgContent = new String(bytes, c);
          InbMsg msg = null;
          if (state == InbReaderState.IAC_END) {
               msg = InbMsg.buildIACConfirmMsg(session, bytes);
          } else {
               msg = InbMsg.build(session, msgContent);
          }
          inbMsgQueue.put(session, msg);
          this.clear();
          state = InbReaderState.NOT_STARTED;
     }

     private void add(int currentByte, OversizeCallback oversizeCallback) {
          if (this.currentIndex >= maxLength) {
               oversizeCallback.handle();
               this.clear();
          }
          this.buf[currentIndex++] = currentByte;
     }

     private void clear() {
          this.currentIndex = 0;
     }

     @FunctionalInterface
     interface OversizeCallback {
          void handle();
     }
}
