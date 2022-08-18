package team.creative.littletiles.common.gui.tool;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.common.animation.preview.AnimationPreview;
import team.creative.littletiles.common.gui.controls.IAnimationControl;

public class GuiRecipe extends GuiConfigure implements IAnimationControl {
    
    public final GuiSyncLocal<StringTag> CLEAR_CONTENT = getSyncHolder().register("clear_content", tag -> {
        //GuiCreator.openGui("recipeadvanced", new CompoundTag(), getPlayer());
    });
    
    public GuiRecipe(ContainerSlotView view) {
        super("recipe", 350, 200, view);
    }
    
    @Override
    public void onLoaded(AnimationPreview preview) {}
    
    @Override
    public CompoundTag saveConfiguration(CompoundTag nbt) {
        return null;
    }
    
    @Override
    public void create() {}
    
}
