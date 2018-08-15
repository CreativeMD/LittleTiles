package com.creativemd.littletiles.common.structure.premade;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.LittleStructure.LittleStructureEntry;
import com.creativemd.littletiles.common.structure.attributes.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade.LittleStructurePremadeEntry;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.google.common.base.Charsets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class LittleStructurePremade extends LittleStructure {
	
	private static HashMap<String, LittleStructurePremadeEntry> structurePreviews = new HashMap<>();
	
	private static JsonParser parser = new JsonParser();
	
	public static void registerPremadeStructureType(String id, Class<? extends LittleStructurePremade> classStructure)
	{
		registerStructureType(id, classStructure, LittleStructureAttribute.PREMADE);
		try {
			ItemStack stack = new ItemStack(LittleTiles.premade);
			NBTTagCompound structureNBT = new NBTTagCompound();
			structureNBT.setString("id", id);
			NBTTagCompound nbt = JsonToNBT.getTagFromJson(IOUtils.toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream("assets/littletiles/premade/" + id + ".struct"), Charsets.UTF_8));
			nbt.setTag("structure", structureNBT);
			stack.setTagCompound(nbt);
			LittlePreviews previews = LittleTilePreview.getPreview(stack);
			structurePreviews.put(id, new LittleStructurePremadeEntry(previews, stack));
			System.out.println("Loaded " + id + " model");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not load '" + id + "'! Structure will not be registered");
		}
		
		
	}
	
	public static LittleStructurePremadeEntry getStructurePremadeEntry(String id)
	{
		return structurePreviews.get(id);
	}
	
	public static Collection<LittleStructurePremadeEntry> getPremadeStructures()
	{
		return structurePreviews.values();
	}
	
	public static Set<String> getPremadeStructureIds()
	{
		return structurePreviews.keySet();
	}
	
	public static ItemStack getPremadeStack(String id)
	{
		return structurePreviews.get(id).stack.copy();
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
	
	@Override
	@SideOnly(Side.CLIENT)
	public void createControls(SubGui gui, LittleStructure structure) {
		
	}

	@Override
	@SideOnly(Side.CLIENT)
	public LittleStructure parseStructure(SubGui gui) {
		return null;
	}
	
	public static void initPremadeStructures()
	{
		registerPremadeStructureType("workbench", LittleWorkbench.class);
		registerPremadeStructureType("importer", LittleImporter.class);
		registerPremadeStructureType("exporter", LittleExporter.class);
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
