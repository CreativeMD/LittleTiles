package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiChildControl;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.simple.GuiIconButton;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.creativecore.common.gui.flow.GuiFlow;
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
        super("recipe", 350, 200, view);
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
        GuiParent sidebar = new GuiParent(GuiFlow.STACK_Y);
        add(sidebar.setAlign(Align.STRETCH));
        
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
        sidebar.add(tree.setExpandableY());
        
        GuiParent bottomSidebar = new GuiParent(GuiFlow.STACK_X).setAlign(Align.CENTER);
        sidebar.add(bottomSidebar);
        bottomSidebar.add(new GuiIconButton("up", GuiIcon.ARROW_UP, x -> tree.moveUp()));
        bottomSidebar.add(new GuiIconButton("down", GuiIcon.ARROW_DOWN, x -> tree.moveDown()));
        
        add(new GuiAnimationViewer("viewer").setExpandable());
        LittleGroup group = LittleGroup.load(tool.get().getOrCreateTag());
        AnimationPreview preview = new AnimationPreview(group);
        onLoaded(preview);
    }
    
}
