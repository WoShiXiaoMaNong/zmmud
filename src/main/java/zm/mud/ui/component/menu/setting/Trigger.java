package zm.mud.ui.component.menu.setting;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import zm.mud.core.automation.trigger.TriggerFactory;
import zm.mud.core.automation.trigger.cfg.MatcherAndActionConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerConfigEntry;
import zm.mud.core.automation.trigger.cfg.TriggerType;
import zm.mud.ui.component.menu.AbsZmMudDialog;
import zm.mud.ui.component.menu.KeyValuePair;
import zm.mud.utils.SpringBeanUtil;

public class Trigger extends AbsZmMudDialog {

        private static final Logger logger = LogManager.getLogger(Trigger.class);

        // =========================================================
        // MUD World
        // =========================================================

        /**
         * MUD World 下拉框
         *
         * Key = mudWorldCode
         * Value = MUD World 显示名称
         */
        private JComboBox<KeyValuePair<String, String>> cbMudWorld;

        /**
         * 当前选择的 MUD World Code
         */
        private String currentMudWorldCode;

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

        private JComboBox<KeyValuePair<String, String>> cbTriggerType;

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

        private JComboBox<KeyValuePair<String, String>> cbActionType;

        private JTextArea taActionExpression;

        private JPanel actionParamsPanel;

        // =========================================================
        // Trigger 选项
        // =========================================================

        private JCheckBox chkSync;

        private JCheckBox chkUnique;

        private JCheckBox chkAutoRegister;

     
        // =========================================================
        // Constructor
        // =========================================================

        public Trigger(Frame owner, String title) {

                super(owner, title);

                this.setSize(950, 550);

        }

        // =========================================================
        // Main UI
        // =========================================================

        @Override
        protected JPanel getContentPanelUi() {

                JPanel mainPanel = new JPanel(
                                new BorderLayout(
                                                10,
                                                10));

                mainPanel.setBorder(
                                BorderFactory.createEmptyBorder(
                                                10,
                                                10,
                                                10,
                                                10));

                // -----------------------------------------------------
                // 顶部 MUD World
                // -----------------------------------------------------

                mainPanel.add(
                                createMudWorldPanel(),
                                BorderLayout.NORTH);

                // -----------------------------------------------------
                // 中间内容
                // -----------------------------------------------------

                JPanel contentPanel = new JPanel(
                                new BorderLayout(
                                                10,
                                                10));

                contentPanel.add(
                                createLeftPanel(),
                                BorderLayout.WEST);

                contentPanel.add(
                                createRightPanel(),
                                BorderLayout.CENTER);

                mainPanel.add(
                                contentPanel,
                                BorderLayout.CENTER);

                /*
                 * 注意：
                 *
                 * 初始化时不加载 Trigger 数据。
                 *
                 * 原来的：
                 *
                 * loadTriggerConfigData();
                 *
                 * 已经删除。
                 *
                 * 只有选择 MUD World 后才加载。
                 */

                return mainPanel;
        }

        // =========================================================
        // MUD World
        // =========================================================

        private JPanel createMudWorldPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                8,
                                                5));

                panel.setBorder(
                                BorderFactory.createTitledBorder(
                                                "MUD 配置"));

                panel.add(
                                new JLabel("选择配置"),
                                BorderLayout.WEST);

                cbMudWorld = new JComboBox<>();

                /*
                 * 加载 MUD World 列表。
                 *
                 * 注意：
                 * 这里只加载 World 列表，
                 * 不加载 Trigger。
                 */
                loadMudWorlds();

                /*
                 * World 选择变化时加载对应 Trigger。
                 */
                cbMudWorld.addActionListener(
                                e -> onMudWorldChanged());

                panel.add(
                                cbMudWorld,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // 加载 MUD World 列表
        // =========================================================

        private void loadMudWorlds() {

                cbMudWorld.removeAllItems();

                /*
                 * key = mudWorldCode
                 * value = MUD World 名称
                 */
                List<KeyValuePair<String, String>> mudWorlds = TriggerService.getMudWorlds();

                if (mudWorlds == null
                                || mudWorlds.isEmpty()) {

                        return;
                }

                for (KeyValuePair<String, String> mudWorld : mudWorlds) {

                        cbMudWorld.addItem(
                                        mudWorld);
                }

                /*
                 * 初始化时不要主动选择第一个。
                 *
                 * 如果自动选择第一个，
                 * JComboBox 会触发 ActionListener，
                 * 从而提前加载 Trigger。
                 */
                cbMudWorld.setSelectedIndex(-1);
        }

        // =========================================================
        // MUD World 改变
        // =========================================================

        private void onMudWorldChanged() {

                KeyValuePair<String, String> selectedWorld = (KeyValuePair<String, String>) cbMudWorld
                                .getSelectedItem();

                if (selectedWorld == null) {
                        return;
                }

                String mudWorldCode = selectedWorld.getKey();

                if (mudWorldCode == null
                                || mudWorldCode.trim().isEmpty()) {

                        return;
                }

                /*
                 * 如果选择的还是当前 World，
                 * 不需要重新加载。
                 */
                if (mudWorldCode.equals(
                                currentMudWorldCode)) {

                        return;
                }

                /*
                 * 如果之前有正在编辑的 Trigger，
                 * 先把 UI 数据保存回 POJO。
                 */
                saveCurrentTrigger();

                /*
                 * 更新当前 World。
                 */
                currentMudWorldCode = mudWorldCode;

                /*
                 * 根据 mudWorldCode 加载 Trigger。
                 */
                loadTriggerConfigData(
                                mudWorldCode);
        }

        // =========================================================
        // 保存当前 Trigger
        // =========================================================

        private void saveCurrentTrigger() {

                if (currentTrigger == null) {
                        return;
                }

                saveFormToConfig(
                                currentTrigger);
        }

        // =========================================================
        // 左侧 Trigger
        // =========================================================

        private JPanel createLeftPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                panel.setBorder(
                                BorderFactory.createTitledBorder(
                                                "触发器列表"));                               
                panel.setPreferredSize(
                                new Dimension(
                                                180,
                                                0));

                listModel = new DefaultListModel<>();

                triggerList = new JList<>(
                                listModel);

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
                                new JScrollPane(
                                                triggerList),
                                BorderLayout.CENTER);

                // -----------------------------------------------------
                // Buttons
                // -----------------------------------------------------

                JPanel buttonPanel = new JPanel(
                                new GridLayout(
                                                1,
                                                2,
                                                5,
                                                5));

                btnAdd = new JButton("添加");

                btnDelete = new JButton("删除");

                btnAdd.addActionListener(
                                e -> addTrigger());

                btnDelete.addActionListener(
                                e -> deleteTrigger());

                buttonPanel.add(
                                btnAdd);

                buttonPanel.add(
                                btnDelete);

                panel.add(
                                buttonPanel,
                                BorderLayout.SOUTH);

                return panel;
        }

        // =========================================================
        // 右侧
        // =========================================================

        private JPanel createRightPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                8,
                                                8));

                // -----------------------------------------------------
                // 基础配置
                // -----------------------------------------------------

                JPanel basicPanel = new JPanel(
                                new GridBagLayout());

                basicPanel.setBorder(
                                BorderFactory.createTitledBorder(
                                                "Trigger 基础配置"));

                GridBagConstraints gbc = createGbc();

                // -----------------------------------------------------
                // Name
                // -----------------------------------------------------

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

                // -----------------------------------------------------
                // Trigger Type
                // -----------------------------------------------------

                addLabel(
                                basicPanel,
                                gbc,
                                0,
                                1,
                                "Trigger 类型:");

                cbTriggerType = new JComboBox<>();

                for (KeyValuePair<String, String> triggerType : TriggerService.getTriggerTypes()) {

                        cbTriggerType.addItem(
                                        triggerType);
                }

                addComponent(
                                basicPanel,
                                gbc,
                                1,
                                1,
                                cbTriggerType);

                // -----------------------------------------------------
                // Remaining Count
                // -----------------------------------------------------

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
                // Options
                // -----------------------------------------------------

                addLabel(
                                basicPanel,
                                gbc,
                                0,
                                3,
                                "选项:");

                JPanel optionsPanel = new JPanel(
                                new FlowLayout(
                                                FlowLayout.LEFT,
                                                5,
                                                0));

                chkSync = new JCheckBox("Sync");

                chkUnique = new JCheckBox("Unique");

                chkAutoRegister = new JCheckBox(
                                "Auto Register");

                optionsPanel.add(
                                chkSync);

                optionsPanel.add(
                                chkUnique);

                optionsPanel.add(
                                chkAutoRegister);

                addComponent(
                                basicPanel,
                                gbc,
                                1,
                                3,
                                optionsPanel);

                // -----------------------------------------------------
                // Matcher
                // -----------------------------------------------------

                JPanel matcherPanel = createMatcherPanel();

                // -----------------------------------------------------
                // Action
                // -----------------------------------------------------

                JPanel actionPanel = createActionPanel();

                // -----------------------------------------------------
                // Top
                // -----------------------------------------------------

                JPanel topPanel = new JPanel(
                                new GridLayout(
                                                1,
                                                2,
                                                8,
                                                8));

                topPanel.add(
                                basicPanel);

                topPanel.add(
                                matcherPanel);

                panel.add(
                                topPanel,
                                BorderLayout.NORTH);

                // -----------------------------------------------------
                // Action
                // -----------------------------------------------------

                panel.add(
                                actionPanel,
                                BorderLayout.CENTER);

                return panel;
        }

        // =========================================================
        // Matcher UI
        // =========================================================

        private JPanel createMatcherPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                panel.setBorder(
                                BorderFactory.createTitledBorder(
                                                "Matcher 匹配器"));

                JPanel top = new JPanel(
                                new GridBagLayout());

                GridBagConstraints gbc = createGbc();

                addLabel(
                                top,
                                gbc,
                                0,
                                0,
                                "类型:");

                cbMatcherType = new JComboBox<>();

                for (KeyValuePair<String, String> matcherItem : TriggerService.getMathers()) {

                        cbMatcherType.addItem(
                                        matcherItem);
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

                // -----------------------------------------------------
                // Expression
                // -----------------------------------------------------

                JPanel expressionPanel = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                expressionPanel.add(
                                new JLabel(
                                                "Expression 表达式:"),
                                BorderLayout.NORTH);

                taMatcherExpression = new JTextArea(
                                4,
                                30);

                taMatcherExpression.setLineWrap(
                                false);

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

                // -----------------------------------------------------
                // Params
                // -----------------------------------------------------

                // matcherParamsPanel = new JPanel();

                // matcherParamsPanel.setLayout(
                //                 new BoxLayout(
                //                                 matcherParamsPanel,
                //                                 BoxLayout.Y_AXIS));

                // matcherParamsPanel.setBorder(
                //                 BorderFactory.createTitledBorder(
                //                                 "Params"));

                // panel.add(
                //                 matcherParamsPanel,
                //                 BorderLayout.SOUTH);

                cbMatcherType.addActionListener(
                                e -> rebuildMatcherParams());

                return panel;
        }

        // =========================================================
        // Action UI
        // =========================================================

        private JPanel createActionPanel() {

                JPanel panel = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                panel.setBorder(
                                BorderFactory.createTitledBorder(
                                                "Action 动作器"));

                JPanel top = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                top.add(
                                new JLabel("类型:"),
                                BorderLayout.WEST);

                List<KeyValuePair<String, String>> actionTypes = TriggerService.getActionTypes();

                cbActionType = new JComboBox<>();

                for (KeyValuePair<String, String> actionType : actionTypes) {

                        cbActionType.addItem(
                                        actionType);
                }

                top.add(
                                cbActionType,
                                BorderLayout.CENTER);

                panel.add(
                                top,
                                BorderLayout.NORTH);

                // -----------------------------------------------------
                // Expression
                // -----------------------------------------------------

                JPanel expressionPanel = new JPanel(
                                new BorderLayout(
                                                5,
                                                5));

                expressionPanel.add(
                                new JLabel(
                                                "Expression表达式:"),
                                BorderLayout.NORTH);

                taActionExpression = new JTextArea();

                taActionExpression.setLineWrap(
                                false);

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

                // -----------------------------------------------------
                // Params
                // -----------------------------------------------------

                // actionParamsPanel = new JPanel();

                // actionParamsPanel.setLayout(
                //                 new BoxLayout(
                //                                 actionParamsPanel,
                //                                 BoxLayout.Y_AXIS));

                // actionParamsPanel.setBorder(
                //                 BorderFactory.createTitledBorder(
                //                                 "Params"));

                // panel.add(
                //                 actionParamsPanel,
                //                 BorderLayout.SOUTH);

                cbActionType.addActionListener(
                                e -> rebuildActionParams());

                return panel;
        }

        // =========================================================
        // Matcher Params
        // =========================================================

        private void rebuildMatcherParams() {

                // matcherParamsPanel.removeAll();

                // KeyValuePair<String, String> selectedItem = (KeyValuePair<String, String>) cbMatcherType
                //                 .getSelectedItem();

                // if (selectedItem == null) {

                //         matcherParamsPanel.revalidate();
                //         matcherParamsPanel.repaint();

                //         return;
                // }

                // String type = selectedItem.getKey();

                // if ("Regex".equals(type)) {

                //         JCheckBox ignoreCase = new JCheckBox(
                //                         "ignoreCase");

                //         matcherParamsPanel.add(
                //                         ignoreCase);
                // }

                // matcherParamsPanel.revalidate();

                // matcherParamsPanel.repaint();
        }

        // =========================================================
        // Action Params
        // =========================================================

        private void rebuildActionParams() {

                // actionParamsPanel.removeAll();

                // String type = null;

                // KeyValuePair<String, String> selectedActionType = (KeyValuePair<String, String>) cbActionType
                //                 .getSelectedItem();

                // if (selectedActionType != null) {

                //         type = selectedActionType.getKey();
                // }

                // if ("Lua".equals(type)) {

                //         JTextField timeout = new JTextField();

                //         JPanel row = new JPanel(
                //                         new BorderLayout(
                //                                         5,
                //                                         5));

                //         row.add(
                //                         new JLabel(
                //                                         "timeout:"),
                //                         BorderLayout.WEST);

                //         row.add(
                //                         timeout,
                //                         BorderLayout.CENTER);

                //         actionParamsPanel.add(
                //                         row);
                // }

                // actionParamsPanel.revalidate();

                // actionParamsPanel.repaint();
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

                /*
                 * 保存之前正在编辑的 Trigger
                 */
                if (currentTrigger != null
                                && currentTrigger != selected) {

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
                                TriggerService.getTriggerType(
                                                config.getType()));

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
                                        TriggerService.getMatcherType(
                                                        matcher.getType()));

                        taMatcherExpression.setText(
                                        nullToEmpty(
                                                        matcher.getExpression()));

                } else {

                        if (cbMatcherType.getItemCount() > 0) {

                                cbMatcherType.setSelectedIndex(
                                                0);
                        }

                        taMatcherExpression.setText("");
                }

                // -----------------------------------------------------
                // Action
                // -----------------------------------------------------

                MatcherAndActionConfigEntry action = config.getAction();

                if (action != null) {

                        cbActionType.setSelectedItem(
                                        TriggerService.getActionType(
                                                        action.getType()));

                        taActionExpression.setText(
                                        nullToEmpty(
                                                        action.getExpression()));

                } else {

                        if (cbActionType.getItemCount() > 0) {

                                cbActionType.setSelectedIndex(
                                                0);
                        }

                        taActionExpression.setText("");
                }

                // -----------------------------------------------------
                // 重建参数 UI
                // -----------------------------------------------------

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

                KeyValuePair<String, String> selectedTriggerType = (KeyValuePair<String, String>) cbTriggerType
                                .getSelectedItem();

                if (selectedTriggerType != null) {

                        config.setType(
                                        selectedTriggerType.getKey());
                }

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

                        config.setMatcher(
                                        matcher);
                }

                KeyValuePair<String, String> selectedMatcherType = (KeyValuePair<String, String>) cbMatcherType
                                .getSelectedItem();

                if (selectedMatcherType != null) {

                        matcher.setType(
                                        selectedMatcherType.getKey());
                }

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

                        config.setAction(
                                        action);
                }

                KeyValuePair<String, String> selectedActionType = (KeyValuePair<String, String>) cbActionType
                                .getSelectedItem();

                if (selectedActionType != null) {

                        action.setType(
                                        selectedActionType.getKey());
                }

                action.setExpression(
                                taActionExpression.getText());

                action.setParams(
                                collectActionParams());
        }

        // =========================================================
        // Matcher Params
        // =========================================================

        private Map<String, Object> collectMatcherParams() {

                Map<String, Object> params = new LinkedHashMap<>();

                KeyValuePair<String, String> selectedItem = (KeyValuePair<String, String>) cbMatcherType
                                .getSelectedItem();

                if (selectedItem == null) {
                        return params;
                }

                String type = selectedItem.getKey();

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

        // =========================================================
        // Action Params
        // =========================================================

        private Map<String, Object> collectActionParams() {

                Map<String, Object> params = new LinkedHashMap<>();

                KeyValuePair<String, String> selectedActionType = (KeyValuePair<String, String>) cbActionType
                                .getSelectedItem();

                String type = selectedActionType == null
                                ? null
                                : selectedActionType.getKey();

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
        // 添加 Trigger
        // =========================================================

        private void addTrigger() {

                /*
                 * 必须先选择 MUD World。
                 */
                if (currentMudWorldCode == null
                                || currentMudWorldCode.trim().isEmpty()) {

                        JOptionPane.showMessageDialog(
                                        this,
                                        "请先选择 MUD World。",
                                        "提示",
                                        JOptionPane.INFORMATION_MESSAGE);

                        return;
                }

                TriggerConfigEntry config = new TriggerConfigEntry();

                config.setName(
                                "新 Trigger");

                config.setType(
                                TriggerType.INBOUNG_TRIGGER.getType());

                config.setRemainingCount(
                                0);

                config.setSync(
                                false);

                config.setUnique(
                                false);

                config.setAutoRegister(
                                false);

                // -----------------------------------------------------
                // 默认 Matcher
                // -----------------------------------------------------

                MatcherAndActionConfigEntry matcher = new MatcherAndActionConfigEntry();

                matcher.setType(
                                "Regex");

                matcher.setExpression(
                                "");

                matcher.setParams(
                                new LinkedHashMap<>());

                config.setMatcher(
                                matcher);

                // -----------------------------------------------------
                // 默认 Action
                // -----------------------------------------------------

                MatcherAndActionConfigEntry action = new MatcherAndActionConfigEntry();

                action.setType(
                                "Command");

                action.setExpression(
                                "");

                action.setParams(
                                new LinkedHashMap<>());

                config.setAction(
                                action);

                // -----------------------------------------------------
                // 添加到列表
                // -----------------------------------------------------

                listModel.addElement(
                                config);

                triggerList.setSelectedValue(
                                config,
                                true);
        }

        // =========================================================
        // 删除 Trigger
        // =========================================================

        private void deleteTrigger() {

                int index = triggerList.getSelectedIndex();

                if (index < 0) {
                        return;
                }

                TriggerConfigEntry config = listModel.getElementAt(
                                index);

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

                listModel.remove(
                                index);

                currentTrigger = null;

                if (!listModel.isEmpty()) {

                        triggerList.setSelectedIndex(
                                        Math.min(
                                                        index,
                                                        listModel.size() - 1));

                } else {

                        clearForm();
                }
        }

        // =========================================================
        // OK
        // =========================================================

        @Override
        protected void ok() {

                /*
                 * 没有选择 MUD World，
                 * 不保存任何 Trigger。
                 */
                if (currentMudWorldCode == null
                                || currentMudWorldCode.trim().isEmpty()) {

                        return;
                }

                /*
                 * 保存当前正在编辑的 Trigger。
                 */
                saveCurrentTrigger();

                List<TriggerConfigEntry> configs = new ArrayList<>();

                for (int i = 0; i < listModel.size(); i++) {

                        configs.add(
                                        listModel.getElementAt(i));
                }

                TriggerFactory factory = SpringBeanUtil.getBean(
                                TriggerFactory.class);

                factory.save(this.currentMudWorldCode, configs);
        }

        // =========================================================
        // 根据 mudWorldCode 加载 Trigger
        // =========================================================

        private void loadTriggerConfigData(
                        String mudWorldCode) {

                /*
                 * 先清空旧 World 的 Trigger。
                 */
                listModel.clear();

                currentTrigger = null;

                /*
                 * 根据 mudWorldCode 加载。
                 */
                List<TriggerConfigEntry> configs = TriggerService.getTriggerConfigEntries(
                                mudWorldCode);

                if (configs == null
                                || configs.isEmpty()) {

                        clearForm();

                        return;
                }

                /*
                 * 加入列表。
                 */
                for (TriggerConfigEntry entry : configs) {

                        listModel.addElement(
                                        entry);
                }

                /*
                 * 默认选中第一个。
                 */
                triggerList.setSelectedIndex(
                                0);
        }

        // =========================================================
        // 清空右侧 Form
        // =========================================================

        private void clearForm() {

                currentTrigger = null;

                // -----------------------------------------------------
                // Basic
                // -----------------------------------------------------

                txtName.setText("");

                if (cbTriggerType.getItemCount() > 0) {

                        cbTriggerType.setSelectedIndex(
                                        0);
                }

                spRemainingCount.setValue(
                                0);

                // -----------------------------------------------------
                // Options
                // -----------------------------------------------------

                chkSync.setSelected(
                                false);

                chkUnique.setSelected(
                                false);

                chkAutoRegister.setSelected(
                                false);

                // -----------------------------------------------------
                // Matcher
                // -----------------------------------------------------

                if (cbMatcherType.getItemCount() > 0) {

                        cbMatcherType.setSelectedIndex(
                                        0);
                }

                taMatcherExpression.setText(
                                "");

                // -----------------------------------------------------
                // Action
                // -----------------------------------------------------

                if (cbActionType.getItemCount() > 0) {

                        cbActionType.setSelectedIndex(
                                        0);
                }

                taActionExpression.setText(
                                "");

                // -----------------------------------------------------
                // Params
                // -----------------------------------------------------

                rebuildMatcherParams();

                rebuildActionParams();
        }

        // =========================================================
        // Utils
        // =========================================================

        private GridBagConstraints createGbc() {

                GridBagConstraints gbc = new GridBagConstraints();

                gbc.insets = new Insets(
                                4,
                                6,
                                4,
                                6);

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
}
