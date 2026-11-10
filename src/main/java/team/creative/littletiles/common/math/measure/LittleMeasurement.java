package team.creative.littletiles.common.math.measure;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import team.creative.creativecore.common.util.mc.TooltipUtils;
import team.creative.creativecore.common.util.registry.NamedTypeRegistry;
import team.creative.creativecore.common.util.registry.exception.RegistryException;
import team.creative.littletiles.client.render.overlay.OverlayRenderer;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.LittleToolMeasure.RenderMeasurementResult;
import team.creative.littletiles.common.action.exception.LittleActionException;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBoxLevel;

public abstract class LittleMeasurement {
    
    public static Component displayLength(double length) {
        int blocks = (int) length;
        double pixels = (length - blocks) / LittleGrid.overallDefault().pixelLength;
        var text = Component.empty();
        if (blocks > 0)
            text.append(blocks + "").append(Component.translatable("volume.unit.big.short"));
        if (pixels > 0)
            text.append((blocks > 0 ? " " : "") + TooltipUtils.print(pixels) + "").append(Component.translatable("volume.unit.small.short"));
        return text;
    }
    
    public static Component displayArea(double area) {
        int blocks = (int) area;
        double pixels = (area - blocks) / LittleGrid.overallDefault().pixelArea;
        var text = Component.empty();
        if (blocks > 0)
            text.append(blocks + "").append(Component.translatable("volume.unit.big.short"));
        if (pixels > 0)
            text.append((blocks > 0 ? " " : "") + TooltipUtils.print(pixels) + "").append(Component.translatable("volume.unit.small.short"));
        return text;
    }
    
    public static Component displayVolume(double volume) {
        int blocks = (int) volume;
        double pixels = (volume - blocks) / LittleGrid.overallDefault().pixelVolume;
        var text = Component.empty();
        if (blocks > 0)
            text.append(blocks + "").append(Component.translatable("volume.unit.big.short"));
        if (pixels > 0)
            text.append((blocks > 0 ? " " : "") + TooltipUtils.print(pixels) + "").append(Component.translatable("volume.unit.small.short"));
        return text;
    }
    
    public static final NamedTypeRegistry<LittleMeasurement> REGISTRY = new NamedTypeRegistry<LittleMeasurement>().addConstructorPattern(Level.class, CompoundTag.class);
    
    @Nullable
    public static LittleMeasurement load(Level level, CompoundTag nbt) {
        try {
            return REGISTRY.create(nbt.getString("type"), level, nbt);
        } catch (RegistryException e) {
            return null;
        }
    }
    
    static {
        REGISTRY.register("line", LittleMeasurementLine.class);
        REGISTRY.register("box", LittleMeasurementBox.class);
        REGISTRY.register("area", LittleMeasurementArea.class);
        REGISTRY.register("volume", LittleMeasurementVolume.class);
    }
    
    public int color;
    
    public LittleMeasurement(Level level, CompoundTag nbt) {
        this.color = nbt.getInt("color");
    }
    
    public LittleMeasurement() {}
    
    public abstract RenderMeasurementResult build(PreviewRenderer renderer, PoseStack pose, BufferBuilder builder, Vec3 cam);
    
    public abstract void overlay(PreviewRenderer renderer, OverlayRenderer overlay, Vec3 cam);
    
    public abstract void collectPositions(List<LittleBoxLevel> positions);
    
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
        
        protected final LittleBoxLevel first;
        protected final LittleBoxLevel second;
        
        public LittleMeasurementSimple(Level level, CompoundTag nbt) throws LittleActionException {
            super(level, nbt);
            first = LittleBoxLevel.of(level, nbt.get("0"));
            second = LittleBoxLevel.of(level, nbt.get("1"));
            changed();
        }
        
        public LittleMeasurementSimple(List<LittleBoxLevel> positions) {
            this.first = positions.get(0);
            this.second = positions.get(1);
            changed();
        }
        
        @Override
        public void collectPositions(List<LittleBoxLevel> positions) {
            positions.add(first);
            positions.add(second);
        }
        
        @Override
        protected void saveExtra(CompoundTag nbt) {
            nbt.put("0", first.write());
            nbt.put("1", second.write());
        }
        
    }
    
}
