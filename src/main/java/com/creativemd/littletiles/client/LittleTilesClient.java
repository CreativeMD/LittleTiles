package com.creativemd.littletiles.client;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.client.render.SpecialBlockTilesRenderer;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.server.LittleTilesServer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LittleTilesClient extends LittleTilesServer{
	
	public static KeyBinding flip;
	public static boolean pressedFlip = false;
	
	public static KeyBinding mark;
	public static boolean pressedMark = false;
	
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
		//ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLittleTiles.class, new SpecialBlockTilesRenderer());
		
		CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTile);
		
		/*MinecraftForgeClient.registerItemRenderer(LittleTiles.recipe, renderer);
		MinecraftForgeClient.registerItemRenderer(LittleTiles.multiTiles, renderer);*/
		
		BlockTile.mc = Minecraft.getMinecraft();
		
		MinecraftForge.EVENT_BUS.register(new PreviewRenderer());
		
		up = new KeyBinding("key.rotateup", Keyboard.KEY_UP, "key.categories.littletiles");
		down = new KeyBinding("key.rotatedown", Keyboard.KEY_DOWN, "key.categories.littletiles");
		right = new KeyBinding("key.rotateright", Keyboard.KEY_RIGHT, "key.categories.littletiles");
		left = new KeyBinding("key.rotateleft", Keyboard.KEY_LEFT, "key.categories.littletiles");
		
		flip = new KeyBinding("key.flip", Keyboard.KEY_F, "key.categories.littletiles");
		mark = new KeyBinding("key.mark", Keyboard.KEY_M, "key.categories.littletiles");
		
		ClientRegistry.registerKeyBinding(up);
		ClientRegistry.registerKeyBinding(down);
		ClientRegistry.registerKeyBinding(right);
		ClientRegistry.registerKeyBinding(left);
		ClientRegistry.registerKeyBinding(flip);
		ClientRegistry.registerKeyBinding(mark);
	}
	
}
