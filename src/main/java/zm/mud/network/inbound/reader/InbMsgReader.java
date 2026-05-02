package zm.mud.network.inbound.reader;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
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

     private List<Integer> buf;
     private InbReaderState state;

     @Autowired
     private InbMsgQueue inbMsgQueue;

     public InbMsgReader() {
          this.buf = new ArrayList<>();
          this.state = InbReaderState.NOT_STARTED;
     }

     public synchronized void handleByte(int currentByte, Charset c) {
     
          if (currentByte == IACConsts.IAC) {
               if(state == InbReaderState.NORMAL_READING){
                    state = InbReaderState.NORMAL_END;
                    this.procesEnd(c);
               }
               state = InbReaderState.IAC_READING;
               buf.add(currentByte);
               return;
              
          }
          switch (state) {
               case NOT_STARTED:
                    if (currentByte == IACConsts.IAC) {
                         state = InbReaderState.IAC_READING;
                    } else {
                         state = InbReaderState.NORMAL_READING;
                    }
                    buf.add(currentByte);
                    break;
               case NORMAL_READING:
                    if (currentByte == '\r') {
                         state = InbReaderState.NORMAL_READING_CR;
                    } else {
                         buf.add(currentByte);
                    }
                    break;
               case NORMAL_READING_CR:
                    if (currentByte == '\n') {
                         state = InbReaderState.NORMAL_READING_CRLF;
                         state = InbReaderState.NORMAL_END;
                    } else {
                         buf.add(currentByte);
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
                         buf.add(currentByte);
                    }
                    break;
               case IAC_SUBNEGOTIATION:
                    if (currentByte == IACConsts.CMD_SE) { // SE
                         state = InbReaderState.IAC_END; // <<<<<< END
                    } else {
                         buf.add(currentByte);
                    }
                    break;
               case IAC_COMMAND_WITH_OPTION:
                    buf.add(currentByte);
                    state = InbReaderState.IAC_END; // <<<<<< END
                    break;
               case NORMAL_END:
                    break;
               default:
                    logger.warn("Invalid reader state: " + state);
                    state = InbReaderState.NORMAL_READING; // reset to normal reading on invalid state
          }

          // read end, convert buf to message and put to queue
          if (state == InbReaderState.NORMAL_END || state == InbReaderState.IAC_END) {
               this.procesEnd(c);
          }
     }

     private void procesEnd(Charset c){
           // convert buf to byte array and then to string
               byte[] bytes = new byte[buf.size()];
               for (int i = 0; i < buf.size(); i++) {
                    bytes[i] = buf.get(i).byteValue();
               }
               String msgContent = new String(bytes, c);
               InbMsg msg = null;
               if (state == InbReaderState.IAC_END) {
                    msg = InbMsg.buildIACConfirmMsg(buf.stream().mapToInt(Integer::intValue).toArray());
               } else {
                    msg = InbMsg.build(msgContent);
               }
               inbMsgQueue.put(msg);
               buf.clear();
               state = InbReaderState.NOT_STARTED;
     }

}
