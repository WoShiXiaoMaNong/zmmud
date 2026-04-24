package zm.mud.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.outbound.message.OubMessage;
import zm.mud.utils.CloseUtil;

@Service
public class MudClient implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(MudClient.class);

    private String host;
    private int port;
    private Charset charset;

    private Socket socket;


    private InputStream inputStream;
    private OutputStream  outputStream;

    @Autowired
    private CloseUtil closeUtil;

    public MudClient() {
    }

    

    public void connect(String host, int port, Charset charset) {
        this.host = host;
        this.port = port;
        this.charset = charset == null ? Charset.forName("GBK") : charset;
        try{
        socket = new Socket(this.host, this.port);
        // reader = new BufferedReader(
        //         new InputStreamReader(socket.getInputStream(), this.charset));
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        logger.info("Connected to server {}:{}", this.host, this.port);
    } catch (IOException e) {
        logger.error("Failed to connect to server", e);
    }

}

    // public String readLine(){
    //     try {
    //         return reader.readLine();
    //     } catch (IOException e) {
    //         logger.error("Failed to read line from server", e);
    //         return null;
    //     }
    // }

    public int read(){
        try {
            return inputStream.read();
        } catch (IOException e) {
            logger.error("Failed to read byte from server", e);
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public synchronized void sendLine(OubMessage msg) {
        try {
            String line = msg.getContent();
            outputStream.write(line.getBytes(this.charset));
            outputStream.write("\r\n".getBytes(this.charset)); // 发送行结束符
            outputStream.flush();
        } catch (IOException e) {
            logger.error("Failed to send line to server", e);
        }
    }

    public synchronized void send(byte[] data) {
        try {
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            logger.error("Failed to send data to server", e);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public void close() throws Exception {
        this.closeUtil.close(inputStream, outputStream, socket);
    }
}
