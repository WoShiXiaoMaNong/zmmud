package zm.mud.core.protocol.gmcp;

import zm.mud.core.session.MudSession;

public interface IGMCPOnMessage {
    /**
     * 处理 GMCP 消息
     * @param packageName GMCP 包名
     * @param jsonPayload JSON 数据
     */
    void onMessage(MudSession session,String packageName, String jsonPayload);
}
