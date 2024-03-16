package team.creative.littletiles.common.gui.controls.animation;

import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.controls.simple.GuiButtonIcon;
import team.creative.creativecore.common.gui.controls.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.style.Icon;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.gui.tool.recipe.GuiRecipeAnimationHandler;

public class GuiAnimationPanel extends GuiParent {
    
    public final GuiTree tree;
    public final GuiAnimationViewerStorage storage;
    public final boolean options;
    public final GuiRecipeAnimationHandler animation;
    
    public GuiAnimationPanel(GuiTree tree, GuiAnimationViewerStorage storage, boolean options, GuiRecipeAnimationHandler animation) {
        super("animation", GuiFlow.STACK_Y);
        setExpandable();
        
        this.tree = tree;
        this.storage = storage;
        this.options = options;
        this.animation = animation;
        
        GuiAnimationViewer viewer = new GuiAnimationViewer("viewer", storage);
        add(viewer.setExpandable());
        
        GuiParent animationButtons = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        add(animationButtons.setExpandableX());
        
        animationButtons.add(new GuiButtonIcon("perspective", Icon.CAMERA, x -> viewer.nextProjection()).setTooltip(new TextBuilder().translate("gui.recipe.perspective").build()));
        animationButtons.add(new GuiButtonIcon("home", Icon.HOUSE, x -> viewer.resetView()).setTooltip(new TextBuilder().translate("gui.recipe.home").build()));
        if (animation != null) {
            animationButtons.add(new GuiButtonIcon("play", Icon.PLAY, x -> animation.play()).setTooltip(new TextBuilder().translate("gui.recipe.play").build()));
            animationButtons.add(new GuiButtonIcon("pause", Icon.PAUSE, x -> animation.pause()).setTooltip(new TextBuilder().translate("gui.recipe.pause").build()));
            animationButtons.add(new GuiButtonIcon("stop", Icon.STOP, x -> animation.stop()).setTooltip(new TextBuilder().translate("gui.recipe.stop").build()));
        }
        
        GuiParent checkboxes = new GuiParent(GuiFlow.FIT_X).setAlign(Align.CENTER);
        add(checkboxes.setExpandableX());
        
        if (options) {
            checkboxes.add(new GuiCheckBox("filter", tree.hasCheckboxes()).setTranslate("gui.recipe.view.filter").consumeChanged(x -> {
                tree.setCheckboxes(x, false);
                tree.updateTree();
            }));
            
            checkboxes.add(new GuiCheckBox("highlight", storage.highlightSelected()).setTranslate("gui.recipe.view.highlight").consumeChanged(x -> storage.highlightSelected(x)));
        }
    }
    
    public void refresh() {
        if (options) {
            get("filter", GuiCheckBox.class).value = tree.hasCheckboxes();
            get("highlight", GuiCheckBox.class).value = storage.highlightSelected();
        }
    }
    
}
