package com.creativemd.littletiles.client;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.client.render.SpecialBlockTilesRenderer;
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
	
	public static KeyBinding up;
	public static boolean pressedUp = false;
	public static KeyBinding down;
	public static boolean pressedDown = false;
	public static KeyBinding right;
	public static boolean pressedRight = false;
	public static KeyBinding left;
	public static boolean pressedLeft = false;
	
	@Override
	public void loadSide()
	{
		modelID = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(modelID, renderer);
		
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(LittleTiles.blockTile), renderer);
		MinecraftForgeClient.registerItemRenderer(LittleTiles.recipe, renderer);
		MinecraftForgeClient.registerItemRenderer(LittleTiles.multiTiles, renderer);
		
		BlockTile.mc = Minecraft.getMinecraft();
		
		FMLCommonHandler.instance().bus().register(new PreviewRenderer());
		MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
		
		up = new KeyBinding("key.rotateup", Keyboard.KEY_UP, "key.categories.littletiles");
		down = new KeyBinding("key.rotatedown", Keyboard.KEY_DOWN, "key.categories.littletiles");
		right = new KeyBinding("key.rotateright", Keyboard.KEY_RIGHT, "key.categories.littletiles");
		left = new KeyBinding("key.rotateleft", Keyboard.KEY_LEFT, "key.categories.littletiles");
	}
	
}
