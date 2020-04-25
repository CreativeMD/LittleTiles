package com.creativemd.littletiles.common.structure.type.premade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.item.ItemPremadeStructure;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalCable;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalCable.LittleStructureTypeCable;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalInput;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalInput.LittleStructureTypeInput;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalOutput;
import com.creativemd.littletiles.common.structure.type.premade.signal.LittleSignalOutput.LittleStructureTypeOutput;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.google.common.base.Charsets;
import com.google.gson.JsonParser;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructurePremade extends LittleStructure {
	
	public LittleStructurePremade(LittleStructureType type) {
		super(type);
	}
	
	private static LinkedHashMap<String, LittleStructurePremadeEntry> structurePreviews = new LinkedHashMap<>();
	private static List<LittleStructureTypePremade> premadeStructures = new ArrayList<>();
	
	private static JsonParser parser = new JsonParser();
	
	public static void reloadPremadeStructures() {
		
		structurePreviews.clear();
		ItemPremadeStructure.clearCache();
		
		for (LittleStructureTypePremade type : premadeStructures) {
			try {
				ItemStack stack = new ItemStack(LittleTiles.premade);
				NBTTagCompound structureNBT = new NBTTagCompound();
				structureNBT.setString("id", type.id);
				NBTTagCompound nbt = JsonToNBT.getTagFromJson(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream("assets/" + type.modid + "/premade/" + type.id + ".struct"), Charsets.UTF_8));
				nbt.setTag("structure", structureNBT);
				stack.setTagCompound(nbt);
				LittlePreviews previews = LittlePreview.getPreview(stack);
				
				NBTTagCompound stackNBT = new NBTTagCompound();
				stackNBT.setTag("structure", structureNBT);
				stack.setTagCompound(stackNBT);
				
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
		premadeStructures.add((LittleStructureTypePremade) LittleStructureRegistry.registerStructureType(new LittleStructureTypePremade(id, "premade", classStructure, attribute, modid), null));
	}
	
	public static void registerPremadeStructureType(LittleStructureTypePremade type) {
		premadeStructures.add((LittleStructureTypePremade) LittleStructureRegistry.registerStructureType(type, null));
	}
	
	public static LittlePreviews getPreviews(String id) {
		return getStructurePremadeEntry(id).previews;
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
	
	public static void initPremadeStructures() {
		registerPremadeStructureType("workbench", LittleTiles.modid, LittleWorkbench.class);
		registerPremadeStructureType("importer", LittleTiles.modid, LittleImporter.class);
		registerPremadeStructureType("exporter", LittleTiles.modid, LittleExporter.class);
		
		registerPremadeStructureType(new LittleStructureTypeCable("single_cable1", "premade", LittleSignalCable.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 1));
		registerPremadeStructureType(new LittleStructureTypeCable("single_cable4", "premade", LittleSignalCable.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 4));
		registerPremadeStructureType(new LittleStructureTypeCable("single_cable16", "premade", LittleSignalCable.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 16));
		
		registerPremadeStructureType(new LittleStructureTypeOutput("single_output1", "premade", LittleSignalOutput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 1));
		registerPremadeStructureType(new LittleStructureTypeOutput("single_output4", "premade", LittleSignalOutput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 4));
		registerPremadeStructureType(new LittleStructureTypeOutput("single_output16", "premade", LittleSignalOutput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 16));
		
		registerPremadeStructureType(new LittleStructureTypeInput("single_input1", "premade", LittleSignalInput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 1));
		registerPremadeStructureType(new LittleStructureTypeInput("single_input4", "premade", LittleSignalInput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 4));
		registerPremadeStructureType(new LittleStructureTypeInput("single_input16", "premade", LittleSignalInput.class, LittleStructureAttribute.EXTRA_RENDERING, LittleTiles.modid, 16));
	}
	
	public static class LittleStructureTypePremade extends LittleStructureType {
		
		public final String modid;
		
		public LittleStructureTypePremade(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid) {
			super(id, category, structureClass, LittleStructureAttribute.PREMADE | attribute);
			this.modid = modid;
		}
		
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes(LittlePreviews previews) {
			return null;
		}
		
		@Override
		public boolean canOnlyBePlacedByItemStack() {
			return true;
		}
		
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
