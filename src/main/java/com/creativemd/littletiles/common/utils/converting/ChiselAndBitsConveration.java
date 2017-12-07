package com.creativemd.littletiles.common.utils.converting;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.mods.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

public class ChiselAndBitsConveration {
	
	public static ConcurrentLinkedQueue<TileEntity> tileentities = new ConcurrentLinkedQueue<>();
	
	
	@SubscribeEvent
	public static void worldTick(WorldTickEvent event)
	{
		World world = event.world;
		if(!world.isRemote && event.phase == Phase.END)
		{
			int progress = 0;
			int size = tileentities.size();
			if(!tileentities.isEmpty())
				System.out.println("Attempting to convert " + size + " blocks ...");
			while(!tileentities.isEmpty())
			{
				TileEntity te = tileentities.poll();
				List<LittleTile> tiles = ChiselsAndBitsManager.getTiles(te);
				if(tiles != null && tiles.size() > 0)
				{
					te.getWorld().setBlockState(te.getPos(), LittleTiles.blockTile.getDefaultState());
					TileEntity tileEntity = te.getWorld().getTileEntity(te.getPos());
					
					for (LittleTile tile : tiles) {
						tile.te = (TileEntityLittleTiles) tileEntity;
						tile.place();
					}
				}
				progress++;
				if(progress % 100 == 0)
					System.out.println("Converted " + progress + "/" + size + " blocks ...");
			}
			if(size > 0)
				System.out.println("Converted " + size + " blocks ...");
		}
	}
	
	public static void onAddedTileEntity(TileEntity te)
	{
		if(ChiselsAndBitsManager.isInstalled() && ChiselsAndBitsManager.isChiselsAndBitsStructure(te))
			tileentities.add(te);
	}
	
}
