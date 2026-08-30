package zm.mud.pkuxkx.gmcp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class GMCPContext {
     private static final Logger logger = LogManager.getLogger(GMCPContext.class);
    
     public Map<String/* Channel Name */,Map<String,Object>> gmcpData = new HashMap<>();
    private Lock statusLock = new ReentrantLock();

    private Map<String,Object> currentRoom;
    private Lock roomLock = new ReentrantLock();

    public void setRoom(Map<String,Object> room) {
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

    public Map<String,Object> getCurrentRoom() {
        return this.currentRoom;
    }
    
    public void put(String channel,String key, Object value){
        try {
            statusLock.lock();
            Map<String,Object> dataMap = this.gmcpData.get(channel);
            if(dataMap == null){
                dataMap = new HashMap<>();
                this.gmcpData.put(channel,dataMap);
            }
            dataMap.put(key, value);
        }catch(Exception e){
            logger.error("Error while putting status: " + key + " with value: " + value, e);
        }
        finally {
            statusLock.unlock();
        }
    }

    public Object getGmcpData(String channel,String key) {
        Map<String,Object> dataMap = this.gmcpData.get(channel);
        if(dataMap == null){
            return null;
        }
        return dataMap.get(key);
    }

    public Map<String, Object> getGmcpData(String channel) {
        try {
            statusLock.lock();
            return new HashMap<>(this.gmcpData.get(channel));
        } finally {
            statusLock.unlock();
        }
    }

    public Map<String/* Channel Name */,Map<String,Object>> getGmcpData(){
        return this.gmcpData;
    }


}
