package com.creativemd.littletiles.common.structure.registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.client.render.tile.LittleRenderingCube;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleStructureType {
	
	public final String id;
	public final String category;
	public final Class<? extends LittleStructure> clazz;
	public final int attribute;
	public final List<StructureDirectionalField> directional;
	
	public LittleStructureType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
		this.id = id;
		this.category = category;
		this.clazz = structureClass;
		this.attribute = attribute;
		
		this.directional = new ArrayList<>();
		for (Field field : structureClass.getFields())
			if (field.isAnnotationPresent(StructureDirectional.class))
				directional.add(new StructureDirectionalField(field, field.getAnnotation(StructureDirectional.class)));
	}
	
	public LittleStructure createStructure() {
		try {
			return clazz.getConstructor(LittleStructureType.class).newInstance(this);
		} catch (Exception e) {
			throw new RuntimeException("Invalid structure type " + id);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public List<LittleRenderingCube> getPositingCubes(World world, BlockPos pos, ItemStack stack) {
		return null;
	}
	
	@Override
	public boolean equals(Object object) {
		return object instanceof LittleStructureType && ((LittleStructureType) object).clazz == this.clazz;
	}
	
	@Override
	public String toString() {
		return clazz.toString();
	}
	
	public boolean canOnlyBePlacedByItemStack() {
		return false;
	}
	
	public void addIngredients(LittlePreviews previews, LittleIngredients ingredients) {
		
	}
	
	public void finializePreview(LittlePreviews previews) {
		
	}
	
	public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
		if (directional.isEmpty())
			return new ArrayList<>();
		
		List<PlacePreview> placePreviews = new ArrayList<>();
		
		for (StructureDirectionalField field : directional) {
			Object value = field.create(previews.structure);
			PlacePreview tile = getPlacePreview(value, field, previews);
			if (tile == null)
				continue;
			
			if (field.getContext(value).size < previews.getContext().size)
				tile.convertTo(field.getContext(value), previews.getContext());
			
			placePreviews.add(tile);
		}
		return placePreviews;
	}
	
	protected PlacePreview getPlacePreview(Object value, StructureDirectionalField type, LittlePreviews previews) {
		return type.getPlacePreview(value, previews);
	}
	
	public LittleGridContext getMinContext(LittlePreviews previews) {
		LittleGridContext context = LittleGridContext.getMin();
		
		for (StructureDirectionalField field : directional) {
			Object value = field.create(previews.structure);
			field.convertToSmallest(value);
			LittleGridContext fieldContext = field.getContext(value);
			if (fieldContext == null)
				continue;
			
			context = LittleGridContext.max(context, fieldContext);
			field.save(previews.structure, value);
		}
		return context;
	}
	
	public Object loadDirectional(LittlePreviews previews, String key) {
		for (StructureDirectionalField field : directional)
			if (field.key.equals(key))
				return field.create(previews.structure);
		return null;
	}
	
	public void move(LittleStructure structure, LittleGridContext context, LittleVec offset) {
		for (StructureDirectionalField field : directional) {
			Object value = field.get(structure);
			value = field.move(value, context, offset);
			field.set(structure, value);
		}
	}
	
	public void move(LittlePreviews previews, LittleGridContext context, LittleVec offset) {
		for (StructureDirectionalField field : directional) {
			Object value = field.create(previews.structure);
			value = field.move(value, context, offset);
			field.save(previews.structure, value);
		}
	}
	
	public void flip(LittlePreviews previews, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
		for (StructureDirectionalField field : directional) {
			Object value = field.create(previews.structure);
			value = field.flip(value, context, axis, doubledCenter);
			field.save(previews.structure, value);
		}
	}
	
	public void rotate(LittlePreviews previews, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
		for (StructureDirectionalField field : directional) {
			Object value = field.create(previews.structure);
			value = field.rotate(value, context, rotation, doubledCenter);
			field.save(previews.structure, value);
		}
	}
	
}