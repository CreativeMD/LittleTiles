package team.creative.littletiles.common.math.measure;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public abstract class LittleMeasurement {
    
    public static final NamedTypeRegistry<LittleMeasurement> REGISTRY = new NamedTypeRegistry<LittleMeasurement>().addConstructorPattern(CompoundTag.class);
    
    @Nullable
    public static LittleMeasurement load(CompoundTag nbt) {
        try {
            return REGISTRY.create(nbt.getString("type"), nbt);
        } catch (RegistryException e) {
            return null;
        }
    }
    
    static {
        REGISTRY.register("line", LittleMeasurementLine.class);
        REGISTRY.register("box", LittleMeasurementBox.class);
    }
    
    public int color;
    
    public LittleMeasurement(CompoundTag nbt) {
        this.color = nbt.getInt("color");
    }
    
    public LittleMeasurement() {}
    
    public abstract void build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder);
    
    public abstract void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam);
    
    public abstract void collectPositions(List<LittleBoxAbsolute> positions);
    
    public void changed() {}
    
    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("color", color);
        nbt.putString("type", REGISTRY.getId(this));
        saveExtra(nbt);
        return nbt;
    }
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    public static abstract class LittleMeasurementSimple extends LittleMeasurement {
        
        protected final LittleBoxAbsolute first;
        protected final LittleBoxAbsolute second;
        
        public LittleMeasurementSimple(CompoundTag nbt) {
            super(nbt);
            first = LittleBoxAbsolute.of(nbt.getIntArray("0"));
            second = LittleBoxAbsolute.of(nbt.getIntArray("1"));
            changed();
        }
        
        public LittleMeasurementSimple(List<LittleBoxAbsolute> positions) {
            this.first = positions.get(0);
            this.second = positions.get(1);
            changed();
        }
        
        @Override
        public void collectPositions(List<LittleBoxAbsolute> positions) {
            positions.add(first);
            positions.add(second);
        }
        
        @Override
        protected void saveExtra(CompoundTag nbt) {
            nbt.putIntArray("0", first.toArray());
            nbt.putIntArray("1", second.toArray());
        }
        
    }
    
}
