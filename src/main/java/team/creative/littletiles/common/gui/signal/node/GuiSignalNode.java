package team.creative.littletiles.common.gui.signal.node;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.gui.signal.GuiSignalNodeAnchor;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public abstract class GuiSignalNode extends GuiParent {
    
    private int x = -1;
    private int y = -1;
    private boolean added = false;
    
    public GuiButton button;
    
    public GuiSignalNode(String caption, @Nullable SignalPosition position) {
        super();
        if (position != null) {
            x = position.x();
            y = position.y();
        }
        
        beforeAddingButton();
        add(button = new GuiButton(caption, x -> {
            GuiSignalController controller = controller();
            if (x == 1 && removable()) {
                controller.removeNode(GuiSignalNode.this);
                return;
            }
            
            controller.selectOrConnect(GuiSignalNode.this, from -> buttonAnchor(from), x == 0);
        }));
        button.setTitle(Component.literal(caption));
        flow = GuiFlow.STACK_Y;
        align = Align.CENTER;
    }
    
    protected GuiSignalNodeAnchor buttonAnchor(boolean from) {
        return null;
    }
    
    protected void beforeAddingButton() {}
    
    public boolean hasUnderline() {
        return false;
    }
    
    public String getUnderline() {
        return null;
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    public SignalPosition position() {
        return new SignalPosition(x, y);
    }
    
    public void updatePosition(int col, int row) {
        this.x = col;
        this.y = row;
        added = true;
    }
    
    public boolean added() {
        return added;
    }
    
    public GuiSignalController controller() {
        return (GuiSignalController) getParent();
    }
    
    public void reset() {
        setTooltip((List) null);
        button.setDefaultColor(ColorUtils.WHITE);
    }
    
    public void setError(String error) {
        setTooltip(error);
        button.setDefaultColor(ColorUtils.RED);
    }
    
    @Override
    public boolean testForDoubleClick(double x, double y, int button) {
        return button != 1;
    }
    
    public boolean removable() {
        return true;
    }
    
    public abstract SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException;
    
    public abstract void disconnect(GuiSignalConnection connection);
    
    public abstract GuiSignalConnection getConnectionTo(GuiSignalNode node);
    
    public abstract boolean canConnectTo(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor);
    
    public abstract boolean canConnectFrom(GuiSignalNode node, @Nullable GuiSignalNodeAnchor anchor);
    
    public abstract void connect(GuiSignalConnection connection);
    
    public abstract void remove();
    
    public abstract Iterable<GuiSignalConnection> toConnections();
    
    public GuiSignalNodeAnchor connectionAnchor(boolean from, GuiSignalNode other) {
        return from ? GuiSignalNodeAnchor.RIGHT : GuiSignalNodeAnchor.LEFT;
    }
    
    public void setDefaultColor(int white) {
        button.setDefaultColor(white);
    }
    
}