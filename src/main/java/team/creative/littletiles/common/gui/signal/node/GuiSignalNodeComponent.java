package team.creative.littletiles.common.gui.signal.node;

import javax.annotation.Nullable;

import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition.SignalPosition;

public abstract class GuiSignalNodeComponent extends GuiSignalNode {
    
    public final String underline;
    public final GuiSignalComponent component;
    
    public GuiSignalNodeComponent(GuiSignalComponent component, @Nullable SignalPosition position) {
        super(component.name(), position);
        this.component = component;
        this.underline = component.name().equals(component.totalName()) ? null : component.totalName();
    }
    
    @Override
    public boolean hasUnderline() {
        return underline != null;
    }
    
    @Override
    public String getUnderline() {
        return underline;
    }
    
}
