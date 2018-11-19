package com.creativemd.littletiles.common.structure.registry;

import javax.annotation.Nullable;

import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;

public final class LittleStructureType {
	
	public final String id;
	public final Class<? extends LittleStructure> structureClass;
	public final LittleStructureAttribute attribute;
	public final LittleStructurePreviewHandler handler;
	
	LittleStructureType(String id, Class<? extends LittleStructure> structureClass, LittleStructureAttribute attribute, @Nullable LittleStructurePreviewHandler handler) {
		this.id = id;
		this.structureClass = structureClass;
		this.attribute = attribute;
		if (handler == null)
			this.handler = new LittleStructurePreviewHandler();
		else
			this.handler = handler;
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
}