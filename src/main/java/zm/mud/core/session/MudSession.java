package zm.mud.core.session;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;

import zm.mud.ZmMud;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.client.MudClient;
import zm.mud.core.network.threads.ThreadPoolService;
import zm.mud.pkuxkx.gmcp.GMCPContext;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.utils.SpringBeanUtil;

public class MudSession {
    private static final Logger logger = LogManager.getLogger(MudSession.class);

    private String host;
    private int port;

    private String sessionId;
    private String sessionName;

    private MudClient client;
    private OubMsgService oubMsgService;

    private GMCPContext gmcpContext;

    private TriggerFactory triggerFactory;

    private ThreadPoolService threadPoolService;

    private volatile SessionStatus status;

    private GlobalCfg globalCfg ;

    private static final Map<String, MudSession> allSessionMap = new HashMap<>();
    private static final Lock sessionMapLock = new ReentrantLock();

    public static MudSession newSession(String host,int port) {
        try {
            sessionMapLock.tryLock();
            MudSession session = new MudSession(UuidUtil.getTimeBasedUuid().toString(),host,port);
            allSessionMap.put(session.getSessionId(), session);
            return session;
        } catch (Exception e) {
            logger.error("Session start error!", e);
        } finally {
            sessionMapLock.unlock();
        }
        return null;
    }

    public static Map<String, MudSession> allSession() {
        return allSessionMap;
    }

    private MudSession(String sessionId,String host,int port) {
        this.sessionId = sessionId;
        this.oubMsgService = SpringBeanUtil.getBean(OubMsgService.class);
        this.triggerFactory = SpringBeanUtil.getBean(TriggerFactory.class);
        this.threadPoolService = SpringBeanUtil.getBean(ThreadPoolService.class);
        this.gmcpContext = new GMCPContext();
        this.host = host;
        this.port = port;
        this.status = SessionStatus.CREATED;
        this.globalCfg = SpringBeanUtil.getBean(GlobalCfg.class);
    }

        

    public GlobalCfg getGlobalCfg() {
        return globalCfg;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void start() {
        this.client = ZmMud.context.getBean(MudClient.class,this);
        this.triggerFactory.reload(this);
        boolean isConnected = client.connect(this.getHost(),this.getPort());
        if (!isConnected) {
            logger.error("Failed to connect to server");
            return;
        }
        logger.info("Connected to server successfully");

        ThreadPoolService threadStarter = ZmMud.context.getBean(ThreadPoolService.class);
        threadStarter.startAllThreads(this);
        this.setStatus(SessionStatus.ACTIVE);

    }

    public GMCPContext getGmcpContext() {
        return gmcpContext;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public MudClient getClient() {
        return client;
    }

    /**
     * 关闭当前session
     */
    public void close() {
        try {
            sessionMapLock.tryLock();
            allSessionMap.remove(this.getSessionId());
            threadPoolService.shutdown(this);
            this.client.close();
            this.setStatus(SessionStatus.CLOSED);
        } catch (Exception e) {
            logger.error("Session start error!", e);
        } finally {
            sessionMapLock.unlock();
        }
    }

    public static MudSession getSession(String sessionId) {
        try {
            sessionMapLock.tryLock();
            return allSessionMap.get(sessionId);
        } catch (Exception e) {
            logger.error("Session start error!", e);
        } finally {
            sessionMapLock.unlock();
        }
        return null;

    }

    public void send(String input) {
        this.oubMsgService.send(this, input);
    }

    public boolean isAvailable() {
        return SessionStatus.isAvailable(this.status);
    }
}
