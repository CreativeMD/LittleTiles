package team.creative.littletiles.common.block.little;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.filter.block.BlockFilters;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.tile.parent.IParentCollection;

public class LittleBlocks {
    
    static {
        LittleBlockRegistry.register(BlockFilters.block(Blocks.BARRIER), x -> new LittleMCBlock(x) {
            @Override
            @OnlyIn(Dist.CLIENT)
            public void randomDisplayTick(IParentCollection parent, LittleTile tile, java.util.Random rand) {
                Minecraft mc = Minecraft.getInstance();
                ItemStack itemstack = mc.player.getMainHandItem();
                if (mc.player.isCreative() && itemstack.is(Blocks.BARRIER))
                    mc.level.addParticle.spawnParticle(EnumParticleTypes.BARRIER, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0D, 0.0D, 0.0D, new int[0]);
            }
        });
    }
    
}
