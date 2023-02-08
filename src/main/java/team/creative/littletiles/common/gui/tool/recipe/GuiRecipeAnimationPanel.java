package team.creative.littletiles.common.gui.tool.recipe;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer;

public class GuiRecipeAnimationPanel extends GuiParent {
    
    public GuiRecipeAnimationPanel(GuiRecipeAnimationStorage storage) {
        super(GuiFlow.STACK_Y);
        setExpandable();
        
        GuiAnimationViewer viewer = new GuiAnimationViewer("viewer", storage);
        add(viewer.setExpandable());
        
        GuiParent animationButtons = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(animationButtons.setExpandableX());
        
        animationButtons.add(new GuiIconButton("perspective", GuiIcon.CAMERA, x -> {}).setTooltip(new TextBuilder().translate("gui.recipe.perspective").build()));
        animationButtons.add(new GuiIconButton("home", GuiIcon.HOUSE, x -> {
            viewer.distance.set(storage.longestSide() / 2D + 2);
            viewer.offX.set(0);
            viewer.offY.set(0);
            viewer.offZ.set(0);
            viewer.rotX.set(0);
            viewer.rotY.set(0);
            viewer.rotZ.set(0);
        }).setTooltip(new TextBuilder().translate("gui.recipe.home").build()));
    }
    
}
