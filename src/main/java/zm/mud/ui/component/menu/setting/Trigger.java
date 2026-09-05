package zm.mud.ui.component.menu.setting;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.ui.component.menu.AbsZmMudDialog;
import zm.mud.ui.component.menu.KeyValuePair;
import zm.mud.utils.SpringBeanUtil;

public class Trigger extends AbsZmMudDialog {

    private static final Logger logger = LogManager.getLogger(Trigger.class);

    // =========================================================
    // 左侧 Trigger
    // =========================================================

    private JList<TriggerConfigEntry> triggerList;
    private DefaultListModel<TriggerConfigEntry> listModel;

    private JButton btnAdd;
    private JButton btnDelete;

    private TriggerConfigEntry currentTrigger;

    // =========================================================
    // Trigger 基础配置
    // =========================================================

    private JTextField txtName;

    private JComboBox<String> cbTriggerType;

    private JSpinner spRemainingCount;

    // =========================================================
    // Matcher
    // =========================================================

    private JComboBox<KeyValuePair<String, String>> cbMatcherType;

    private JTextArea taMatcherExpression;

    private JPanel matcherParamsPanel;

    // =========================================================
    // Action
    // =========================================================

    private JComboBox<String> cbActionType;

    private JTextArea taActionExpression;

    private JPanel actionParamsPanel;

    // =========================================================
    // Trigger 选项
    // =========================================================

    private JCheckBox chkSync;

    private JCheckBox chkUnique;

    private JCheckBox chkAutoRegister;


    public Trigger(Frame owner, String title) {
        super(owner, title);
        
    }

    @Override
    protected JPanel getContentPanelUi() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

        mainPanel.setBorder(
                BorderFactory.createEmptyBorder(
                        10, 10, 10, 10));

        mainPanel.add(
                createLeftPanel(),
                BorderLayout.WEST);

        mainPanel.add(
                createRightPanel(),
                BorderLayout.CENTER);

        loadDemoData();

        return mainPanel;
    }

    // =========================================================
    // 左侧
    // =========================================================

    private JPanel createLeftPanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setPreferredSize(
                new Dimension(220, 0));

        listModel = new DefaultListModel<>();

        triggerList = new JList<>(listModel);

        triggerList.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        triggerList.setCellRenderer(
                new DefaultListCellRenderer() {

                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus) {

                        super.getListCellRendererComponent(
                                list,
                                value,
                                index,
                                isSelected,
                                cellHasFocus);

                        if (value instanceof TriggerConfigEntry) {

                            TriggerConfigEntry config = (TriggerConfigEntry) value;

                            String name = config.getName();

                            setText(
                                    name == null
                                            ? "(未命名)"
                                            : name);
                        }

                        return this;
                    }
                });

        triggerList.addListSelectionListener(
                this::onTriggerSelected);

        panel.add(
                new JScrollPane(triggerList),
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(
                new GridLayout(1, 2, 5, 5));

        btnAdd = new JButton("添加");

        btnDelete = new JButton("删除");

        btnAdd.addActionListener(
                e -> addTrigger());

        btnDelete.addActionListener(
                e -> deleteTrigger());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnDelete);

        panel.add(
                buttonPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // 右侧
    // =========================================================

    private JPanel createRightPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // -----------------------------------------------------
        // 基础配置
        // -----------------------------------------------------

        JPanel basicPanel = new JPanel(new GridBagLayout());

        basicPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Trigger 基础配置"));

        GridBagConstraints gbc = createGbc();

        // Name

        addLabel(
                basicPanel,
                gbc,
                0,
                0,
                "名称:");

        txtName = new JTextField();

        addComponent(
                basicPanel,
                gbc,
                1,
                0,
                txtName);

        // Trigger Type

        addLabel(
                basicPanel,
                gbc,
                0,
                1,
                "Trigger 类型:");

        cbTriggerType = new JComboBox<>(
                new String[] {
                        "Inbound",
                        "Outbound"
                });

        addComponent(
                basicPanel,
                gbc,
                1,
                1,
                cbTriggerType);

        // Remaining Count

        addLabel(
                basicPanel,
                gbc,
                0,
                2,
                "执行次数:");

        spRemainingCount = new JSpinner(
                new SpinnerNumberModel(
                        0,
                        0,
                        Integer.MAX_VALUE,
                        1));

        addComponent(
                basicPanel,
                gbc,
                1,
                2,
                spRemainingCount);

        // -----------------------------------------------------
        // Matcher
        // -----------------------------------------------------

        JPanel matcherPanel = createMatcherPanel();

        // -----------------------------------------------------
        // Action
        // -----------------------------------------------------

        JPanel actionPanel = createActionPanel();

        // -----------------------------------------------------
        // 顶部
        // -----------------------------------------------------

        JPanel topPanel = new JPanel(
                new GridLayout(1, 2, 8, 8));

        topPanel.add(basicPanel);
        topPanel.add(matcherPanel);

        panel.add(
                topPanel,
                BorderLayout.NORTH);

        // -----------------------------------------------------
        // 中间 Action
        // -----------------------------------------------------

        panel.add(
                actionPanel,
                BorderLayout.CENTER);

        // -----------------------------------------------------
        // 底部 Options
        // -----------------------------------------------------

        JPanel optionPanel = createOptionPanel();

        panel.add(
                optionPanel,
                BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================
    // Matcher UI
    // =========================================================

    private JPanel createMatcherPanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Matcher"));

        JPanel top = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = createGbc();

        addLabel(
                top,
                gbc,
                0,
                0,
                "类型:");

        cbMatcherType = new JComboBox<>();

        for(KeyValuePair<String,String> matcherItem: TriggerService.getMathers()){
            cbMatcherType.addItem(matcherItem);
        }

        addComponent(
                top,
                gbc,
                1,
                0,
                cbMatcherType);

        panel.add(
                top,
                BorderLayout.NORTH);

        JPanel expressionPanel = new JPanel(new BorderLayout(5, 5));

        expressionPanel.add(
                new JLabel("Expression:"),
                BorderLayout.NORTH);

        taMatcherExpression = new JTextArea(4, 30);

        taMatcherExpression.setLineWrap(false);

        taMatcherExpression.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        13));

        expressionPanel.add(
                new JScrollPane(
                        taMatcherExpression),
                BorderLayout.CENTER);

        panel.add(
                expressionPanel,
                BorderLayout.CENTER);

        matcherParamsPanel = new JPanel();

        matcherParamsPanel.setLayout(
                new BoxLayout(
                        matcherParamsPanel,
                        BoxLayout.Y_AXIS));

        matcherParamsPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Params"));

        panel.add(
                matcherParamsPanel,
                BorderLayout.SOUTH);

        cbMatcherType.addActionListener(
                e -> rebuildMatcherParams());

        return panel;
    }

    // =========================================================
    // Action UI
    // =========================================================

    private JPanel createActionPanel() {

        JPanel panel = new JPanel(new BorderLayout(5, 5));

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Action"));

        JPanel top = new JPanel(new BorderLayout(5, 5));

        top.add(
                new JLabel("类型:"),
                BorderLayout.WEST);

        cbActionType = new JComboBox<>(
                new String[] {
                        "Command",
                        "Lua",
                        "Java"
                });

        top.add(
                cbActionType,
                BorderLayout.CENTER);

        panel.add(
                top,
                BorderLayout.NORTH);

        JPanel expressionPanel = new JPanel(new BorderLayout(5, 5));

        expressionPanel.add(
                new JLabel("Expression:"),
                BorderLayout.NORTH);

        taActionExpression = new JTextArea();

        taActionExpression.setLineWrap(false);

        taActionExpression.setFont(
                new Font(
                        "Consolas",
                        Font.PLAIN,
                        14));

        expressionPanel.add(
                new JScrollPane(
                        taActionExpression),
                BorderLayout.CENTER);

        panel.add(
                expressionPanel,
                BorderLayout.CENTER);

        actionParamsPanel = new JPanel();

        actionParamsPanel.setLayout(
                new BoxLayout(
                        actionParamsPanel,
                        BoxLayout.Y_AXIS));

        actionParamsPanel.setBorder(
                BorderFactory.createTitledBorder(
                        "Params"));

        panel.add(
                actionParamsPanel,
                BorderLayout.SOUTH);

        cbActionType.addActionListener(
                e -> rebuildActionParams());

        return panel;
    }

    // =========================================================
    // Options
    // =========================================================

    private JPanel createOptionPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT));

        panel.setBorder(
                BorderFactory.createTitledBorder(
                        "Trigger 选项"));

        chkSync = new JCheckBox("Sync");

        chkUnique = new JCheckBox("Unique");

        chkAutoRegister = new JCheckBox("Auto Register");

        panel.add(chkSync);
        panel.add(chkUnique);
        panel.add(chkAutoRegister);

        return panel;
    }

    // =========================================================
    // Matcher Params
    // =========================================================

    private void rebuildMatcherParams() {

        matcherParamsPanel.removeAll();

        KeyValuePair<String,String> selectedItem= (KeyValuePair<String,String>) cbMatcherType.getSelectedItem();
        String type = selectedItem.getKey();
        /*
         * 这里根据你的 Matcher 实际实现继续扩展。
         *
         * 例如：
         *
         * Regex:
         * ignoreCase
         *
         * Include:
         * ignoreCase
         *
         * 其他 Matcher:
         * 暂时没有参数
         */

        if ("Regex".equals(type)) {

            JCheckBox ignoreCase = new JCheckBox("ignoreCase");

            matcherParamsPanel.add(
                    ignoreCase);
        }

        matcherParamsPanel.revalidate();

        matcherParamsPanel.repaint();
    }

    // =========================================================
    // Action Params
    // =========================================================

    private void rebuildActionParams() {

        actionParamsPanel.removeAll();

        String type = (String) cbActionType.getSelectedItem();

        /*
         * 根据 Action 实现动态生成参数。
         *
         * 例如：
         *
         * Lua:
         * timeout
         *
         * Command:
         * delay
         *
         * Java:
         * class
         */

        if ("Lua".equals(type)) {

            JTextField timeout = new JTextField();

            JPanel row = new JPanel(
                    new BorderLayout(5, 5));

            row.add(
                    new JLabel("timeout:"),
                    BorderLayout.WEST);

            row.add(
                    timeout,
                    BorderLayout.CENTER);

            actionParamsPanel.add(row);
        }

        actionParamsPanel.revalidate();

        actionParamsPanel.repaint();
    }

    // =========================================================
    // Trigger 选择
    // =========================================================

    private void onTriggerSelected(
            ListSelectionEvent e) {

        if (e.getValueIsAdjusting()) {
            return;
        }

        TriggerConfigEntry selected = triggerList.getSelectedValue();

        if (selected == null) {
            return;
        }

        // 保存之前正在编辑的 Trigger

        if (currentTrigger != null) {

            saveFormToConfig(
                    currentTrigger);
        }

        currentTrigger = selected;

        loadConfigToForm(
                selected);
    }

    // =========================================================
    // POJO -> UI
    // =========================================================

    private void loadConfigToForm(
            TriggerConfigEntry config) {

        // -----------------------------------------------------
        // 基础配置
        // -----------------------------------------------------

        txtName.setText(
                nullToEmpty(
                        config.getName()));

        cbTriggerType.setSelectedItem(
                config.getType());

        spRemainingCount.setValue(
                config.getRemainingCount() == null
                        ? 0
                        : config.getRemainingCount());

        // -----------------------------------------------------
        // Options
        // -----------------------------------------------------

        chkSync.setSelected(
                config.isSync());

        chkUnique.setSelected(
                config.isUnique());

        chkAutoRegister.setSelected(
                config.isAutoRegister());

        // -----------------------------------------------------
        // Matcher
        // -----------------------------------------------------

        MatcherAndActionConfigEntry matcher = config.getMatcher();

        if (matcher != null) {

            cbMatcherType.setSelectedItem(
                    matcher.getType());

            taMatcherExpression.setText(
                    nullToEmpty(
                            matcher.getExpression()));
        } else {

            cbMatcherType.setSelectedIndex(0);

            taMatcherExpression.setText("");
        }

        // -----------------------------------------------------
        // Action
        // -----------------------------------------------------

        MatcherAndActionConfigEntry action = config.getAction();

        if (action != null) {

            cbActionType.setSelectedItem(
                    action.getType());

            taActionExpression.setText(
                    nullToEmpty(
                            action.getExpression()));
        } else {

            cbActionType.setSelectedIndex(0);

            taActionExpression.setText("");
        }

        // 重新生成参数 UI

        rebuildMatcherParams();

        rebuildActionParams();
    }

    // =========================================================
    // UI -> POJO
    // =========================================================

    private void saveFormToConfig(
            TriggerConfigEntry config) {

        // -----------------------------------------------------
        // 基础配置
        // -----------------------------------------------------

        config.setName(
                txtName.getText().trim());

        config.setType(
                (String) cbTriggerType.getSelectedItem());

        config.setRemainingCount(
                (Integer) spRemainingCount.getValue());

        // -----------------------------------------------------
        // Options
        // -----------------------------------------------------

        config.setSync(
                chkSync.isSelected());

        config.setUnique(
                chkUnique.isSelected());

        config.setAutoRegister(
                chkAutoRegister.isSelected());

        // -----------------------------------------------------
        // Matcher
        // -----------------------------------------------------

        MatcherAndActionConfigEntry matcher = config.getMatcher();

        if (matcher == null) {

            matcher = new MatcherAndActionConfigEntry();

            config.setMatcher(matcher);
        }

        matcher.setType(
                (String) cbMatcherType.getSelectedItem());

        matcher.setExpression(
                taMatcherExpression.getText());

        matcher.setParams(
                collectMatcherParams());

        // -----------------------------------------------------
        // Action
        // -----------------------------------------------------

        MatcherAndActionConfigEntry action = config.getAction();

        if (action == null) {

            action = new MatcherAndActionConfigEntry();

            config.setAction(action);
        }

        action.setType(
                (String) cbActionType.getSelectedItem());

        action.setExpression(
                taActionExpression.getText());

        action.setParams(
                collectActionParams());
    }

    // =========================================================
    // Params
    // =========================================================

    private Map<String, Object> collectMatcherParams() {

        Map<String, Object> params = new LinkedHashMap<>();

        String type = (String) cbMatcherType.getSelectedItem();

        /*
         * 这里暂时只是演示。
         *
         * 真正项目中应该根据你的 Matcher
         * 实现类定义参数。
         */

        if ("Regex".equals(type)) {

            /*
             * TODO:
             *
             * 从 matcherParamsPanel 中
             * 找到 ignoreCase checkbox。
             */
        }

        return params;
    }

    private Map<String, Object> collectActionParams() {

        Map<String, Object> params = new LinkedHashMap<>();

        String type = (String) cbActionType.getSelectedItem();

        if ("Lua".equals(type)) {

            /*
             * TODO:
             *
             * 从 actionParamsPanel 中
             * 读取 timeout。
             */
        }

        return params;
    }

    // =========================================================
    // 添加
    // =========================================================

    private void addTrigger() {

        TriggerConfigEntry config = new TriggerConfigEntry();

        config.setName(
                "新 Trigger");

        config.setType(
                "Inbound");

        config.setRemainingCount(0);

        config.setSync(false);

        config.setUnique(false);

        config.setAutoRegister(false);

        // 默认 Matcher

        MatcherAndActionConfigEntry matcher = new MatcherAndActionConfigEntry();

        matcher.setType("Regex");

        matcher.setExpression("");

        matcher.setParams(
                new LinkedHashMap<>());

        config.setMatcher(matcher);

        // 默认 Action

        MatcherAndActionConfigEntry action = new MatcherAndActionConfigEntry();

        action.setType("Command");

        action.setExpression("");

        action.setParams(
                new LinkedHashMap<>());

        config.setAction(action);

        listModel.addElement(config);

        triggerList.setSelectedValue(
                config,
                true);
    }

    // =========================================================
    // 删除
    // =========================================================

    private void deleteTrigger() {

        int index = triggerList.getSelectedIndex();

        if (index < 0) {
            return;
        }

        TriggerConfigEntry config = listModel.getElementAt(index);

        int result = JOptionPane.showConfirmDialog(
                this,
                "确定删除 Trigger「"
                        + config.getName()
                        + "」吗？",
                "确认删除",
                JOptionPane.YES_NO_OPTION);

        if (result != JOptionPane.YES_OPTION) {
            return;
        }

        listModel.remove(index);

        currentTrigger = null;

        if (!listModel.isEmpty()) {

            triggerList.setSelectedIndex(
                    Math.min(
                            index,
                            listModel.size() - 1));
        }
    }

    // =========================================================
    // OK
    // =========================================================

    @Override
    protected void ok() {

        if (currentTrigger != null) {
            saveFormToConfig(currentTrigger);
        }

        List<TriggerConfigEntry> configs = new ArrayList<>();

        for (int i = 0; i < listModel.size(); i++) {
            configs.add(
                    listModel.getElementAt(i));
        }

        TriggerFactory factory = SpringBeanUtil.getBean(
                TriggerFactory.class);

        factory.save(configs);
    }

    // =========================================================
    // Utils
    // =========================================================

    private GridBagConstraints createGbc() {

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(4, 6, 4, 6);

        gbc.fill = GridBagConstraints.HORIZONTAL;

        return gbc;
    }

    private void addLabel(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            String text) {

        gbc.gridx = x;
        gbc.gridy = y;

        gbc.weightx = 0;

        panel.add(
                new JLabel(text),
                gbc);
    }

    private void addComponent(
            JPanel panel,
            GridBagConstraints gbc,
            int x,
            int y,
            Component component) {

        gbc.gridx = x;
        gbc.gridy = y;

        gbc.weightx = 1.0;

        panel.add(
                component,
                gbc);
    }

    private String nullToEmpty(
            String value) {

        return value == null
                ? ""
                : value;
    }

    // =========================================================
    // Demo
    // =========================================================

    private void loadDemoData() {

        // -----------------------------------------------------
        // 自动吃药
        // -----------------------------------------------------

        TriggerConfigEntry eatMedicine = new TriggerConfigEntry();

        eatMedicine.setName("自动吃药");
        eatMedicine.setType("Inbound");
        eatMedicine.setRemainingCount(0);

        eatMedicine.setSync(false);
        eatMedicine.setUnique(true);
        eatMedicine.setAutoRegister(true);

        MatcherAndActionConfigEntry matcher = new MatcherAndActionConfigEntry();

        matcher.setType("Regex");

        matcher.setExpression(
                "^你的气血发生危险");

        matcher.setParams(
                new LinkedHashMap<>());

        eatMedicine.setMatcher(matcher);

        MatcherAndActionConfigEntry action = new MatcherAndActionConfigEntry();

        action.setType("Command");

        action.setExpression(
                "eat yao\n"
                        + "drink shui");

        action.setParams(
                new LinkedHashMap<>());

        eatMedicine.setAction(action);

        listModel.addElement(
                eatMedicine);

        // -----------------------------------------------------
        // Fullme
        // -----------------------------------------------------

        TriggerConfigEntry fullme = new TriggerConfigEntry();

        fullme.setName("Fullme 验证码");
        fullme.setType("Inbound");
        fullme.setRemainingCount(1);

        fullme.setSync(false);
        fullme.setUnique(true);
        fullme.setAutoRegister(true);

        MatcherAndActionConfigEntry fullmeMatcher = new MatcherAndActionConfigEntry();

        fullmeMatcher.setType("Include");

        fullmeMatcher.setExpression(
                "请点击以下链接或者输入验证码");

        fullmeMatcher.setParams(
                new LinkedHashMap<>());

        fullme.setMatcher(
                fullmeMatcher);

        MatcherAndActionConfigEntry fullmeAction = new MatcherAndActionConfigEntry();

        fullmeAction.setType("Java");

        fullmeAction.setExpression(
                "FullmeShowAction");

        fullmeAction.setParams(
                new LinkedHashMap<>());

        fullme.setAction(
                fullmeAction);

        listModel.addElement(
                fullme);

        // 默认选中第一个

        if (!listModel.isEmpty()) {

            triggerList.setSelectedIndex(0);
        }
    }
}
