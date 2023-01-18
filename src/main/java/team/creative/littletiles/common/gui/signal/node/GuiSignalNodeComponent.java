package team.creative.littletiles.common.gui.signal.node;

import team.creative.littletiles.common.gui.signal.GuiSignalComponent;

public abstract class GuiSignalNodeComponent extends GuiSignalNode {
    
    public final String underline;
    public final GuiSignalComponent component;
    
    public GuiSignalNodeComponent(GuiSignalComponent component) {
        super(component.name());
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
