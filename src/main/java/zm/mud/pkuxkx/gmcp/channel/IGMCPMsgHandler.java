package zm.mud.pkuxkx.gmcp.channel;

import zm.mud.pkuxkx.gmcp.GMCPContext;

public interface IGMCPMsgHandler {
    /**
     * 解析 GMCP 消息, 并更新 GMCPContext 中的状态
     * @param packageName GMCP 包名
     * @param jsonPayload JSON 数据
     */
    void parse(String packageName, String jsonPayload, GMCPContext gmcpContext);
}
