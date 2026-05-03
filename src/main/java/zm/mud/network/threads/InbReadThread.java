package zm.mud.network.threads;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zm.mud.client.MudClient;
import zm.mud.network.inbound.reader.InbMsgReader;

@Service
public class InbReadThread extends IZmmudThread {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(InbReadThread.class);

    @Autowired
    private MudClient client;

    @Autowired
    private InbMsgReader reader;

    @Override
    public boolean doRun() {
        try {
            reader.handleByte(client.read(), client.getCharset());
            return true;
        } catch (Exception e) {
            logger.error("Failed to read from server", e);
            throw e;
        }

    }

}
