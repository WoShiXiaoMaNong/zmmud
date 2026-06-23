package zm.mud.core.cfg;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration 
public class ApplicationConfig {
    

    @Value("${mud.ui.terminalType}")
    private String terminalType;

    public String getTerminalType() {
        return terminalType;
    }



    
}
