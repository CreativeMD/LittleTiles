package team.creative.littletiles.common.gui.controls.animation;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationPanel extends GuiParent {
    
    public GuiIsoAnimationPanel(GuiTreeItemStructure item, LittleBox box, LittleGrid grid, boolean even) {
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        GuiIsoAnimationViewer viewer = new GuiIsoAnimationViewer("viewer", item, box, grid, even);
        add(viewer);
        GuiParent buttons = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(buttons);
        buttons.add(new GuiButtonIcon("reset", Icon.HOUSE, x -> viewer.resetView()));
        buttons.add(new GuiButtonIcon("axis", Icon.COORDS, x -> viewer.nextAxis()));
        buttons.add(new GuiButtonIcon("flip", Icon.MIRROR, x -> viewer.mirrorView()));
    }
    
    public GuiIsoAnimationPanel setVisibleAxis(boolean visible) {
        get("viewer", GuiIsoAnimationViewer.class).visibleAxis = visible;
        return this;
    }
    
    public GuiIsoAnimationPanel setViewerDim(int width, int height) {
        get("viewer", GuiIsoAnimationViewer.class).setDim(width, height);
        return this;
    }
    
}
