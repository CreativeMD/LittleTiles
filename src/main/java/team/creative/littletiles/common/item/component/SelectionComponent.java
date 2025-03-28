package team.creative.littletiles.common.item.component;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.placement.selection.SelectionMode;

public class SelectionComponent {
    
    public static final Codec<SelectionComponent> CODEC = RecordCodecBuilder.<SelectionComponent>create(x -> x.group(Codec.STRING.fieldOf("mode").forGetter(e -> e.mode.getName()),
        CompoundTag.CODEC.fieldOf("config").forGetter(e -> e.config)).apply(x, SelectionComponent::new));
    public static final StreamCodec<FriendlyByteBuf, SelectionComponent> STREAM_CODEC = StreamCodec.of((buffer, s) -> {
        buffer.writeUtf(s.mode.getName());
        buffer.writeNbt(s.config);
    }, (buffer) -> new SelectionComponent(buffer.readUtf(), buffer.readNbt()));
    
    public static SelectionComponent of(SelectionMode mode, CompoundTag config) {
        return new SelectionComponent(mode, config);
    }
    
    public static SelectionComponent getOrDefault(ItemStack stack) {
        var com = stack.get(LittleTilesRegistry.SELECTION);
        if (com != null)
            return com;
        return new SelectionComponent(SelectionMode.REGISTRY.getDefault(), new CompoundTag());
    }
    
    public final SelectionMode mode;
    private final CompoundTag config;
    
    private SelectionComponent(String id, CompoundTag config) {
        this(SelectionMode.REGISTRY.get(id), config);
    }
    
    private SelectionComponent(SelectionMode mode, CompoundTag config) {
        this.mode = mode;
        this.config = config;
    }
    
    public CompoundTag getConfig() {
        return config.copy();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SelectionComponent s)
            return s.mode == mode && Objects.equal(s.config, config);
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return mode.hashCode() + config.hashCode();
    }
    
    public SelectionComponent withConfig(CompoundTag config) {
        return new SelectionComponent(mode, config);
    }
    
    public SelectionComponent withMode(SelectionMode mode) {
        return new SelectionComponent(mode, config);
    }
}
