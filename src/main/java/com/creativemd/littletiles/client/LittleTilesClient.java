package com.creativemd.littletiles.client;

import org.lwjgl.input.Keyboard;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.client.rendering.model.CreativeBlockRenderHelper;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.core.CreativeCoreClient;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.server.LittleTilesServer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
		Minecraft mc = Minecraft.getMinecraft();
		
		CreativeBlockRenderHelper.registerCreativeRenderedBlock(LittleTiles.blockTile);
		
		CreativeCoreClient.registerBlockModels(LittleTiles.coloredBlock, LittleTiles.modid, "colored_block_", BlockLTColored.EnumType.values());
		
		CreativeCoreClient.registerItemRenderer(LittleTiles.hammer);
		CreativeCoreClient.registerItemRenderer(LittleTiles.recipe);
		CreativeCoreClient.registerItemRenderer(LittleTiles.saw);
		CreativeCoreClient.registerItemRenderer(LittleTiles.container);
		CreativeCoreClient.registerItemRenderer(LittleTiles.wrench);
		CreativeCoreClient.registerItemRenderer(LittleTiles.chisel);
		CreativeCoreClient.registerItemRenderer(LittleTiles.colorTube);
		CreativeCoreClient.registerItemRenderer(LittleTiles.rubberMallet);
		
		
		/*MinecraftForgeClient.registerItemRenderer(LittleTiles.recipe, renderer);
		MinecraftForgeClient.registerItemRenderer(LittleTiles.multiTiles, renderer);*/
		
		
		mc.getRenderItem().getItemModelMesher().register(LittleTiles.colorTube, new ItemMeshDefinition()
        {
            public ModelResourceLocation getModelLocation(ItemStack stack)
            {
                return new ModelResourceLocation("LTChisel", "inventory");
            }
        });
		mc.getItemColors().registerItemColorHandler(new IItemColor() {
			
			@Override
			@SideOnly(Side.CLIENT)
		    public int getColorFromItemstack(ItemStack stack, int color)
		    {
				if (color == 0)
		        	return ColorUtils.WHITE;
		        return ItemColorTube.getColor(stack);
		    }
			
		}, LittleTiles.colorTube);
		
		BlockTile.mc = mc;
		
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
