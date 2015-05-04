package com.creativemd.littletiles;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.sorting.LittleTileSortingList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.server.LittleTilesServer;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles")
public class LittleTiles {
	
	@Instance(LittleTiles.modid)
	public static LittleTiles instance = new LittleTiles();
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "0.1";
	
	public static BlockTile blockTile = new BlockTile(Material.rock);
	
	public static Item hammer = new ItemHammer().setUnlocalizedName("LTHammer");
	public static Item recipe = new ItemRecipe().setUnlocalizedName("LTRecipe");
	public static Item multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles");
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		GameRegistry.registerItem(hammer, "hammer");
		GameRegistry.registerItem(recipe, "recipe");
		
		GameRegistry.registerBlock(blockTile, ItemBlockTiles.class, "BlockLittleTiles");
		
		GameRegistry.registerItem(multiTiles, "multiTiles");
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		
		proxy.loadSide();
		
		LittleTile.registerLittleTile(LittleTile.class, "BlockTile");
		LittleTile.registerLittleTile(LittleTileTileEntity.class, "BlockTileEntity");
		
		CreativeCorePacket.registerPacket(LittlePlacePacket.class, "LittlePlace");
		CreativeCorePacket.registerPacket(LittleBlockPacket.class, "LittleBlock");
		FMLCommonHandler.instance().bus().register(new LittleEvent());
		MinecraftForge.EVENT_BUS.register(new LittleEvent());
		
		//Recipes
		GameRegistry.addRecipe(new ItemStack(hammer),  new Object[]
				{
				"XAX", "AXA", "AXA", 'X', Items.iron_ingot
				});
    }
	
	@EventHandler
    public void LoadComplete(FMLLoadCompleteEvent event)
    {
		LittleTileSortingList.initVanillaBlocks();
    }
}
