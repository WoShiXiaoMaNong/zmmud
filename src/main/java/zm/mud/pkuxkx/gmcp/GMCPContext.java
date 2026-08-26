package zm.mud.pkuxkx.gmcp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import zm.mud.pkuxkx.gmcp.channel.move.PkuxkxRoom;

@Component
public class GMCPContext {
     private static final Logger logger = LogManager.getLogger(GMCPContext.class);
    
    public Map<String,Object> statusData = new HashMap<>();
    private Lock statusLock = new ReentrantLock();

    private PkuxkxRoom currentRoom;
    private Lock roomLock = new ReentrantLock();

    public void setRoom(PkuxkxRoom room) {
        try {
            roomLock.lock();
            this.currentRoom = room;
        }catch(Exception e){
            logger.error("Error while setting current room: " + room, e);
        }
        finally {
            roomLock.unlock();
        }
    }

    public PkuxkxRoom getCurrentRoom() {
        return this.currentRoom;
    }
    

    public void putStatus(String key, Object value) {
        try {
            statusLock.lock();
            statusData.put(key, value);
        }catch(Exception e){
            logger.error("Error while putting status: " + key + " with value: " + value, e);
        }
        finally {
            statusLock.unlock();
        }
    }


    public Object getStatus( String key) {
        return statusData.get(key);
    }


}
