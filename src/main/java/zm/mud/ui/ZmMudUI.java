package zm.mud.ui;

import zm.mud.core.api.InbMsgService;
import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.ui.component.MudMainScreen;
import zm.mud.ui.component.MudTextAare;
import zm.mud.ui.processor.MsgPrintProcessor;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ZmMudUI {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(ZmMudUI.class);

    @Autowired
    private GlobleCfg globleCfg;

    @Autowired
    private MsgPrintProcessor msgPinter;

    private static ApplicationContext context;

    private MudMainScreen mudMain;

    @Autowired
    private InbMsgService inbMsgService;

    private static final ThreadPoolExecutor uiThreadPool = new ThreadPoolExecutor(
            1, 3, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1024),
            r -> {
                Thread t = new Thread(r, "fullme-thread");
                t.setDaemon(true); // 强烈建议：客户端退出时，这些线程会自动销毁
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    @PostConstruct
    public void init() {
        mudMain = new MudMainScreen(globleCfg, this);
        this.registerFont();
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            mudMain.setShow();
            mudMain.resetFont(this.globleCfg.getFontName(), this.globleCfg.getFontSize());
            inbMsgService.registerMsgHandler(msgPinter);
        });
    }

    public int getMsgOffset(String msg) {
        return this.mudMain.getMsgOffset(msg);
    }

    public void printlnToScreen(String text) {
        this.mudMain.printlnToScreen(text, false);
    }

    public void printlnToScreen(String text, boolean enableBlod) {
        this.mudMain.printlnToScreen(text, enableBlod);
    }

    /**
     * @see MudTextAare#printImg(String, int)
     * @param imgUrl
     * @param offset
     */
    public void printImg(String imgUrl, int offset, boolean insertMode) {
        uiThreadPool.execute(() -> {
            mudMain.printImg(imgUrl, offset, insertMode);
        });
    }

    @Autowired
    public void setContext(ApplicationContext aContext) {
        context = aContext;
    }

    public static ApplicationContext getContext() {
        return context;
    }

    private void registerFont() {
        // 1. 定義 resources/mono/ 下所有的字型檔名清單（精準匹配，避開 Jar 包無法遍歷的問題）
        List<String> fontFiles = Arrays.asList(
                "sarasa-mono.ttf" // 請替換成你實際的檔名
        );

        // 2. 獲取 JVM 本地圖形環境
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        // 3. 循環載入並註冊每一個字型
        for (String fileName : fontFiles) {
            String resourcePath = "/fonts/" + fileName; // 注意你的路徑對應 resources/mono/

            try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    System.err.println("找不到字型資源檔: " + resourcePath);
                    continue;
                }

                // 載入字型
                Font baseFont = Font.createFont(Font.TRUETYPE_FONT, is);

                // 註冊字型
                boolean success = ge.registerFont(baseFont);

                if (success) {
                    // 打印邏輯名稱與家族名稱，方便你後續在代碼中調用
                    logger.info("成功註冊字型 -> Name: " + baseFont.getName() + " | Family: " + baseFont.getFamily());
                } else {
                    logger.error("字型已存在或註冊失敗: " + fileName);
                }

            } catch (Exception e) {
                logger.error("載入字型失敗 [" + fileName + "] ",e);
            }
        }
    }

}