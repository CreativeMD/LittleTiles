package com.creativemd.littletiles;

import net.minecraft.block.material.Material;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.packet.LittlePacket;
import com.creativemd.littletiles.common.packet.RecieveHandler;
import com.creativemd.littletiles.common.sorting.LittleTileSortingList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.server.LittleTilesServer;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles")
public class LittleTiles {
	
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "0.1";
	
	public static BlockTile blockTile = new BlockTile(Material.rock);
	public static SimpleNetworkWrapper network;
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		GameRegistry.registerBlock(blockTile, ItemBlockTiles.class, "BlockLittleTiles");
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		
		proxy.loadSide();
		
		LittleTile.registerLittleTile(LittleTile.class, "BlockTile");
		LittleTile.registerLittleTile(LittleTileTileEntity.class, "BlockTileEntity");
		
		network = NetworkRegistry.INSTANCE.newSimpleChannel("LittleTilePacket");
		network.registerMessage(RecieveHandler.class, LittlePacket.class, 0, Side.SERVER);
    }
	
	@EventHandler
    public void LoadComplete(FMLLoadCompleteEvent event)
    {
		LittleTileSortingList.initVanillaBlocks();
    }
}
