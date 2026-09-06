package zm.mud.core.automation.trigger.cfg;

public enum TriggerType {
    INBOUNG_TRIGGER("inbound","入站触发器"),
    OUTBOUNG_TRIGGER("outbound","出站触发器");

    String type;
    String desc;

    TriggerType(String type,String desc){
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    


}
