package zm.mud.client;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import zm.mud.network.ConnectionManager;
import zm.mud.network.outbound.message.OubMsg;
import zm.mud.network.utils.CloseUtil;

@Service
public class MudClient implements AutoCloseable {
    private static final Logger logger = LogManager.getLogger(MudClient.class);

    private String host;
    private int port;
    private Charset charset;

    @Autowired
    private CloseUtil closeUtil;

    @Autowired
    private ConnectionManager connectionManager;

    public MudClient() {
    }

    /**
     * <pre>
     * 提供一个无参的 connect 方法，
     * 使用已经设置好的 host、port 和 charset 进行连接。
     * 这对于在 Spring 中通过配置文件注入参数后直接连接非常有用。
     * </pre>
     */
    public boolean connect() {
        return this.connect(this.host, this.port, this.charset);
    }

    public boolean connect(String host, int port, Charset charset) {
        try {
            if (this.connectionManager == null) {
                this.connectionManager = new ConnectionManager();
            }

            if (this.connectionManager.isConnected()) {
                logger.warn("Already connected to server {}:{}", this.host, this.port);
                return true;
            }

            this.connectionManager.connect(host, port, charset);
            return true;
        } catch (Exception e) {
            logger.error("Failed to connect to server {}:{}", host, port, e);
            return false;
        }
    }

    public int read() {
        try {
            return this.connectionManager.readByte();
        } catch (IOException e) {
            logger.error("Failed to read byte from server", e);
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    public synchronized void sendLine(OubMsg msg) {
        try {
            String line = msg.getContent();
            this.connectionManager.sendLine(line);
        } catch (IOException e) {
            logger.error("Failed to send line to server", e);
        }
    }

    public synchronized void send(byte[] data) {
        try {
            this.connectionManager.sendData(data);
        } catch (IOException e) {
            logger.error("Failed to send data to server", e);
        }
    }

    public Charset getCharset() {
        return charset;
    }

    @Override
    public void close() throws Exception {
        this.closeUtil.close(this.connectionManager);
    }

    @Value("${mud.server.host}")
    public void setHost(String host) {
        this.host = host;
    }

    @Value("${mud.server.port}")
    public void setPort(int port) {
        this.port = port;
    }

    @Value("${mud.server.charset}")
    public void setCharset(String charset) {
        if (charset == null || charset.isEmpty()) {
            this.charset = Charset.forName("GBK");
        } else {
            this.charset = Charset.forName(charset);
        }
        logger.info("Charset set to {}", this.charset.name());
    }
}
