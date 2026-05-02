package zm.mud.network.outbound.message;

public class NrmOubMsg  implements OubMsg {

    private String content;

    public NrmOubMsg(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
