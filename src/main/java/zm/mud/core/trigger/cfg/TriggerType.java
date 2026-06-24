package zm.mud.core.trigger.cfg;

public enum TriggerType {
    INBOUNG_TRIGGER("inbound"),
    OUTBOUNG_TRIGGER("outbound");

    String type;

    TriggerType(String type){
        this.type = type;
    }


}
