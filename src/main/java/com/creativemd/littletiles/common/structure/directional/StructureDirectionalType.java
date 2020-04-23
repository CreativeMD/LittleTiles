package com.creativemd.littletiles.common.structure.directional;

import java.lang.reflect.Field;
import java.util.HashMap;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

public abstract class StructureDirectionalType<T> {
	
	private static HashMap<Class, StructureDirectionalType> types = new HashMap<>();
	
	public static StructureDirectionalType getType(Field field) {
		StructureDirectionalType type = types.get(field.getType());
		if (type == null)
			throw new RuntimeException("No registered directional type for " + field.getType() + ", " + field.getName());
		return type;
	}
	
	public static <T> void registerType(Class<T> clazz, StructureDirectionalType<T> type) {
		if (types.containsKey(clazz))
			throw new IllegalArgumentException("Type already exists. " + clazz);
		
		types.put(clazz, type);
	}
	
	static {
		registerType(EnumFacing.class, new StructureDirectionalType<EnumFacing>() {
			@Override
			public EnumFacing read(NBTBase nbt) {
				if (nbt instanceof NBTTagInt)
					return EnumFacing.VALUES[((NBTTagInt) nbt).getInt()];
				return EnumFacing.EAST;
			}
			
			@Override
			public NBTBase write(EnumFacing value) {
				return new NBTTagInt(value.ordinal());
			}
			
			@Override
			public EnumFacing move(EnumFacing value, LittleGridContext context, LittleVec offset) {
				return value;
			}
			
			@Override
			public EnumFacing flip(EnumFacing value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
				if (axis == value.getAxis())
					return value.getOpposite();
				return value;
			}
			
			@Override
			public EnumFacing rotate(EnumFacing value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
				return RotationUtils.rotate(value, rotation);
			}
			
			@Override
			public EnumFacing getDefault() {
				return EnumFacing.EAST;
			}
		});
		registerType(Axis.class, new StructureDirectionalType<Axis>() {
			
			@Override
			public Axis read(NBTBase nbt) {
				if (nbt instanceof NBTTagInt)
					return Axis.values()[((NBTTagInt) nbt).getInt()];
				return Axis.X;
			}
			
			@Override
			public NBTBase write(Axis value) {
				return new NBTTagInt(value.ordinal());
			}
			
			@Override
			public Axis move(Axis value, LittleGridContext context, LittleVec offset) {
				return value;
			}
			
			@Override
			public Axis flip(Axis value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
				return value;
			}
			
			@Override
			public Axis rotate(Axis value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
				return RotationUtils.rotate(value, rotation);
			}
			
			@Override
			public Axis getDefault() {
				return Axis.X;
			}
			
		});
		registerType(StructureRelative.class, new StructureDirectionalType<StructureRelative>() {
			
			@Override
			public StructureRelative read(NBTBase nbt) {
				if (nbt instanceof NBTTagIntArray)
					return new StructureRelative(((NBTTagIntArray) nbt).getIntArray());
				return null;
			}
			
			@Override
			public NBTBase write(StructureRelative value) {
				return new NBTTagIntArray(value.write());
			}
			
			@Override
			public StructureRelative move(StructureRelative value, LittleGridContext context, LittleVec offset) {
				value.move(context, offset);
				return value;
			}
			
			@Override
			public StructureRelative flip(StructureRelative value, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
				value.flip(context, axis, doubledCenter);
				return value;
			}
			
			@Override
			public StructureRelative rotate(StructureRelative value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
				value.rotate(context, rotation, doubledCenter);
				return value;
			}
			
			@Override
			public LittleGridContext getContext(StructureRelative value) {
				return value.getContext();
			}
			
			@Override
			public void convertToSmallest(StructureRelative value) {
				value.convertToSmallest();
			}
			
			@Override
			public PlacePreview getPlacePreview(StructureRelative value, LittlePreviews previews, StructureDirectionalField field) {
				return value.getPlacePreview(previews, field);
			}
			
			@Override
			public StructureRelative getDefault() {
				return new StructureRelative(new LittleBox(0, 0, 0, 1, 1, 1), LittleGridContext.get());
			}
			
		});
	}
	
	public abstract T read(NBTBase nbt);
	
	public abstract NBTBase write(T value);
	
	public abstract T move(T value, LittleGridContext context, LittleVec offset);
	
	public abstract T flip(T value, LittleGridContext context, Axis axis, LittleVec doubledCenter);
	
	public abstract T rotate(T value, LittleGridContext context, Rotation rotation, LittleVec doubledCenter);
	
	public abstract T getDefault();
	
	public LittleGridContext getContext(T value) {
		return null;
	}
	
	public void convertToSmallest(T value) {
		
	}
	
	public PlacePreview getPlacePreview(T value, LittlePreviews previews, StructureDirectionalField field) {
		return null;
	}
	
}
