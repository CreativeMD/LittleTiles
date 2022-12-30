package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.flow.GuiSizeRule.GuiSizeRatioRules;
import team.creative.creativecore.common.gui.style.GuiIcon;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.controls.GuiAnimationViewer;
import team.creative.littletiles.common.gui.controls.IAnimationControl;

public class GuiRecipe extends GuiConfigure implements IAnimationControl {
    
    public final GuiSyncLocal<StringTag> CLEAR_CONTENT = getSyncHolder().register("clear_content", tag -> {
        //GuiCreator.openGui("recipeadvanced", new CompoundTag(), getPlayer());
    });
    
    public GuiRecipe(ContainerSlotView view) {
        super("recipe", view);
        flow = GuiFlow.STACK_X;
        valign = VAlign.STRETCH;
    }
    
    @Override
    public void onLoaded(AnimationPreview preview) {
        callOnLoaded(preview, this);
    }
    
    private void callOnLoaded(AnimationPreview preview, Iterable<GuiChildControl> controls) {
        for (GuiChildControl child : controls) {
            if (child.control instanceof IAnimationControl a)
                a.onLoaded(preview);
            if (child.control instanceof GuiParent p)
                callOnLoaded(preview, p);
        }
    }
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        return null;
    }
    
    @Override
    public void create() {
        GuiParent topBottom = new GuiParent(GuiFlow.STACK_Y).setAlign(Align.STRETCH);
        add(topBottom.setExpandable());
        
        GuiParent top = new GuiParent(GuiFlow.STACK_X);
        topBottom.add(top.setExpandableY());
        
        GuiTree tree = new GuiTree("overview", false).setRootVisibility(false);
        GuiTreeItem level1 = new GuiTreeItem("test", tree).setTitle(Component.literal("level 1"));
        GuiTreeItem level2 = new GuiTreeItem("test", tree).setTitle(Component.literal("level 1.1"));
        level2.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 1.1.1 ...")));
        level2.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 1.1.2")));
        level1.addItem(level2);
        level1.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 1.2 ...")));
        tree.root().addItem(level1);
        
        level1 = new GuiTreeItem("test", tree).setTitle(Component.literal("level 2"));
        level1.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 2.1")));
        level2 = new GuiTreeItem("test", tree).setTitle(Component.literal("level 2.2"));
        level2.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 2.2.1")));
        level2.addItem(new GuiTreeItem("test", tree).setTitle(Component.literal("level 2.2.2 ...")));
        level1.addItem(level2);
        tree.root().addItem(level1);
        
        tree.root().setTitle(Component.literal("root"));
        tree.updateTree();
        top.add(tree.setDim(new GuiSizeRatioRules().widthRatio(0.2F).maxWidth(100)).setExpandableY());
        
        GuiParent topCenter = new GuiParent(GuiFlow.STACK_Y);
        top.add(topCenter.setDim(new GuiSizeRatioRules().widthRatio(0.4F).maxWidth(300)).setExpandableY());
        
        top.add(new GuiAnimationViewer("viewer").setExpandable());
        
        GuiParent bottom = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        topBottom.add(bottom);
        
        GuiParent leftBottom = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        bottom.add(leftBottom.setDim(new GuiSizeRatioRules().widthRatio(0.2F).maxWidth(100)));
        leftBottom.add(new GuiIconButton("up", GuiIcon.ARROW_UP, x -> tree.moveUp()));
        leftBottom.add(new GuiIconButton("down", GuiIcon.ARROW_DOWN, x -> tree.moveDown()));
        
        GuiLeftRightBox rightBottom = new GuiLeftRightBox();
        bottom.add(rightBottom.setExpandableX());
        rightBottom.addLeft(new GuiButton("cancel", x -> {}).setTranslate("gui.cancel"));
        rightBottom.addLeft(new GuiButton("clear", x -> {}).setTranslate("gui.recipe.clear"));
        rightBottom.addLeft(new GuiButton("selection", x -> {}).setTranslate("gui.recipe.selection"));
        rightBottom.addRight(new GuiButton("check", x -> {}).setTranslate("gui.check"));
        rightBottom.addRight(new GuiButton("save", x -> {}).setTranslate("gui.save"));
        
        LittleGroup group = LittleGroup.load(tool.get().getOrCreateTagElement("content"));
        AnimationPreview preview = new AnimationPreview(group);
        onLoaded(preview);
    }
    
}
