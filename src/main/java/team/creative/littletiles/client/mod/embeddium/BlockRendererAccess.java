package team.creative.littletiles.client.mod.embeddium;

import me.jellysquid.mods.sodium.client.model.quad.BakedQuadView;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockRendererAccess {
    void drawQuad(ChunkModelBuilder builder, Direction face, WorldSlice levelSlice, BakedQuadView quad, BlockState state, RenderType renderType, BlockPos pos, BlockPos offset, int color);
}
