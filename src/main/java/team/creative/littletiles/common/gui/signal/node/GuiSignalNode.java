package team.creative.littletiles.common.gui.signal.node;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.util.math.geo.Rect;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalConnection;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;

public abstract class GuiSignalNode extends GuiButton implements Iterable<GuiSignalConnection> {
    
    private int x;
    private int y;
    private boolean added = false;
    
    public GuiSignalNode(String caption) {
        super(caption, null);
        pressed = x -> {
            GuiSignalController controller = controller();
            if (x == 1 && removable()) {
                controller.removeNode(this);
                return;
            }
            
            if (controller.selected() != null)
                controller.tryConnectSelectedTo(this);
            else if (Minecraft.getInstance().mouseHandler.isLeftPressed())
                controller.drag(this);
            else
                controller.select(this);
        };
        setTitle(Component.literal(caption));
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
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
        setTooltip(null);
        setDefaultColor(ColorUtils.WHITE);
    }
    
    public void setError(String error) {
        setTooltip(new TextBuilder().translate(error).build());
        setDefaultColor(ColorUtils.RED);
    }
    
    @Override
    public boolean testForDoubleClick(Rect rect, double x, double y) {
        return true;
    }
    
    public boolean removable() {
        return true;
    }
    
    public abstract SignalInputCondition generateCondition(List<GuiSignalNode> processed) throws GeneratePatternException;
    
    public abstract void removeConnection(GuiSignalConnection connection);
    
    public abstract boolean canConnectTo(GuiSignalNode node);
    
    public abstract boolean canConnectFrom(GuiSignalNode node);
    
    public abstract void connect(GuiSignalConnection connection);
    
    public abstract void remove();
    
    public abstract int indexOf(GuiSignalConnection connection);
    
    public abstract Iterable<GuiSignalConnection> toConnections();
    
}