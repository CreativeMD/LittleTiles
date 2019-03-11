package com.creativemd.littletiles.common.structure.registry;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.relative.LTStructureAnnotation;
import com.creativemd.littletiles.common.structure.relative.StructureRelative;

import net.minecraft.nbt.NBTTagCompound;

public final class LittleStructureType {
	
	public final String id;
	public final String category;
	public final Class<? extends LittleStructure> structureClass;
	public final LittleStructureAttribute attribute;
	public final List<StructureTypeRelative> relatives;
	
	LittleStructureType(String id, String category, Class<? extends LittleStructure> structureClass, LittleStructureAttribute attribute) {
		this.id = id;
		this.category = category;
		this.structureClass = structureClass;
		this.attribute = attribute;
		
		this.relatives = new ArrayList<>();
		for (Field field : structureClass.getFields())
			if (field.isAnnotationPresent(LTStructureAnnotation.class))
				relatives.add(new StructureTypeRelative(field, field.getAnnotation(LTStructureAnnotation.class)));
	}
	
	public LittleStructure createStructure() {
		try {
			return structureClass.getConstructor(LittleStructureType.class).newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException("Invalid structure type " + id);
		}
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof LittleStructureType && ((LittleStructureType) object).structureClass == this.structureClass;
	}
	
	@Override
	public String toString() {
		return structureClass.toString();
	}
	
	public class StructureTypeRelative {
		public final Field field;
		public final String key;
		public final String saveKey;
		public final LTStructureAnnotation annotation;
		
		public StructureTypeRelative(Field field, LTStructureAnnotation annotation) {
			this.field = field;
			this.key = field.getName();
			this.saveKey = annotation.saveKey().isEmpty() ? key : annotation.saveKey();
			this.annotation = annotation;
		}
		
		public void setRelative(LittleStructure structure, StructureRelative relative) {
			try {
				field.set(structure, relative);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		
		public StructureRelative createRelative(NBTTagCompound nbt) {
			try {
				return (StructureRelative) field.getType().getConstructor(String.class, NBTTagCompound.class).newInstance(saveKey, nbt);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		
		public StructureRelative createAndSetRelative(LittleStructure structure, NBTTagCompound nbt) {
			StructureRelative relative = createRelative(nbt);
			setRelative(structure, relative);
			return relative;
		}
		
		public StructureRelative getRelative(LittleStructure structure) {
			try {
				return (StructureRelative) field.get(structure);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}