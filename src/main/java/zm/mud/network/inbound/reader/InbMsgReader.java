package zm.mud.network.inbound.reader;

import java.nio.charset.Charset;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zm.mud.network.inbound.consts.IACConsts;
import zm.mud.network.inbound.message.InbMsg;
import zm.mud.network.queue.InbMsgQueue;

@Service
public class InbMsgReader {
     private static final Logger logger = LogManager.getLogger(InbMsgReader.class);
    
     enum InbReaderState {
          NOT_STARTED,
          NORMAL_READING,
          NORMAL_READING_CR, // after reading \r, waiting for \n
          NORMAL_READING_CRLF, // after reading \r\n, waiting for next byte to determine if it's normal or IAC
          NORMAL_END,
          IAC_READING,
          IAC_COMMAND_WITH_OPTION,
          IAC_COMMAND_WITHOUT_OPTION,
          IAC_OPTION,
          IAC_SUBNEGOTIATION, // SB
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
     public void  setMaxLength(int maxLength) {
          this.maxLength = maxLength;
          this.buf = new int[maxLength];
     }

     public synchronized void handleByte(int currentByte, Charset c) {
          OversizeCallback oversizeCallback = () -> {
               //logger.warn("Message exceeds max length of " + MAX_LENGTH + " bytes. Processing current buffer as a message.");
               this.procesEnd(c);
          };
          if (currentByte == IACConsts.IAC) {
               if(state == InbReaderState.NORMAL_READING){
                    state = InbReaderState.NORMAL_END;
                    this.procesEnd(c);
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
                         state = InbReaderState.NORMAL_READING_CRLF;
                         state = InbReaderState.NORMAL_END;
                    } else {
                         this.add(currentByte, oversizeCallback);
                         state = InbReaderState.NORMAL_READING;
                    }
                    break;
               case IAC_READING:
                    if (currentByte == IACConsts.CMD_SB) { // SB COMMAND
                         state = InbReaderState.IAC_SUBNEGOTIATION;
                    } else {
                         if (IACConsts.NON_OPTION_COMMANDS.contains(currentByte)) {
                              state = InbReaderState.IAC_COMMAND_WITHOUT_OPTION;
                              state = InbReaderState.IAC_END; // <<<<<< END
                         } else {
                              state = InbReaderState.IAC_COMMAND_WITH_OPTION;
                         }
                         this.add(currentByte, oversizeCallback);
                    }
                    break;
               case IAC_SUBNEGOTIATION:
                    if (currentByte == IACConsts.CMD_SE) { // SE
                         state = InbReaderState.IAC_END; // <<<<<< END
                    } else {
                         this.add(currentByte, oversizeCallback);
                    }
                    break;
               case IAC_COMMAND_WITH_OPTION:
                    this.add(currentByte, oversizeCallback);
                    state = InbReaderState.IAC_END; // <<<<<< END
                    break;
               case NORMAL_END:
                    break;
               default:
                    logger.warn("Invalid reader state: " + state);
                    state = InbReaderState.NORMAL_READING; // reset to normal reading on invalid state
          }

          if (state == InbReaderState.NORMAL_END || state == InbReaderState.IAC_END) {
               this.procesEnd(c);
          }
     }

     private void procesEnd(Charset c) {
          // convert buf to byte array and then to string
          byte[] bytes = new byte[this.currentIndex];
          for (int i = 0; i < this.currentIndex; i++) {
               bytes[i] = (byte) buf[i];
          }
          String msgContent = new String(bytes, c);
          InbMsg msg = null;
          if (state == InbReaderState.IAC_END) {
               msg = InbMsg.buildIACConfirmMsg(bytes);
          } else {
               msg = InbMsg.build(msgContent);
          }
          inbMsgQueue.put(msg);
          this.clear();
          state = InbReaderState.NOT_STARTED;
     }

     /**
      * Add current byte to buffer and check for oversize. If oversize, call the callback to handle it and clear the buffer.
      * @param currentByte
      * @param oversizeCallback
      */
     private void add(int currentByte, OversizeCallback oversizeCallback){
           if(this.currentIndex >= maxLength){
               oversizeCallback.handle();
               this.clear();
          }
          this.buf[currentIndex++] = currentByte;
     }
     private void clear(){
          this.currentIndex = 0;
     }

     @FunctionalInterface
     interface OversizeCallback {
          void handle();
     }

}
