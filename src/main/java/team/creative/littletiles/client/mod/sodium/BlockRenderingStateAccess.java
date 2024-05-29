package team.creative.littletiles.client.mod.sodium;

import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockRenderingStateAccess {
    void setupState(BlockPos blockPos, BlockPos origin, BlockState state, LevelSlice slice, RenderType type);

    void setCustomTint(int color);

}
