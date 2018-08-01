package com.creativemd.littletiles.common.structure;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.common.structure.LittleStructure.LittleStructureEntry;
import com.creativemd.littletiles.common.structure.attributes.LittleStructureAttribute;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class LittleStructurePremade extends LittleStructure {
	
	private static HashMap<String, LittleStructurePremadeEntry> structurePreviews = new HashMap<>();
	
	public static void registerPremadeStructureType(String id, Class<? extends LittleStructurePremade> classStructure, LittlePreviews previews, ItemStack stack)
	{
		registerStructureType(id, classStructure, LittleStructureAttribute.NO_DROP);
		
		structurePreviews.put(id, new LittleStructurePremadeEntry(previews, stack));
	}
	
	public static LittleStructurePremadeEntry getStructurePremadeEntry(String id)
	{
		return structurePreviews.get(id);
	}

	@Override
	public ItemStack getStructureDrop() {
		return getStructurePremadeEntry(structureID).stack.copy();
	}
	
	@Override
	public boolean canOnlyBePlacedByItemStack()
	{
		return true;
	}
	
	public static class LittleStructurePremadeEntry {
		
		public final LittlePreviews previews;
		public final ItemStack stack;
		
		public LittleStructurePremadeEntry(LittlePreviews previews, ItemStack stack) {
			this.previews = previews;
			this.stack = stack;
		}
		
		public boolean arePreviewsEqual(LittlePreviews previews)
		{
			return this.previews.isVolumeEqual(previews);
		}
	}

}
