package com.creativemd.littletiles.common.utils.converting;

import java.util.ArrayList;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.ILittleTile;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.utils.LittleTilePreview;
import com.creativemd.littletiles.common.utils.PlacementHelper;
import com.creativemd.littletiles.common.utils.small.LittleTileBox;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;

public class StructureStringUtils {
	
	public static String exportStructure(ItemStack stack)
	{
		String text = "";
		if(stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe))
		{
			NBTTagCompound nbt = new NBTTagCompound();
			ArrayList<LittleTilePreview> previews = null;
			LittleStructure structure = null;
			if(stack.getItem() instanceof ItemRecipe)
			{
				previews = ItemRecipe.getPreview(stack);
				structure = ItemMultiTiles.getLTStructure(stack);
			}else{
				ILittleTile tile = PlacementHelper.getLittleInterface(stack);
				previews = tile.getLittlePreview(stack);
				structure = tile.getLittleStructure(stack);
			}
			
			nbt.setInteger("tiles", previews.size());
			ArrayList<String> blockNames = new ArrayList<>();
			for (int i = 0; i < previews.size(); i++) {
				if(previews.get(i).box != null)
				{
					LittleTileBox box = previews.get(i).box;
					String blockName = previews.get(i).getPreviewBlockName();
					if(!blockNames.contains(blockName))
						blockNames.add(blockName);
					
					String tileString = blockNames.indexOf(blockName) + "." + previews.get(i).getPreviewBlockMeta();
					if(previews.get(i).hasColor())
						tileString += "." + previews.get(i).getColor();
					
					nbt.setString("" + i, box.minX+"."+box.minY+"."+box.minZ+"."+box.maxX+"."+box.maxY+"."+box.maxZ+"."+tileString);
				}
			}
			
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < blockNames.size(); i++) {
				if(i > 0)
					builder.append(".");
				builder.append(blockNames.get(i));
			}
			nbt.setString("names", builder.toString());
			
			if(structure != null)
			{
				NBTTagCompound nbtStructure = new NBTTagCompound();
				structure.writeToNBT(nbtStructure);
				nbt.setTag("structure", nbtStructure);
			}
			text = nbt.toString();
		}
		return text;
	}
	
	public static ItemStack importStructure(String input)
	{
		try{
			NBTTagCompound nbt = JsonToNBT.getTagFromJson(input);
			
			NBTTagCompound itemNBT = new NBTTagCompound();
			if(nbt.hasKey("structure"))
				itemNBT.setTag("structure", nbt.getCompoundTag("structure"));
			
			String[] names = nbt.getString("names").split("\\.");
			
			int tiles = nbt.getInteger("tiles");
			for (int i = 0; i < tiles; i++) {
				String[] entries = nbt.getString("" + i).split("\\.");
				
				if(entries.length >= 8)
				{
					NBTTagCompound tileNBT = new NBTTagCompound();
					LittleTileBox box = new LittleTileBox(Integer.parseInt(entries[0]), Integer.parseInt(entries[1]), Integer.parseInt(entries[2]), Integer.parseInt(entries[3]), Integer.parseInt(entries[4]), Integer.parseInt(entries[5]));
					tileNBT.setString("block", names[Integer.parseInt(entries[6])]);
					tileNBT.setInteger("meta", Integer.parseInt(entries[7]));
					if(entries.length >= 9)
						tileNBT.setInteger("color", Integer.parseInt(entries[8]));
					box.writeToNBT("bBox", tileNBT);
					tileNBT.setString("tID", "BlockTileBlock");
					itemNBT.setTag("tile" + i, tileNBT);
					
				}
			}
			
			itemNBT.setInteger("tiles", tiles);
			
			ItemStack stack = new ItemStack(LittleTiles.recipe);
			stack.setTagCompound(itemNBT);
			return stack;
		}catch(Exception e){
			//e.printStackTrace();
		}
		return null;
	}
}
