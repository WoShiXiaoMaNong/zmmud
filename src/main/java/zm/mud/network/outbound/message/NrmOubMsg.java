package zm.mud.network.outbound.message;

public class NormalOutboundMsg  implements OubMsg {

    private String content;

    public NormalOutboundMsg(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
