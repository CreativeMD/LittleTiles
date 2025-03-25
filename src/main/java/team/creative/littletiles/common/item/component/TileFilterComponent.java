package team.creative.littletiles.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.filter.BiFilter;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.parent.IParentCollection;

public class TileFilterComponent {
    
    public static final Codec<TileFilterComponent> CODEC = RecordCodecBuilder.<TileFilterComponent>create(x -> x.group(Codec.BOOL.fieldOf("active").forGetter(e -> e.active),
        CompoundTag.CODEC.fieldOf("filter").forGetter(e -> e.nbt)).apply(x, TileFilterComponent::new));
    public static final StreamCodec<FriendlyByteBuf, TileFilterComponent> STREAM_CODEC = StreamCodec.of((buffer, i) -> {
        buffer.writeBoolean(i.active);
        buffer.writeNbt(i.nbt);
    }, (buffer) -> new TileFilterComponent(buffer.readBoolean(), buffer.readNbt()));
    
    public static TileFilterComponent of(boolean active, BiFilter<IParentCollection, LittleTile> filter) {
        try {
            return new TileFilterComponent(active, BiFilter.SERIALIZER.write(filter));
        } catch (RegistryException e) {
            return new TileFilterComponent(false, new CompoundTag());
        }
    }
    
    public static TileFilterComponent getOrCreate(ItemStack stack) {
        var filter = stack.get(LittleTilesRegistry.FILTER);
        if (filter != null)
            return filter;
        return new TileFilterComponent(false, new CompoundTag());
    }
    
    private final boolean active;
    private final CompoundTag nbt;
    
    private TileFilterComponent(boolean active, CompoundTag nbt) {
        this.active = active;
        this.nbt = nbt;
    }
    
    public boolean hasFilter() {
        return active;
    }
    
    public BiFilter<IParentCollection, LittleTile> getFilter() {
        try {
            if (nbt.isEmpty())
                return null;
            return BiFilter.SERIALIZER.read(nbt);
        } catch (RegistryException e) {
            return null;
        }
    }
    
    @Override
    public int hashCode() {
        return nbt.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TileFilterComponent t)
            return t.active == active && nbt.equals(t.nbt);
        return false;
    }
    
}
