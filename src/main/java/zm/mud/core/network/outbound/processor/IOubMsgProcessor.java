package zm.mud.core.network.outbound.processor;

import zm.mud.core.network.outbound.message.OubMsg;

public interface IOubMsgProcessor {

    /**
     * <pre>
     * Processes the given outbound message.
     * 1. return false : 当前消息会被后续processor继续处理，直到有一个processor返回false，或者所有processor都处理完毕
     * 2. return true : 当前消息不会被后续processor继续处理，直接结束处理
     * </pre>
     * @param msg
     * @return
     */
    boolean processMessage(OubMsg msg);

}
