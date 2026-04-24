package zm.mud.outbound.message;

public class NormalOutboundMsg  implements OubMessage {

    private String content;

    public NormalOutboundMsg(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
