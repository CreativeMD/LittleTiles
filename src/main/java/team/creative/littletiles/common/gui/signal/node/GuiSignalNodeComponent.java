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
    
    public boolean hasUnderline() {
        return underline != null;
    }
}
