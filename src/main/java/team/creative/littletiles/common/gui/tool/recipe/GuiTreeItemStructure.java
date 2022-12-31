package team.creative.littletiles.common.gui.tool.recipe;

import java.util.concurrent.CompletableFuture;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.controls.tree.GuiTree;
import team.creative.creativecore.common.gui.controls.tree.GuiTreeItem;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.structure.LittleStructure;

public class GuiTreeItemStructure extends GuiTreeItem {
    
    private GuiRecipe recipe;
    public final LittleGroup group;
    
    private AnimationPreview preview;
    public LittleStructure structure;
    private LittleVecGrid offset;
    
    public GuiTreeItemStructure(String name, GuiRecipe recipe, GuiTree tree, LittleGroup group) {
        super(name, tree);
        this.recipe = recipe;
        this.group = group;
        if (group.hasStructure()) {
            this.structure = group.getStructureType().createStructure(null);
            this.structure.load(group.getStructureTag());
        }
        refreshAnimation();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void refreshAnimation() {
        CompletableFuture.supplyAsync(() -> new AnimationPreview(group)).whenComplete((preview, throwable) -> {
            this.preview = preview;
            if (throwable != null)
                throwable.printStackTrace();
            else
                recipe.onLoaded(preview);
        });
    }
    
    public AnimationPreview getAnimationPreview() {
        return preview;
    }
    
}
