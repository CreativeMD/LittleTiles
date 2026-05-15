package team.creative.littletiles.common.item.component;

import com.mojang.serialization.Codec;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import team.creative.littletiles.common.math.measure.LittleMeasurementType;

public class MeasurementTypeComponent {
    
    public static final Codec<MeasurementTypeComponent> CODEC = Codec.stringResolver(x -> LittleMeasurementType.REGISTRY.getId(x.type),
        x -> new MeasurementTypeComponent(LittleMeasurementType.REGISTRY.get(x)));
    public static final StreamCodec<FriendlyByteBuf, MeasurementTypeComponent> STREAM_CODEC = StreamCodec.of((buffer, s) -> {
        buffer.writeUtf(LittleMeasurementType.REGISTRY.getId(s.type));
    }, (buffer) -> new MeasurementTypeComponent(LittleMeasurementType.REGISTRY.get(buffer.readUtf())));
    
    public final LittleMeasurementType type;
    
    public MeasurementTypeComponent(LittleMeasurementType type) {
        this.type = type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MeasurementTypeComponent t)
            return t.type.equals(type);
        return false;
    }
}
