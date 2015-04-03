package com.creativemd.littletiles;

import net.minecraft.block.material.Material;

import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles")
public class LittleTiles {
	
	public static final String modid = "littletiles";
	public static final String version = "0.1";
	
	public BlockTile blockTile = new BlockTile(Material.rock);
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		
		LittleTile.registerLittleTile(LittleTile.class, "BlockTile");
		LittleTile.registerLittleTile(LittleTileTileEntity.class, "BlockTileEntity");
    }
}
