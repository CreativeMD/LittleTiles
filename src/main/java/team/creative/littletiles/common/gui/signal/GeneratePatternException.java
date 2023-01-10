package team.creative.littletiles.common.gui.signal;

import team.creative.littletiles.common.gui.signal.node.GuiSignalNode;

public class GeneratePatternException extends Exception {
    
    public final GuiSignalNode node;
    
    public GeneratePatternException(GuiSignalNode node, String msg) {
        super("exception.pattern." + msg);
        this.node = node;
        this.node.setError(getMessage());
    }
}
