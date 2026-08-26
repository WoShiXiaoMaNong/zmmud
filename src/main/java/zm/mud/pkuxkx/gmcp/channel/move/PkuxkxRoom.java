package zm.mud.pkuxkx.gmcp.channel.move;


import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;

public class PkuxkxRoom {

    @JSONField(name = "short")
    private String name;

    @JSONField(name = "dir")
    private List<String> dir;

    /**
     * 当正常进入房间后，这个字段是true
     */
    @JSONField(name = "result")
    private boolean result;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean getResult() {
        return result;
    }
    public void setResult(boolean result) {
        this.result = result;
    }
    public List<String> getDir() {
        return dir;
    }
    public void setDir(List<String> dir) {
        this.dir = dir;
    }

    

    
}
