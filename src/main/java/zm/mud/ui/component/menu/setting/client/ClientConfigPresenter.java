package zm.mud.ui.component.menu.setting.client;


import javax.swing.*;

/**
 * 客户端全局配置中心控制器 (Presenter)
 */
public class ClientConfigPresenter {

    private final ClientConfigView configView;
    // 内存中的全局配置数据缓存
    private ClientConfigEntry configEntry;

    public ClientConfigPresenter(ClientConfigView configView) {
        this.configView = configView;
    }

    /**
     * 加载全局配置并回显到界面
     */
    public void loadGlobalConfig() {
        // 核心：直接调用服务层获取全局配置
        ClientConfigEntry entry = ClientConfigService.getGlobalConfig();
        
        // 🛠️ 健壮性优化：防止底层返回空指针
        this.configEntry = entry != null ? entry : new ClientConfigEntry();
        
        // ─── 数据回显 ───
        configView.setConfig(configEntry.getFontName(), configEntry.getFontSize());
    }

    /**
     * 当玩家点击对话框自带的“确定”按钮时进行持久化
     */
    public void saveGlobalConfig() {
        if (configEntry == null) {
            configEntry = new ClientConfigEntry();
        }

        // ─── 收集配置面板上的最新数据 ───
        configEntry.setFontName(configView.getSelectedFontName());
        configEntry.setFontSize(configView.getSelectedFontSize());

        // ─── 数据合法性校验 ───
        if (configEntry.getFontName() == null || configEntry.getFontName().isEmpty()) {
            JOptionPane.showMessageDialog(null, "未选择有效字体", "校验失败", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (configEntry.getFontSize() <= 0) {
            JOptionPane.showMessageDialog(null, "字体大小必须大于 0", "校验失败", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 调用服务层一次性持久化到全局文件
        ClientConfigService.saveGlobalConfig(configEntry);
    }

    public ClientConfigEntry getConfigEntry() {
        // 提供给外部（如主界面渲染器）直接获取最新内存配置的接口
        return configEntry;
    }
}