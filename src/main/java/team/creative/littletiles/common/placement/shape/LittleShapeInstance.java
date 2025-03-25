package team.creative.littletiles.common.placement.shape;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.network.type.NetworkFieldTypeClass;
import team.creative.creativecore.common.network.type.NetworkFieldTypes;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.placement.shape.config.LittleShapeConfig;

public class LittleShapeInstance {
    
    public static final Codec<LittleShapeInstance> CODEC = RecordCodecBuilder.<LittleShapeInstance>create(x -> x.group(Codec.STRING.fieldOf("shape").forGetter(
        e -> ShapeRegistry.REGISTRY.getId(e.shape)), CompoundTag.CODEC.fieldOf("config").forGetter(e -> e.config)).apply(x, LittleShapeInstance::new));
    public static final StreamCodec<FriendlyByteBuf, LittleShapeInstance> STREAM_CODEC = StreamCodec.of((buffer, i) -> {
        buffer.writeUtf(ShapeRegistry.REGISTRY.getId(i.shape));
        buffer.writeNbt(i.config);
    }, (buffer) -> new LittleShapeInstance(buffer.readUtf(), buffer.readNbt()));
    
    public static LittleShapeInstance of(LittleShape shape) {
        return new LittleShapeInstance(shape, new CompoundTag());
    }
    
    public static LittleShapeInstance getOrCreate(ItemStack stack, LittleShape defaultShape) {
        LittleShapeInstance in = stack.get(LittleTilesRegistry.SHAPE);
        if (in != null)
            return in;
        return new LittleShapeInstance(defaultShape, new CompoundTag());
    }
    
    static {
        NetworkFieldTypes.register(new NetworkFieldTypeClass<LittleShapeInstance>() {
            
            @Override
            protected void writeContent(LittleShapeInstance content, RegistryFriendlyByteBuf buffer) {
                buffer.writeUtf(content.shape.getKey());
                buffer.writeNbt(content.config);
            }
            
            @Override
            protected LittleShapeInstance readContent(RegistryFriendlyByteBuf buffer) {
                return new LittleShapeInstance(buffer.readUtf(), buffer.readNbt());
            }
        }, LittleShapeInstance.class);
    }
    
    public final LittleShape shape;
    private final CompoundTag config;
    
    private LittleShapeInstance(String id, CompoundTag config) {
        this(ShapeRegistry.get(id), config);
    }
    
    private LittleShapeInstance(LittleShape shape, CompoundTag config) {
        this.shape = shape;
        this.config = config;
    }
    
    public LittleShapeConfig getConfig(HolderLookup.Provider provider, Side side) {
        return loadOtherConfig(provider, shape, side);
    }
    
    public LittleShapeConfig loadOtherConfig(HolderLookup.Provider provider, LittleShape shape, Side side) {
        String shapeId = ShapeRegistry.getConfig(shape);
        if (shapeId != null)
            return ShapeRegistry.SHAPE_CONFIG_REGISTRY.loadOrCreateDefault(provider, config, shapeId, side);
        return null;
    }
    
    public LittleShapeInstance configure(HolderLookup.Provider provider, LittleShapeConfig config, Side side) {
        return configure(provider, shape, config, side);
    }
    
    public <T extends LittleShapeConfig> LittleShapeInstance configure(HolderLookup.Provider provider, LittleShape<T> shape, @Nullable T config, Side side) {
        CompoundTag tag = this.config.copy();
        if (config != null)
            ShapeRegistry.SHAPE_CONFIG_REGISTRY.save(provider, config, tag, side);
        return new LittleShapeInstance(shape, tag);
    }
    
    public void appendInformation(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("gui.shape").append(": ").append(shape.translatable()));
        var config = getConfig(context.registries(), Side.CLIENT);
        if (config != null)
            tooltip.addAll(config.information());
    }
    
    public Component translatable() {
        return shape.translatable();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleShapeInstance i)
            return i.shape == shape && Objects.equal(i.config, config);
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return shape.hashCode() + config.hashCode();
    }
    
}
