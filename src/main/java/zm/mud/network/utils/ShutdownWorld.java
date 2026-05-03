package zm.mud.network.utils;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import zm.mud.IShutdownFunc;

@Service
public class ShutdownWorld extends Thread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ShutdownWorld.class);

    @Autowired
    List<IShutdownFunc> shutdownFuncs;

    @Autowired
    private ApplicationContext context;
    
    @PostConstruct
    public void init() {
        this.setName("ShutdownWorld");
        Runtime.getRuntime().addShutdownHook(this);
        logger.info("ShutdownWorld initialized and ready to handle shutdown tasks.");
    }

    @Override
    public void run() {
        logger.info("ShutdownWorld is running. Performing cleanup tasks...");
            if (context instanceof AbstractApplicationContext) {
                ((AbstractApplicationContext) context).close();
            }

        for (IShutdownFunc func : shutdownFuncs) {
            try {
                func.shutdown();
            } catch (Exception e) {
                logger.error("Error occurred while executing shutdown function: " + func.getClass().getSimpleName(), e);
            }
        }
        logger.info("Cleanup tasks completed. ShutdownWorld is exiting.");
    }

}
