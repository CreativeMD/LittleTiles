package team.creative.littletiles.client.mod.iris;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.sodium.terrain.VertexEncoderInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.LittleTiles;

public class IrisInteractor {
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Iris extension");
    }
    
    public static boolean isShaders() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
    
    public static boolean uploadCachedVertices(VertexConsumer consumer, ByteBuffer buffer, VertexFormat format) {
        VertexBufferWriter writer = VertexBufferWriter.tryOf(consumer);
        if (writer == null)
            return false;
        
        // Iris handles Sodium's push as completed vertices, rather than adding
        // the last cached vertex to the next quad in endLastVertex.
        try (MemoryStack stack = MemoryStack.stackPush()) {
            writer.push(stack, MemoryUtil.memAddress(buffer), buffer.remaining() / format.getVertexSize(), format);
        }
        return true;
    }
    
    public static void beginBlock(Object buffers, BlockState state, BlockPos pos) {
        var ids = WorldRenderingSettings.INSTANCE.getBlockStateIds();
        if (ids == null)
            return;
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.beginBlock(ids.getOrDefault(state, -1), state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
        else if (buffers instanceof VertexEncoderInterface ext)
            ext.beginBlock(ids.getOrDefault(state, -1), state.liquid() ? (byte) 1 : (byte) 0, (byte) state.getLightEmission(), pos.getX(), pos.getY(), pos.getZ());
    }
    
    public static void resetBlockContext(Object buffers) {
        if (buffers instanceof BlockSensitiveBufferBuilder ext)
            ext.endBlock();
        else if (buffers instanceof VertexEncoderInterface ext)
            ext.restoreBlock();
    }
    
}
