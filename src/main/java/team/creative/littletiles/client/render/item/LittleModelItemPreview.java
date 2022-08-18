package team.creative.littletiles.client.render.item;

import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.item.ItemMultiTiles;

@OnlyIn(Dist.CLIENT)
public class LittleModelItemPreview extends LittleModelItemBackground {
    
    private final Function<ItemStack, LittleElement> func;
    
    public LittleModelItemPreview(ModelResourceLocation location, Function<ItemStack, LittleElement> function) {
        super(location, null);
        this.func = function;
    }
    
    @Override
    public boolean shouldRenderFake(ItemStack stack) {
        return true;
    }
    
    @Override
    protected ItemStack getFakeStack(ItemStack current) {
        return ItemMultiTiles.of(func.apply(current));
    }
    
    @Override
    public void prepareRenderer(PoseStack pose) {
        pose.translate(0.1, 0.1, 0.2);
        pose.scale(0.7F, 0.7F, 0.7F);
    }
    
}
