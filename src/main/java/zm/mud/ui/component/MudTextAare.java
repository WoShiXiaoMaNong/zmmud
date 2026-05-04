package zm.mud.ui.component;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import zm.mud.ui.ZmMudUI;
import zm.mud.ui.cfg.GlobleCfg;
import zm.mud.ui.util.AnsiToStyleDocUtil;


public class MudTextAare extends JTextPane {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudTextAare.class);

    private static final int MAX_LINES = 100;

    private StyledDocument doc;
    private AnsiToStyleDocUtil ansiToStyleDocUtil;

    private GlobleCfg globleCfg;

    public MudTextAare(GlobleCfg cfg) {
        this.globleCfg = cfg;
        this.setEditable(false);
        this.setBackground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setForeground(this.globleCfg.getThemeType().getTheme().getDefaultBackground());
        this.setParagraphAttributes(this.getParagraphAttributes(), true);
        this.doc = this.getStyledDocument();
        this.ansiToStyleDocUtil = ZmMudUI.getContext().getBean(AnsiToStyleDocUtil.class);
        logger.info("displayBufLineNumber :" + MAX_LINES);
    }



    public void printlnToScreen(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                ansiToStyleDocUtil.parseAnsiToStyledDocument(text + "\r\n", doc,this.globleCfg.getFont(), this.globleCfg.getThemeType().getTheme());
                trimLines();
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print to screen", e);
            }
        });
    }

    private void trimLines() throws BadLocationException {
        javax.swing.text.Element root = doc.getDefaultRootElement();
        int lineCount = root.getElementCount();

        if (lineCount <= MAX_LINES) {
            return;
        }

        // 需要删除的行数
        int linesToRemove = lineCount - MAX_LINES;

        // 找到第 N 行的结束位置
        javax.swing.text.Element lineElement = root.getElement(linesToRemove - 1);
        int endOffset = lineElement.getEndOffset();

        // 删除从开头到这个位置的内容
        doc.remove(0, endOffset);
    }
       
}
