package zm.mud.core.session;

/**
 * MUD Client 会话状态枚举
 */
public enum SessionStatus {
    
    /**
     * 已创建。
     * Session 对象已在内存中初始化（加载了服务器配置、触发器、别名等），但尚未发起网络连接。
     * 适用于：新建未连接的 Tab。
     */
    CREATED,

    /** 1. 正在尝试连接或登录服务器（合并了 CONNECTING, CONNECTED, AUTHENTICATING） */
    CONNECTING,

    /** 2. 正常游戏里，可以发送指令（核心状态） */
    ACTIVE,

    /** 3. 网络断开了，Tab 还在，界面变灰，等待重连 */
    DISCONNECTED,

    /** 4. 彻底关闭，销毁内存，Tab 被 X 掉 */
    CLOSED;
    

    /** 判断 Session 当前是否能在游戏里正常操作 */
    public static boolean isAvailable(SessionStatus status) {
        return status!= null && status == ACTIVE;
    }
}
