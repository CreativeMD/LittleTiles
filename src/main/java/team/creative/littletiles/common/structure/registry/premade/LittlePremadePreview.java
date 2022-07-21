package team.creative.littletiles.common.structure.registry.premade;

import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public class LittlePremadePreview {
    
    public final LittleGroup previews;
    public final ItemStack stack;
    
    public LittlePremadePreview(LittleGroup previews, ItemStack stack) {
        this.previews = previews;
        this.stack = stack;
    }
    
    public boolean arePreviewsEqual(LittleGroup previews) {
        return this.previews.isVolumeEqual(previews);
    }
}
