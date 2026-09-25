package team.creative.littletiles.mixin.sable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertyHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(PhysicsBlockPropertyHelper.class)
public class PhysicsBlockPropertyHelperMixin {
    
    @Inject(at = @At("HEAD"), method = "getMass(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)D",
            cancellable = true, remap = false, require = 1)
    private static void getMass(final BlockGetter level, final BlockPos pos, final BlockState state, CallbackInfoReturnable<Double> info) {
        if (state.getBlock() instanceof BlockTile) {
            BETiles be = BlockTile.loadBE(level, pos);
            if (be != null) {
                var grid = be.getGrid();
                double mass = 0;
                for (Pair<IParentCollection, LittleTile> pair : be.allTiles())
                    mass += PhysicsBlockPropertyHelper.getMass(level, pos, pair.value.getState()) * pair.value.getPercentVolume(grid);
                info.setReturnValue(mass);
            }
        }
    }
    
}
