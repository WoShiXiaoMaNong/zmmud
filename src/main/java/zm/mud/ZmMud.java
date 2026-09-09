package zm.mud;

import java.io.IOException;
import java.net.UnknownHostException;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import zm.mud.ui.ZmMudUI;

/**
 * Zm MUD 主程序入口
 */
public class ZmMud {
    private static final Logger logger = org.apache.logging.log4j.LogManager.getLogger(ZmMud.class);

    public static ApplicationContext context = new AnnotationConfigApplicationContext("zm.mud");

    public static void main(String[] args) throws UnknownHostException, IOException {
        ZmMud app = new ZmMud();
        app.start();
    }


    public void start(){
        ZmMudUI zmMudUI = context.getBean(ZmMudUI.class);
        zmMudUI.start();

    }

  

}
