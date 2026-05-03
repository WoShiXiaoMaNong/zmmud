package zm.mud.ui.component;

import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import zm.mud.ui.ZmMudUI;
import zm.mud.ui.util.AnsiToStyleDocUtil;


public class MudTextAare extends JTextPane {
    private static final org.apache.logging.log4j.Logger logger = org.apache.logging.log4j.LogManager
            .getLogger(MudTextAare.class);

    private static final Font DEFAUL_FONT = new Font("Consolas", Font.PLAIN, 14);
    private StyledDocument doc;
    private AnsiToStyleDocUtil ansiToStyleDocUtil;
    private Font font;

    public MudTextAare() {
  
        this(null);
    }

    public MudTextAare(Font f) {
        if(f != null){
            this.font = f;
            this.setFont(this.font);
        }else{
            this.font = DEFAUL_FONT;
        }
        this.setEditable(false);
        
        this.setParagraphAttributes(this.getParagraphAttributes(), true);
        this.doc = this.getStyledDocument();
        this.ansiToStyleDocUtil = ZmMudUI.getContext().getBean(AnsiToStyleDocUtil.class);
    }


    @Override
    public void setFont(Font f){
        this.font = f;
        super.setFont(f);
    }

    public void printlnToScreen(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                ansiToStyleDocUtil.parseAnsiToStyledDocument(text + "\r\n", doc,this.font);
                this.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                logger.error("Failed to print to screen", e);
            }
        });
    }
       
}
