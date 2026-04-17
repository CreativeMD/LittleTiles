package team.creative.littletiles.common.item.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public class MeasurementsComponent {
    
    public static final Codec<MeasurementsComponent> CODEC = CompoundTag.CODEC.xmap(MeasurementsComponent::new, MeasurementsComponent::save);
    public static final StreamCodec<FriendlyByteBuf, MeasurementsComponent> STREAM_CODEC = StreamCodec.of((buffer, s) -> {
        buffer.writeNbt(s.save());
    }, (buffer) -> new MeasurementsComponent(buffer.readNbt()));
    
    public static MeasurementsComponent of(List<LittleMeasurement> measurements) {
        return new MeasurementsComponent(measurements);
    }
    
    public static List<LittleMeasurement> get(ItemStack stack) {
        var com = stack.get(LittleTilesRegistry.MEASUREMENTS);
        if (com != null)
            return com.value();
        return Collections.EMPTY_LIST;
    }
    
    private final List<LittleMeasurement> measurements;
    
    private MeasurementsComponent(CompoundTag nbt) {
        ListTag list = nbt.getList("c", Tag.TAG_COMPOUND);
        this.measurements = new ArrayList<>();
        for (int i = 0; i < list.size(); i++)
            this.measurements.add(LittleMeasurement.load(list.getCompound(i)));
    }
    
    private MeasurementsComponent(List<LittleMeasurement> measurements) {
        this.measurements = measurements;
    }
    
    public List<LittleMeasurement> value() {
        return new ArrayList<>(measurements);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MeasurementsComponent s)
            return Objects.equal(s.measurements, measurements);
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return measurements.hashCode();
    }
    
    public CompoundTag save() {
        ListTag list = new ListTag();
        for (int i = 0; i < measurements.size(); i++)
            list.add(measurements.get(i).save());
        CompoundTag nbt = new CompoundTag();
        nbt.put("c", list);
        return nbt;
    }
    
}
