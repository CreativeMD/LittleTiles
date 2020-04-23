package com.creativemd.littletiles.common.structure.directional;

import java.lang.reflect.Field;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing.Axis;

public class StructureDirectionalField {
	
	public final Field field;
	public final String key;
	public final String saveKey;
	public final StructureDirectional annotation;
	public final StructureDirectionalType type;
	
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
	
	public Object createAndSet(LittleStructure structure, NBTTagCompound nbt) {
		Object relative = create(nbt);
		set(structure, relative);
		return relative;
	}
	
	public Object create(NBTTagCompound nbt) {
		return type.read(nbt.getTag(saveKey));
	}
	
	public void save(NBTTagCompound nbt, Object value) {
		nbt.setTag(saveKey, type.write(value));
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
	
	public LittleGridContext getContext(Object value) {
		return type.getContext(value);
	}
	
	public void convertToSmallest(Object value) {
		type.convertToSmallest(value);
	}
	
	public PlacePreview getPlacePreview(Object value, LittlePreviews previews) {
		return type.getPlacePreview(value, previews, this);
	}
	
	public Object getDefault() {
		return type.getDefault();
	}
}
