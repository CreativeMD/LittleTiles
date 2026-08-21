package team.creative.littletiles.client.render.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.model.preview.ItemModelPreview;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.item.ItemMultiTiles;

@OnlyIn(Dist.CLIENT)
public interface LittleModelItemPreview extends ItemModelPreview {
    
    public LittleElement getElement(ItemStack stack);
    
    @Override
    public default ItemStack getPreview(ItemStack stack) {
        return ItemMultiTiles.of(getElement(stack));
    }
    
}
