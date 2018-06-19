package com.creativemd.littletiles.common.utils.converting;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.ARBConditionalRenderInverted;

import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.utils.CubeObject;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tiles.preview.LittlePreviews;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.nbt.LittleNBTCompressionTools;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.util.JsonBlendingMode;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class StructureStringUtils {
	
	@SideOnly(Side.CLIENT)
	public static String exportModel(ItemStack stack)
	{
		if(stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe))
		{
			JsonObject object = new JsonObject();
			NBTTagCompound nbt = new NBTTagCompound();
			LittlePreviews previews = null;
			LittleStructure structure = null;
			if(stack.getItem() instanceof ItemRecipe)
			{
				previews = LittleTilePreview.getPreview(stack);
				structure = ItemMultiTiles.getLTStructure(stack);
			}else{
				ILittleTile tile = PlacementHelper.getLittleInterface(stack);
				previews = tile.getLittlePreview(stack);
				structure = tile.getLittleStructure(stack);
			}
			
			List<String> texturenames = new ArrayList<>();
			List<? extends RenderCubeObject> cubes = ((ICreativeRendered)stack.getItem()).getRenderingCubes(null, null, stack);
			JsonArray elements = new JsonArray();
			for (int i = 0; i < cubes.size(); i++) {
				RenderCubeObject cube = cubes.get(i);
				
				JsonObject element = new JsonObject();
				element.addProperty("name", "littletile_" + i);
				
				JsonArray positionArray = new JsonArray();
				positionArray.add(new JsonPrimitive(cube.minX*16));
				positionArray.add(new JsonPrimitive(cube.minY*16));
				positionArray.add(new JsonPrimitive(cube.minZ*16));
				element.add("from", positionArray);
				
				positionArray = new JsonArray();
				positionArray.add(new JsonPrimitive(cube.maxX*16));
				positionArray.add(new JsonPrimitive(cube.maxY*16));
				positionArray.add(new JsonPrimitive(cube.maxZ*16));
				element.add("to", positionArray);
				
				IBakedModel blockModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(cube.getBlockState());
				
				JsonObject faces = new JsonObject();
				for (int j = 0; j < EnumFacing.VALUES.length; j++) {
					EnumFacing facing = EnumFacing.VALUES[j];
					List<BakedQuad> quads = CreativeBakedModel.getBakedQuad(null, cube, cube.getOffset(), cube.getBlockState(), blockModel, null, facing, 0, true);
					if(!quads.isEmpty()) //No support for grass!!!
					{
						JsonObject face = new JsonObject();
						
						BakedQuad quad = quads.get(0);
						if(!texturenames.contains(quad.getSprite().getIconName()))
							texturenames.add(quad.getSprite().getIconName());
						int iconID = texturenames.indexOf(quad.getSprite().getIconName());
						face.addProperty("texture", "#" + iconID);
						JsonArray uv = new JsonArray();
						
						float minX = 16;
						float maxX = 0;
						float minY = 16;
						float maxY = 0;
						
						for (int k = 0; k < 4; k++) {
							int index = k * quad.getFormat().getIntegerSize();
							
							int uvIndex = index + quad.getFormat().getUvOffsetById(0) / 4;
							float u = quad.getSprite().getUnInterpolatedU(Float.intBitsToFloat(quad.getVertexData()[uvIndex]));
							minX = Math.min(minX, u);
							maxX = Math.max(maxX, u);
							
							float v = quad.getSprite().getUnInterpolatedV(Float.intBitsToFloat(quad.getVertexData()[uvIndex+1]));
							minY = Math.min(minY, v);
							maxY = Math.max(maxY, v);
						}
						
						uv.add(new JsonPrimitive(minX));
						uv.add(new JsonPrimitive(minY));
						uv.add(new JsonPrimitive(maxX));
						uv.add(new JsonPrimitive(maxY));
						
						face.add("uv", uv);
						faces.add(facing.getName(), face);
					}
				}
				element.add("faces", faces);
				elements.add(element);
			}
			object.add("elements", elements);
			
			JsonObject textures = new JsonObject();
			for (int j = 0; j < texturenames.size(); j++) {
				textures.addProperty("" + j, texturenames.get(j));
			}
			object.add("textures", textures);
			object.addProperty("__comment", "Model generated by LittleTiles");
			return object.toString();
		}
		return "";
	}
	
	public static String exportStructure(ItemStack stack)
	{
		String text = "";
		if(stack != null && (PlacementHelper.isLittleBlock(stack) || stack.getItem() instanceof ItemRecipe))
		{
			NBTTagCompound nbt = new NBTTagCompound();
			LittlePreviews previews = null;
			LittleStructure structure = null;
			if(stack.getItem() instanceof ItemRecipe)
			{
				previews = LittleTilePreview.getPreview(stack);
				structure = ItemMultiTiles.getLTStructure(stack);
			}else{
				ILittleTile tile = PlacementHelper.getLittleInterface(stack);
				previews = tile.getLittlePreview(stack);
				structure = tile.getLittleStructure(stack);
			}
			
			previews.context.setOverall(nbt);	
			nbt.setTag("tiles", LittleNBTCompressionTools.writePreviews(previews));
			
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
		try {
			return importStructure(JsonToNBT.getTagFromJson(input));
		} catch (NBTException e) {
			e.printStackTrace();
		}
		return ItemStack.EMPTY;
	}
	
	public static ItemStack importStructure(NBTTagCompound nbt)
	{
		NBTTagCompound itemNBT = new NBTTagCompound();
		
		ItemStack stack = new ItemStack(LittleTiles.recipe);
		
		stack.setTagCompound(itemNBT);
		
		if(nbt.hasKey("structure"))
			itemNBT.setTag("structure", nbt.getCompoundTag("structure"));
		
		if(nbt.getTag("tiles") instanceof NBTTagInt)
		{
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
		}else{
			LittleTilePreview.savePreviewTiles(LittleNBTCompressionTools.readPreviews(LittleGridContext.getOverall(nbt), nbt.getTagList("tiles", 10)), stack);
		}
		
		
		return stack;
	}
}
