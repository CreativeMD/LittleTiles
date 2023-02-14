package team.creative.littletiles.mixin.server.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.serialization.Codec;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;

@Mixin(ChunkSerializer.class)
public interface ChunkSerializerAccessor {
    
    @Accessor
    public static Codec<PalettedContainer<BlockState>> getBLOCK_STATE_CODEC() {
        throw new UnsupportedOperationException();
    }
    
}
