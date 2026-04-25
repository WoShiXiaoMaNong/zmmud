package zm.mud.network.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class CloseUtil {
    private static final Logger logger = LogManager.getLogger(CloseUtil.class);

    public void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    logger.error("Failed to close resource: " + closeable, e);
                }
            }
        }
    }
}
