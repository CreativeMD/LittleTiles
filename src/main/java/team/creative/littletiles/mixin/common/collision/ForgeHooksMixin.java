package team.creative.littletiles.mixin.common.collision;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeHooks;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(ForgeHooks.class)
public class ForgeHooksMixin {
    
    @Inject(method = "isLivingOnLadder", at = @At("RETURN"), cancellable = true, remap = false)
    private static void isLivingOnLadder(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull LivingEntity entity, CallbackInfoReturnable<Optional<BlockPos>> info) {
        if (info.getReturnValue().isPresent())
            return;
        MutableBlockPos tmp = new MutableBlockPos();
        AABB bb = entity.getBoundingBox();
        bb = entity.getBoundingBox().inflate(0.0001);
        int mX = Mth.floor(bb.minX);
        int mY = Mth.floor(bb.minY);
        int mZ = Mth.floor(bb.minZ);
        for (int y2 = mY; y2 < bb.maxY; y2++) {
            for (int x2 = mX; x2 < bb.maxX; x2++) {
                for (int z2 = mZ; z2 < bb.maxZ; z2++) {
                    tmp.set(x2, y2, z2);
                    state = level.getBlockState(tmp);
                    if (state.getBlock() instanceof BlockTile && state.getBlock().isLadder(state, level, tmp, entity)) {
                        info.setReturnValue(Optional.of(tmp.immutable()));
                        return;
                    }
                }
            }
        }
    }
    
}
