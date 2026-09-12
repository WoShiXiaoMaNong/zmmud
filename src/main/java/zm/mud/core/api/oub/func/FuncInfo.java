package zm.mud.core.api.oub.func;

import zm.mud.core.api.oub.IOubCommand;

public class FuncInfo {
    private String funcCode;
    private String[] params;
    private String originCommandStr;

    public FuncInfo(String originCommandStr) {
        this.init(originCommandStr);
        this.originCommandStr = originCommandStr;
    }

    private void init(String originCommandStr) {
        if (originCommandStr == null || originCommandStr.trim().isEmpty()) {
            this.funcCode = "";
            this.params = new String[0];
            return;
        }

        // \\s+ 代表按照一个或多个空白字符（空格、制表符等）进行切分
        String[] info = originCommandStr.trim().split("\\s+");

        // 1. 提取功能码（如 #2 或 #wa）
        String rawCode = info[0];
        if (rawCode.startsWith(IOubCommand.FUNCTION_CMD_PREFIX) && rawCode.length() > 1) {
            this.funcCode = rawCode.substring(1); // 截取从索引 1 开始到末尾的字符串
        } else {
            this.funcCode = rawCode; // 如果不以 # 开头（防御性代码）或只有一个 #
        }

        // 2. 提取后续参数（截取从索引 1 开始到最后的数组元素）
        if (info.length > 1) {
            this.params = new String[info.length - 1];
            // 使用 System.arraycopy 高效复制剩余元素到 params 数组中
            System.arraycopy(info, 1, this.params, 0, this.params.length);
        } else {
            // 如果只有功能码没有参数（例如玩家只输入了 "#look"），则参数列表为空数组
            this.params = new String[0];
        }
    }

    public String getFuncCode() {
        return funcCode;
    }

    public String[] getParams() {
        return params;
    }

    public String getOriginCommandStr() {
        return originCommandStr;
    }



}
