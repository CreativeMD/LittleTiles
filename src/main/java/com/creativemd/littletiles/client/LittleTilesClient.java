package com.creativemd.littletiles.client;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.server.LittleTilesServer;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleTilesClient extends LittleTilesServer{
	
	public static int modelID;
	
	public static SpecialBlockTilesRenderer renderer = new SpecialBlockTilesRenderer();
	
	@Override
	public void loadSide()
	{
		modelID = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(modelID, renderer);
		
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(LittleTiles.blockTile), renderer);
		
		BlockTile.mc = Minecraft.getMinecraft();
		
		FMLCommonHandler.instance().bus().register(new PreviewRenderer());
	}
	
}
