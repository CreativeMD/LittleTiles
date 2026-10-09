package team.creative.littletiles.client.mod.iris;

import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormatElement;

import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.shaderpack.materialmap.WorldRenderingSettings;
import net.irisshaders.iris.vertices.BlockSensitiveBufferBuilder;
import net.irisshaders.iris.vertices.BufferBuilderPolygonView;
import net.irisshaders.iris.vertices.IrisVertexFormats;
import net.irisshaders.iris.vertices.NormalHelper;
import net.irisshaders.iris.vertices.NormI8;
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

    public static void recalculateCacheNormals(MeshData mesh) {
        VertexFormat format = mesh.drawState().format();
        if (format != IrisVertexFormats.TERRAIN)
            return;

        // Iris only replaces quad normals while its global rendering-level flag
        // is set. LT builds caches on workers, so that flag cannot determine
        // their normals. Use the same Iris calculation on each completed quad.
        int stride = format.getVertexSize();
        int normalOffset = format.getOffset(VertexFormatElement.NORMAL);
        long pointer = MemoryUtil.memAddress(mesh.vertexBuffer());
        long[] offsets = {0, stride, stride * 2L, stride * 3L};
        var polygon = new BufferBuilderPolygonView();
        var normal = new Vector3f();
        for (int vertex = 0; vertex + 3 < mesh.drawState().vertexCount(); vertex += 4) {
            long quadPointer = pointer + (long) vertex * stride;
            polygon.setup(quadPointer, offsets, stride, 4);
            NormalHelper.computeFaceNormal(normal, polygon);
            int packed = NormI8.pack(normal);
            for (long offset : offsets)
                MemoryUtil.memPutInt(quadPointer + offset + normalOffset, packed);
        }
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
