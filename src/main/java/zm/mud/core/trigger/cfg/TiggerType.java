package zm.mud.core.trigger.cfg;

public enum TiggerType {
    INBOUNG_TRIGGER("inbound"),
    OUTBOUNG_TRIGGER("outbound");

    String type;

    TiggerType(String type){
        this.type = type;
    }


}
