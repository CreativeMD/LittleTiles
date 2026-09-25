package team.creative.littletiles.mixin.sable;

import java.util.Iterator;
import java.util.List;

import org.joml.Vector2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.mc.BlockTile;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {
    
    @Inject(method = "moveBlocks(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/api/SubLevelAssemblyHelper$AssemblyTransform;Ljava/lang/Iterable;)V", remap = false,
            require = 1, at = @At(value = "INVOKE", target = "saveWithFullMetadata(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;"),
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void moveBlocks(final ServerLevel level, final AssemblyTransform transform, final Iterable<BlockPos> blocks, CallbackInfo info, ServerLevel resultingLevel,
            LevelAccelerator accelerator, LevelAccelerator resultingAccelerator, BlockState airState, List<BlockState> states, BlockPos firstBlockPos, Vector2i chunkBoundsMin,
            Vector2i chunkBoundsMax, SubLevel subLevel, Iterator<BlockPos> iterator, BlockPos block, BlockState state) {
        if (state.getBlock() instanceof BlockTile) {
            BETiles be = BlockTile.loadBE(level, block);
            if (be != null)
                be.rotate(transform.getRotation());
        }
    }
}
