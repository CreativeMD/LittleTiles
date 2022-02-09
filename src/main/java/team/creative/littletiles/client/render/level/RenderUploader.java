package team.creative.littletiles.client.render.level;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL15;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.client.render.model.BufferBuilderUtils;
import team.creative.creativecore.common.mod.OptifineHelper;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.render.cache.ChunkBlockLayerCache;

@SideOnly(Side.CLIENT)
public class RenderUploader {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    // VertexBuffer
    private static Field vertexCountField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "count", "field_177364_c" });
    private static Field bufferIdField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "glBufferId", "field_177365_a" });
    private static Field formatField = ReflectionHelper.findField(VertexBuffer.class, new String[] { "vertexFormat", "field_177363_b" });
    private static Method setLayerUsed = ReflectionHelper.findMethod(CompiledChunk.class, "setLayerUsed", "func_178486_a", BlockRenderLayer.class);
    
    public static int getBufferId(VertexBuffer buffer) {
        try {
            return bufferIdField.getInt(buffer);
        } catch (IllegalArgumentException | IllegalAccessException e) {}
        return -1;
    }
    
    public static void uploadRenderData(RenderChunk chunk, List<TileEntityLittleTiles> tiles) {
        if ((FMLClientHandler.instance().hasOptifine() && OptifineHelper.isRenderRegions()) || !LittleTiles.CONFIG.rendering.uploadToVBODirectly)
            return;
        
        try {
            for (int i = 0; i < BlockRenderLayer.values().length; i++) {
                BlockRenderLayer layer = BlockRenderLayer.values()[i];
                
                ChunkBlockLayerCache cache = new ChunkBlockLayerCache(layer.ordinal());
                
                for (TileEntityLittleTiles te : tiles)
                    cache.add(te.render, te.render.getBufferCache().get(i));
                
                try {
                    ByteBuffer toUpload;
                    if (cache.expanded() > 0) {
                        CompiledChunk compiled = chunk.getCompiledChunk();
                        VertexBuffer uploadBuffer = chunk.getVertexBufferByLayer(i);
                        
                        if (uploadBuffer == null)
                            return;
                        
                        if (layer == BlockRenderLayer.TRANSLUCENT) {
                            boolean empty = compiled.getState() == null || compiled.isLayerEmpty(BlockRenderLayer.TRANSLUCENT);
                            BufferBuilder builder = new BufferBuilder((empty ? 0 : compiled.getState().getRawBuffer().length * 4) + cache.expanded() + DefaultVertexFormats.BLOCK
                                    .getNextOffset());
                            
                            builder.begin(7, DefaultVertexFormats.BLOCK);
                            builder.setTranslation(-chunk.getPosition().getX(), -chunk.getPosition().getY(), -chunk.getPosition().getZ());
                            
                            if (!empty)
                                builder.setVertexState(compiled.getState());
                            
                            BufferBuilderUtils.growBufferSmall(builder, cache.expanded());
                            cache.fillBuilder(builder);
                            
                            Entity entity = mc.getRenderViewEntity();
                            float x = (float) entity.posX;
                            float y = (float) entity.posY + entity.getEyeHeight();
                            float z = (float) entity.posZ;
                            builder.sortVertexData(x, y, z);
                            compiled.setState(builder.getVertexState());
                            builder.finishDrawing();
                            
                            toUpload = builder.getByteBuffer();
                        } else {
                            VertexFormat format = (VertexFormat) formatField.get(uploadBuffer);
                            int uploadedVertexCount = vertexCountField.getInt(uploadBuffer);
                            
                            // Retrieve vanilla buffered data
                            uploadBuffer.bindBuffer();
                            boolean empty = compiled.isLayerEmpty(layer);
                            ByteBuffer vanillaBuffer = empty ? null : glMapBufferRange(uploadedVertexCount * format.getNextOffset());
                            uploadBuffer.unbindBuffer();
                            
                            toUpload = ByteBuffer
                                    .allocateDirect((vanillaBuffer != null ? vanillaBuffer.limit() : 0) + cache.expanded() + DefaultVertexFormats.BLOCK.getNextOffset());
                            if (vanillaBuffer != null)
                                toUpload.put(vanillaBuffer);
                            
                            cache.fillBuffer(toUpload);
                        }
                        
                        uploadBuffer.deleteGlBuffers();
                        bufferIdField.setInt(uploadBuffer, OpenGlHelper.glGenBuffers());
                        toUpload.position(0);
                        uploadBuffer.bufferData(toUpload);
                        if (compiled != CompiledChunk.DUMMY)
                            setLayerUsed.invoke(compiled, layer);
                    }
                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        } catch (NotSupportedException e) {}
    }
    
    private static Field arbVboField = ReflectionHelper.findField(OpenGlHelper.class, new String[] { "arbVbo", "field_176090_Y" });
    
    public static ByteBuffer glMapBufferRange(long length) throws NotSupportedException {
        try {
            ByteBuffer result = ByteBuffer.allocateDirect((int) length);
            if (arbVboField.getBoolean(null))
                ARBVertexBufferObject.glGetBufferSubDataARB(ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0, result);
            else
                GL15.glGetBufferSubData(GL15.GL_ARRAY_BUFFER, 0, result);
            return result;
        } catch (IllegalArgumentException | IllegalAccessException | IllegalStateException e) {
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