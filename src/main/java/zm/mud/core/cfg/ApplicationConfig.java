package zm.mud.core.cfg;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class ApplicationConfig {
    

    @Value("${mud.terminalType}")
    private String terminalType;

      @Value("${mud.version}")
    private String version;

    public String getTerminalType() {
        return terminalType;
    }

    public String getVersion() {
        return version;
    }

    

    
}
