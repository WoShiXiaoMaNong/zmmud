package zm.mud.network; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.network.utils.CloseUtil;

@Component
@Scope("prototype") // 设置为多例，每次注入都会创建一个新的实例
public class ConnectionManager implements AutoCloseable,DisposableBean  { 
    private static final Logger logger = LogManager.getLogger(ConnectionManager.class);

    private String host;
    private int port;
    private Charset charset;

    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    private boolean connected = false;

    
    @Autowired
    private CloseUtil closeUtil;

    /**
     * Default charset is GBK, which is commonly used in MUD clients for Chinese characters.
     * @param host
     * @param port
     * @param charset
     */
    public void connect(String host, int port, Charset charset) {
        this.host = host;
        this.port = port;
        this.charset = charset == null ? Charset.forName("GBK") : charset;
        try {
            socket = new Socket(this.host, this.port);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            logger.info("Connected to server {}:{}", this.host, this.port);
            connected = true;
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
            throw new RuntimeException(e); // 或者抛出一个自定义的网络异常
        }
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public int readByte() throws IOException { // 更名以明确其功能
        return inputStream.read(); // 保持异常抛出，让上层处理
    }

    public void sendLine(String content) throws IOException { // 接收原始字符串，不处理 OubMessage
        if (outputStream != null && !socket.isClosed()) {
            outputStream.write(content.getBytes(this.charset));
            outputStream.write("\r\n".getBytes(this.charset));
            outputStream.flush();
        } else {
            throw new IOException("Connection is closed");
        }
    }

    public void sendData(byte[] data) throws IOException {
        if (outputStream != null && !socket.isClosed()) {
            outputStream.write(data);
            outputStream.flush();
        } else {
            throw new IOException("Connection is closed");
        }
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public void close() throws IOException {
        this.closeUtil.close(inputStream, outputStream, socket);
        connected = false;
    }

    @Override
    public void destroy() throws Exception {
        this.close();
    }
}