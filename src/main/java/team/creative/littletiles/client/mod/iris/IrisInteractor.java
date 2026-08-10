package team.creative.littletiles.client.mod.iris;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.api.client.IFakeRenderingBlock;

public class IrisInteractor {
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Iris extension");
    }
    
    public static boolean isShaders() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
    
    /**
     * Iris transparently upgrades {@link DefaultVertexFormat#BLOCK} to its terrain format while a
     * shader pack is active. Cached byte offsets have to use the upgraded stride as well.
     */
    public static int vertexStride(VertexFormat format) {
        if (format == DefaultVertexFormat.BLOCK && isShaders())
            return IrisVertexFormats.TERRAIN.getVertexSize();
        return format.getVertexSize();
    }
    
    /**
     * Opens Iris' terrain material scope for geometry written through Minecraft's
     * {@link VertexConsumer} path.
     *
     * @return whether a matching {@link #endBlock(VertexConsumer)} call is required
     */
    public static boolean beginBlock(VertexConsumer consumer, BlockState state, BlockPos localPos) {
        return beginBlock(consumer, state, localPos.getX(), localPos.getY(), localPos.getZ());
    }

    public static boolean beginBlock(VertexConsumer consumer, BlockState state, int localX, int localY, int localZ) {
        if (!isShaders() || !(consumer instanceof BlockSensitiveBufferBuilder ext))
            return false;

        Object2IntMap<BlockState> ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        if (ids == null)
            return false;

        state = materialState(state);
        int blockId = ids.getOrDefault(state, -1);
        ext.beginBlock(blockId, state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), localX, localY, localZ);
        return true;
    }

    public static void endBlock(VertexConsumer consumer) {
        if (consumer instanceof BlockSensitiveBufferBuilder ext)
            ext.endBlock();
    }
    
    /**
     * Modern Iris keeps Sodium terrain material data on the {@link BlockRenderer}, not on
     * {@code ChunkBuildBuffers}. The context is consumed while the renderer writes each vertex.
     */
    public static void setBlockContext(BlockRenderer renderer, BlockState state, BlockPos pos) {
        if (!isShaders() || !(renderer instanceof VertexEncoderInterface ext))
            return;

        Object2IntMap<BlockState> ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        if (ids == null)
            return;

        state = materialState(state);
        int blockId = ids.getOrDefault(state, -1);
        ext.beginBlock(blockId, state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
    }

    private static BlockState materialState(BlockState state) {
        if (state.getBlock() instanceof IFakeRenderingBlock fake)
            return fake.getFakeState(state);
        return state;
    }

}
