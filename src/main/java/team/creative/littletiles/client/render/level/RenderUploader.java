package team.creative.littletiles.client.render.level;

import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.GL15;

import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder.SortState;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.ChunkLayerCache;
import team.creative.littletiles.client.render.mc.RenderChunkExtender;
import team.creative.littletiles.client.render.mc.VertexBufferExtender;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.mixin.CompiledChunkAccessor;

@OnlyIn(Dist.CLIENT)
public class RenderUploader {
    
    private static final Minecraft mc = Minecraft.getInstance();
    
    public static void uploadRenderData(RenderChunk chunk, List<BETiles> blocks) {
        if (OptifineHelper.isRenderRegions() || !LittleTiles.CONFIG.rendering.uploadToVBODirectly)
            return;
        
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            ChunkLayerCache cache = new ChunkLayerCache();
            
            int size = 0;
            for (BETiles be : blocks)
                size += be.render.getBufferCache().size(layer);
            
            if (size == 0)
                return;
            
            try {
                CompiledChunk compiled = chunk.getCompiledChunk();
                VertexBuffer uploadBuffer = chunk.getBuffer(layer);
                
                if (uploadBuffer == null)
                    return;
                
                VertexFormat format = uploadBuffer.getFormat();
                ChunkRenderDispatcher dispatcher = mc.levelRenderer.getChunkRenderDispatcher();
                
                ByteBuffer vanillaBuffer = null;
                if (compiled.isEmpty(layer)) {
                    uploadBuffer.bind();;
                    vanillaBuffer = glMapBufferRange((long) (((VertexBufferExtender) uploadBuffer).getIndexCount() / 1.5 * format.getVertexSize()));
                    VertexBuffer.unbind();
                }
                
                BufferBuilder builder = new BufferBuilder(((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + size + DefaultVertexFormat.BLOCK.getVertexSize()) / 6); // dividing by 6 is risking and could potentially cause issues
                ((RenderChunkExtender) chunk).invokeBeginLayer(builder);
                if (vanillaBuffer != null) {
                    if (layer == RenderType.translucent()) {
                        SortState state = ((CompiledChunkAccessor) compiled).getTransparencyState();
                        if (state != null)
                            builder.restoreSortState(state);
                    }
                    
                    builder.putBulkData(vanillaBuffer);
                }
                
                for (BETiles be : blocks)
                    be.render.getBufferCache().add(layer, builder, cache);
                
                if (layer == RenderType.translucent()) {
                    Vec3 vec3 = dispatcher.getCameraPosition();
                    float f = (float) vec3.x;
                    float f1 = (float) vec3.y;
                    float f2 = (float) vec3.z;
                    
                    builder.setQuadSortOrigin(f - chunk.getOrigin().getX(), f1 - chunk.getOrigin().getY(), f2 - chunk.getOrigin().getZ());
                }
                
                uploadBuffer.upload(builder.end());
                if (compiled != CompiledChunk.UNCOMPILED)
                    ((CompiledChunkAccessor) compiled).getHasBlocks().add(layer);
                
            } catch (IllegalArgumentException | NotSupportedException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static ByteBuffer glMapBufferRange(long length) throws NotSupportedException {
        try {
            ByteBuffer result = MemoryTracker.create((int) length);
            GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, result);
            return result;
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (e instanceof IllegalStateException)
                throw new NotSupportedException(e);
            else
                e.printStackTrace();
        }
        return null;
    }
    
    public static class NotSupportedException extends Exception {
        
        public NotSupportedException(Exception e) {
            super(e);
        }
        
    }
    
}