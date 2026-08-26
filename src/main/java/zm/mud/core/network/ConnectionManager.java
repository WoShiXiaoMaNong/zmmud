package zm.mud.core.network; 

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import zm.mud.utils.CloseUtil;

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

    private Lock sendLock;
    
    @Autowired
    private CloseUtil closeUtil;

    public ConnectionManager(){
        this.sendLock = new ReentrantLock();
    }

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
             // 1. 改为无参构造，方便后续配置
            socket = new Socket();
            
            // 2. 启用 TCP KeepAlive，防止中间路由因长时间无数据切断连接
            socket.setKeepAlive(true);

            socket.setTcpNoDelay(true); 
            
            // 3. 设置读取超时时间（例如 10-15 分钟），超过该时间未收到数据会抛出 SocketTimeoutException
            // MUD 游戏挂机很常见，不建议设太短。若有心跳机制，可设为 90000 (90秒)
            socket.setSoTimeout(15 * 60 * 1000); 

            // 4. 增加连接超时限制（例如 5 秒），避免无限期等待不可达的服务器
            socket.connect(new InetSocketAddress(this.host, this.port), 5000);

            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
            logger.info("Connected to server {}:{}", this.host, this.port);
            connected = true;
        } catch (IOException e) {
            logger.error("Failed to connect to server", e);
             cleanup();
            throw new RuntimeException(e); // 或者抛出一个自定义的网络异常
        }
    }

    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public int readByte() throws IOException { // 更名以明确其功能
        if (!isConnected()) {
            throw new IOException("Connection is not active");
        }
        try {
            int data = inputStream.read(); 
            
            // 判斷EOF
            if (data == -1) {
                connected = false; 
                throw new IOException("Remote server closed the connection (EOF)");
            }
            
            return data; 
        } catch (IOException e) {
            connected = false;
            throw e;
        }
    }

    public void sendLine(String content) throws IOException { // 接收原始字符串，不处理 OubMessage
        this.sendLock.lock();
        try{
            if (outputStream != null && !socket.isClosed()) {
                outputStream.write(content.getBytes(this.charset));
                outputStream.write("\r\n".getBytes(this.charset));
                outputStream.flush();
            } else {
                throw new IOException("Connection is closed");
            }
        }catch(Exception e){
            throw e;
        }finally{
            this.sendLock.unlock();
        }
    }

    public void sendData(byte[] data) throws IOException {
        this.sendLock.lock();
        try{
            if (outputStream != null && !socket.isClosed()) {
                outputStream.write(data);
                outputStream.flush();
            } else {
                throw new IOException("Connection is closed");
            }
        }catch(Exception e){
            throw e;
        }finally{
            this.sendLock.unlock();
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
    private void cleanup() {
        connected = false;
        if (closeUtil != null) {
            closeUtil.close(inputStream, outputStream, socket);
        }
    }
}