package zm.mud.core.automation.alias;

public class Alias {
    private String aliasName;
    private String aliasCommand;

    public Alias(String aliasName, String aliasCommand) {
        this.aliasName = aliasName;
        this.aliasCommand = aliasCommand;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public String getAliasCommand() {
        return aliasCommand;
    }

    public void setAliasCommand(String aliasCommand) {
        this.aliasCommand = aliasCommand;
    }

    
}
