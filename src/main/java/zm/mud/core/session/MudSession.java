package zm.mud.core.session;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;

import zm.mud.ZmMud;
import zm.mud.core.IUiLogger;
import zm.mud.core.api.OubMsgService;
import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.client.MudClient;
import zm.mud.core.command.IOubCommand;
import zm.mud.core.command.OubCommandParser;
import zm.mud.core.command.cmd.NormalOubCommand;
import zm.mud.core.network.threads.ThreadPoolService;
import zm.mud.core.thread.ZmmudThreadPool;
import zm.mud.ui.cfg.GlobalCfg;
import zm.mud.utils.SpringBeanUtil;
import zm.mud.world.common.gmcp.GMCPContext;

public class MudSession {
    private static final Logger logger = LogManager.getLogger(MudSession.class);

    // 玩家特有的出站命令队列
    private final CommandQue<IOubCommand> oubCommandQueue = new CommandQue<>();

    private String mudWorldCode;

    private String host;
    private int port;

    private String sessionId;
    private String sessionName;

    /**
     * 不需要走命令解析，直接发送
     */
    private Set<String> directlySendMsgPrefix;

    private MudClient client;
    private OubMsgService oubMsgService;

    private GMCPContext gmcpContext;

    private TriggerFactory triggerFactory;

    private ThreadPoolService threadPoolService;

    private volatile SessionStatus status;

    private GlobalCfg globalCfg ;


    private IUiLogger uiLogger;


    private String userName;
    private String userId;

    private OubCommandParser oubCommandParser;

    private static final Map<String, MudSession> allSessionMap = new HashMap<>();
    private static final Lock sessionMapLock = new ReentrantLock();

    /**
     * <pre>
     * mudWorldCode:用于唯一确定一套mud游戏世界的配置相关文件，例如 trigger配置，timer配置等
     * </pre>
     * @param host
     * @param port
     * @param mudWorldCode
     * @return
     */
    public static MudSession newSession(String host,int port,String mudWorldCode,IUiLogger uiLogger) {
        try {
            sessionMapLock.tryLock();
            MudSession session = new MudSession(UuidUtil.getTimeBasedUuid().toString(),host,port,mudWorldCode);
            session.setUiLogger(uiLogger);
            session.addDirectlySendMsgPrefix("qq");
            session.addDirectlySendMsgPrefix("chat");
            allSessionMap.put(session.getSessionId(), session);

            return session;
        } catch (Exception e) {
            logger.error("Session start error!", e);
        } finally {
            sessionMapLock.unlock();
        }
        return null;
    }

    public void setUiLogger(IUiLogger uiLogger){
        this.uiLogger = uiLogger;
    }

    /**
     * <pre>
     * 指定一些不需要走命令解析，直接发送的消息，例如：
     * 1. chat 开头，这是一条聊天消息，不需走客户端这边的命令解析，直接外发
     * 
     * 避免chat之类的消息被客户端的命令解析器拆分为多段往外法。
     * </pre>
     * @param prefix
     */
    public void addDirectlySendMsgPrefix(String prefix){
        this.directlySendMsgPrefix.add(prefix);
    }

    public static Map<String, MudSession> allSession() {
        return allSessionMap;
    }

    private MudSession(String sessionId,String host,int port,String mudWorldCode) {
        this.sessionId = sessionId;
        this.oubMsgService = SpringBeanUtil.getBean(OubMsgService.class);
        this.triggerFactory = SpringBeanUtil.getBean(TriggerFactory.class);
        this.threadPoolService = SpringBeanUtil.getBean(ThreadPoolService.class);
        this.gmcpContext = new GMCPContext();
        this.host = host;
        this.port = port;
        this.mudWorldCode = mudWorldCode;
        this.status = SessionStatus.CREATED;
        this.globalCfg = SpringBeanUtil.getBean(GlobalCfg.class);
        this.directlySendMsgPrefix = new HashSet<>();
        this.oubCommandParser = SpringBeanUtil.getBean(OubCommandParser.class);
    }

        

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMudWorldCode() {
        return mudWorldCode;
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

    /**
     * 只用于命令回显
     * @param msg
     */
    public void echoCommandToUI(String msg){
        if(this.globalCfg.echoCommand()){
            this.printToUI("> " + msg);
        }  
    }

    public void printToUI(String msg){
        if(this.uiLogger != null){
            this.uiLogger.info(this, msg);
        }
    }

    /**
     * 
     * @param input
     */
    public void send(String commandStr) {
        boolean shouldSendDirectly = false;
        for(String prefix : this.directlySendMsgPrefix){
            if( commandStr.startsWith(prefix)){
                shouldSendDirectly = true;
                break;
            }
        }
        if( shouldSendDirectly ){
            this.oubMsgService.sendOutbound(this, commandStr);
        }else{
            this.sendCommand( commandStr);
        }
        
    }

    private void sendCommand(String commandStr){
        
        List<IOubCommand> commands = oubCommandParser.parse(this, commandStr);
       
       ZmmudThreadPool.execute(() -> {
            // 1. 先把新命令安全地放入队列
            oubCommandQueue.addCommands(commands);

            // 2. 核心状态循环：确保新放入的命令一定会被执行
            while (true) {
                // 尝试抢占执行权
                if (!oubCommandQueue.tryToExecute()) {
                    // 如果抢占失败，说明已经有另一个线程在消费队列了。
                    // 刚才我们通过 addCommands 放入的命令，会被那个正在执行的线程在 while 循环里顺便消费掉，
                    // 所以当前线程可以安全地退出。
                    return;
                }

                try {
                    // 抢占成功，开始消费队列中的所有命令
                    IOubCommand cmd = oubCommandQueue.pollCommand();
                    while (cmd != null) {
                        if (cmd instanceof NormalOubCommand) {
                            this.oubMsgService.sendOutbound(this, cmd.getCommandStr());
                        } else {
                            cmd.exec();
                        }
                        cmd = oubCommandQueue.pollCommand();
                    }
                } catch(Exception e){
                    logger.error("Excute Cmd error!",e);
                    // 正常情况，会在 session.pollCommand();方法送，发现queue为空的时候，自动将executing设置为false
                    // 只有异常情况，才需要手动设置，允许别的进程尝试来获取。
                    oubCommandQueue.finishExecuting();
                }

            }
        });
    }

    public boolean isAvailable() {
        return SessionStatus.isAvailable(this.status);
    }

    public static void closeAll() {
        for(MudSession session : allSessionMap.values()){
            session.close();
        }
    }

    public void pushCommand(List<IOubCommand> transferedMsgs) {
        this.oubCommandQueue.pushCommand(transferedMsgs);
    }

    
}
