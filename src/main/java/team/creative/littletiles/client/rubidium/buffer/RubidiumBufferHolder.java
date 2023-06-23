package team.creative.littletiles.client.rubidium.buffer;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import team.creative.littletiles.client.render.cache.buffer.BufferHolder;

public interface RubidiumBufferHolder extends BufferHolder {
    
    public IntArrayList[] facingIndexLists();
    
    public IntArrayList facingIndexList(ModelQuadFacing facing);
    
    public List<TextureAtlasSprite> getUsedTextures();
    
}
