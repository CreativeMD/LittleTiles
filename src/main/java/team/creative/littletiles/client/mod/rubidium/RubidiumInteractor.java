package team.creative.littletiles.client.mod.rubidium;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.passes.BlockRenderPass;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.client.mod.rubidium.buffer.RubidiumBufferHolder;
import team.creative.littletiles.client.mod.rubidium.buffer.RubidiumByteBufferHolder;
import team.creative.littletiles.client.mod.rubidium.pipeline.LittleRenderPipelineTypeRubidium;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public class RubidiumInteractor {
    
    public static final LittleRenderPipelineTypeRubidium PIPELINE = new LittleRenderPipelineTypeRubidium();
    
    public static void init() {
        LittleTiles.LOGGER.info("Loaded Rubidium extension");
    }
    
    public static BlockRenderPass getPass(RenderType layer) {
        if (layer == RenderType.solid())
            return BlockRenderPass.SOLID;
        if (layer == RenderType.cutout())
            return BlockRenderPass.CUTOUT;
        if (layer == RenderType.cutoutMipped())
            return BlockRenderPass.CUTOUT_MIPPED;
        if (layer == RenderType.translucent())
            return BlockRenderPass.TRANSLUCENT;
        if (layer == RenderType.tripwire())
            return BlockRenderPass.TRIPWIRE;
        throw new IllegalArgumentException();
    }
    
    public static boolean isRubidiumBuffer(BufferHolder holder) {
        return holder instanceof RubidiumBufferHolder;
    }
    
    public static BufferHolder combineBuffers(BufferHolder first, BufferHolder second) {
        int vertexCount = 0;
        int length = 0;
        IntArrayList[] facingBuffers = new IntArrayList[ModelQuadFacing.COUNT];
        for (int i = 0; i < facingBuffers.length; i++)
            facingBuffers[i] = new IntArrayList();
        Set<TextureAtlasSprite> sprites = null;
        
        int offset = 0;
        ByteBuffer firstBuffer = first.byteBuffer();
        if (firstBuffer != null) {
            vertexCount += first.vertexCount();
            length += first.length();
            offset = length;
            
            if (first instanceof RubidiumByteBufferHolder f) {
                IntArrayList[] firstFacingBuffers = f.facingIndexLists();
                for (int i = 0; i < facingBuffers.length; i++)
                    facingBuffers[i].addAll(firstFacingBuffers[i]);
                
                if (!f.getUsedTextures().isEmpty()) {
                    sprites = new ObjectOpenHashSet<>();
                    sprites.addAll(f.getUsedTextures());
                }
            }
        }
        
        ByteBuffer secondBuffer = second.byteBuffer();
        if (secondBuffer != null) {
            vertexCount += second.vertexCount();
            length += second.length();
            
            if (second instanceof RubidiumByteBufferHolder s) {
                IntArrayList[] secondFacingBuffers = s.facingIndexLists();
                
                for (int i = 0; i < facingBuffers.length; i++) {
                    int sizeBefore = facingBuffers[i].size();
                    facingBuffers[i].addAll(secondFacingBuffers[i]);
                    for (int j = sizeBefore; j < facingBuffers[i].size(); j++) // Apply offset
                        facingBuffers[i].set(j, facingBuffers[i].getInt(j) + offset);
                }
                
                if (!s.getUsedTextures().isEmpty()) {
                    if (sprites == null)
                        sprites = new ObjectOpenHashSet<>();
                    sprites.addAll(s.getUsedTextures());
                }
            }
        }
        
        if (vertexCount == 0)
            return null;
        
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(length);
        
        if (firstBuffer != null) {
            firstBuffer.position(0);
            firstBuffer.limit(first.length());
            byteBuffer.put(firstBuffer);
            firstBuffer.rewind();
        }
        
        if (secondBuffer != null) {
            secondBuffer.position(0);
            secondBuffer.limit(second.length());
            byteBuffer.put(secondBuffer);
            secondBuffer.rewind();
        }
        byteBuffer.rewind();
        return new RubidiumByteBufferHolder(byteBuffer, length, vertexCount, null, facingBuffers, sprites == null ? Collections.EMPTY_LIST : new ArrayList<>(sprites));
    }
}
