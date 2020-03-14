package com.creativemd.littletiles.common.structure.premade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType.LittleStructureTypePremade;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.google.common.base.Charsets;
import com.google.gson.JsonParser;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

public abstract class LittleStructurePremade extends LittleStructure {
	
	public LittleStructurePremade(LittleStructureType type) {
		super(type);
	}
	
	private static HashMap<String, LittleStructurePremadeEntry> structurePreviews = new HashMap<>();
	
	private static List<LittleStructureTypePremade> premadeStructures = new ArrayList<>();
	
	private static JsonParser parser = new JsonParser();
	
	public static void reloadPremadeStructures() {
		
		structurePreviews.clear();
		
		for (LittleStructureTypePremade type : premadeStructures) {
			try {
				ItemStack stack = new ItemStack(LittleTiles.premade);
				NBTTagCompound structureNBT = new NBTTagCompound();
				structureNBT.setString("id", type.id);
				NBTTagCompound nbt = JsonToNBT.getTagFromJson(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream("assets/" + type.modid + "/premade/" + type.id + ".struct"), Charsets.UTF_8));
				nbt.setTag("structure", structureNBT);
				stack.setTagCompound(nbt);
				LittlePreviews previews = LittlePreview.getPreview(stack);
				LittlePreview.savePreview(previews, stack);
				structurePreviews.put(type.id, new LittleStructurePremadeEntry(previews, stack));
				System.out.println("Loaded " + type.id + " model");
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Could not load '" + type.id + "'! Structure will not be registered");
			}
		}
	}
	
	public static void registerPremadeStructureType(String id, String modid, Class<? extends LittleStructurePremade> classStructure) {
		registerPremadeStructureType(id, modid, classStructure, LittleStructureAttribute.NONE);
	}
	
	public static void registerPremadeStructureType(String id, String modid, Class<? extends LittleStructurePremade> classStructure, int attribute) {
		premadeStructures.add((LittleStructureTypePremade) LittleStructureRegistry.registerStructureType(id, new LittleStructureTypePremade(id, "premade", classStructure, LittleStructureAttribute.PREMADE | attribute, modid), null));
	}
	
	public static LittleStructurePremadeEntry getStructurePremadeEntry(String id) {
		return structurePreviews.get(id);
	}
	
	public static Collection<LittleStructurePremadeEntry> getPremadeStructures() {
		return structurePreviews.values();
	}
	
	public static Set<String> getPremadeStructureIds() {
		return structurePreviews.keySet();
	}
	
	public static ItemStack tryGetPremadeStack(String id) {
		LittleStructurePremadeEntry entry = structurePreviews.get(id);
		if (entry != null)
			return entry.stack.copy();
		return ItemStack.EMPTY;
	}
	
	public static ItemStack getPremadeStack(String id) {
		return structurePreviews.get(id).stack.copy();
	}
	
	@Override
	public ItemStack getStructureDrop() {
		return getPremadeStack(type.id).copy();
	}
	
	@Override
	public boolean canOnlyBePlacedByItemStack() {
		return true;
	}
	
	public static void initPremadeStructures() {
		registerPremadeStructureType("workbench", LittleTiles.modid, LittleWorkbench.class);
		registerPremadeStructureType("importer", LittleTiles.modid, LittleImporter.class);
		registerPremadeStructureType("exporter", LittleTiles.modid, LittleExporter.class);
		
		registerPremadeStructureType("cable", LittleTiles.modid, LittleSignalCable.class, LittleStructureAttribute.EXTRA_COLLSION | LittleStructureAttribute.EXTRA_RENDERING);
	}
	
	public static class LittleStructurePremadeEntry {
		
		public final LittlePreviews previews;
		public final ItemStack stack;
		
		public LittleStructurePremadeEntry(LittlePreviews previews, ItemStack stack) {
			this.previews = previews;
			this.stack = stack;
		}
		
		public boolean arePreviewsEqual(LittlePreviews previews) {
			return this.previews.isVolumeEqual(previews);
		}
	}
	
}
