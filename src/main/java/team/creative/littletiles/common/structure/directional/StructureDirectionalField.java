package team.creative.littletiles.common.structure.directional;

import java.lang.reflect.Field;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction.Axis;
import team.creative.creativecore.common.util.math.Rotation;
import team.creative.littletiles.common.grid.LittleGrid;

public class StructureDirectionalField {
    
    public final Field field;
    public final String key;
    public final String saveKey;
    public final StructureDirectional annotation;
    public final StructureDirectionalType type;
    private Object defaultValue;
    
    public StructureDirectionalField(Field field, StructureDirectional annotation) {
        this.field = field;
        this.key = field.getName();
        this.saveKey = annotation.saveKey().isEmpty() ? key : annotation.saveKey();
        this.annotation = annotation;
        this.type = StructureDirectionalType.getType(field);
    }
    
    public void set(LittleStructure structure, Object value) {
        try {
            field.set(structure, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object get(LittleStructure structure) {
        try {
            return field.get(structure);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Object createAndSet(LittleStructure structure, CompoundNBT nbt) {
        Object relative = create(nbt);
        set(structure, relative);
        return relative;
    }
    
    public void setDefault(Object value) {
        this.defaultValue = value;
    }
    
    public Object create(CompoundNBT nbt) {
        Object value = type.read(nbt.get(saveKey));
        if (value == null)
            if (defaultValue != null)
                return defaultValue;
            else
                return type.getDefault();
        return value;
    }
    
    public void save(CompoundNBT nbt, Object value) {
        nbt.put(saveKey, type.write(value));
    }
    
    public Object move(Object value, LittleGridContext context, LittleVec offset) {
        return type.move(value, context, offset);
    }
    
    public Object flip(Object value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
        return type.flip(value, context, axis, doubledCenter);
    }
    
    public Object rotate(Object value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
        return type.rotate(value, context, rotation, doubledCenter);
    }
    
    public LittleGrid getContext(Object value) {
        return type.getContext(value);
    }
    
    public void convertToSmallest(Object value) {
        type.convertToSmallest(value);
    }
    
    public void advancedScale(Object value, int from, int to) {
        type.advancedScale(value, from, to);
    }
    
    public PlacePreview getPlacePreview(Object value, LittlePreviews previews) {
        return type.getPlacePreview(value, previews, this);
    }
    
    public Object getDefault() {
        return defaultValue != null ? defaultValue : type.getDefault();
    }
}
