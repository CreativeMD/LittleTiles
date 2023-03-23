package team.creative.littletiles.common.gui.controls.animation;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.math.box.LittleBox;

public class GuiIsoAnimationPanel extends GuiParent {
    
    public GuiIsoAnimationPanel(GuiTreeItemStructure item, LittleBox box, LittleGrid grid) {
        flow = GuiFlow.STACK_Y;
        align = Align.STRETCH;
        GuiIsoAnimationViewer viewer = new GuiIsoAnimationViewer("viewer", item, box, grid);
        add(viewer.setDim(200, 200));
        GuiParent buttons = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(buttons);
        buttons.add(new GuiIconButton("reset", GuiIcon.HOUSE, x -> viewer.resetView()));
        buttons.add(new GuiIconButton("axis", GuiIcon.COORDS, x -> viewer.nextAxis()));
        buttons.add(new GuiIconButton("flip", GuiIcon.MIRROR, x -> viewer.mirrorView()));
    }
    
    public GuiIsoAnimationPanel setVisibleAxis(boolean visible) {
        get("viewer", GuiIsoAnimationViewer.class).visibleAxis = visible;
        return this;
    }
    
}
