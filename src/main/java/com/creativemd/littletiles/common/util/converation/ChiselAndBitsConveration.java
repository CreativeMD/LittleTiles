package com.creativemd.littletiles.common.util.converation;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.creativemd.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.tile.LittleTile;

public class ChiselAndBitsConveration {
    
    public static ConcurrentLinkedQueue<BlockEntity> blockEntities = new ConcurrentLinkedQueue<>();
    
    @SubscribeEvent
    public static void worldTick(WorldTickEvent event) {
        Level world = event.world;
        if (!world.isRemote && event.phase == Phase.END) {
            LittleGrid chiselContext = LittleGrid.get(ChiselsAndBitsManager.convertingFrom);
            int progress = 0;
            int size = blockEntities.size();
            if (!blockEntities.isEmpty())
                System.out.println("Attempting to convert " + size + " blocks ...");
            while (!blockEntities.isEmpty()) {
                TileEntity te = blockEntities.poll();
                List<LittleTile> tiles = ChiselsAndBitsManager.getTiles(te);
                if (tiles != null && tiles.size() > 0) {
                    te.getWorld().setBlockState(te.getPos(), BlockTile.getState(false, false));
                    TileEntityLittleTiles tileEntity = (TileEntityLittleTiles) te.getWorld().getTileEntity(te.getPos());
                    tileEntity.convertTo(chiselContext);
                    tileEntity.updateTiles((x) -> {
                        x.noneStructureTiles().addAll(tiles);
                    });
                    
                }
                progress++;
                if (progress % 100 == 0)
                    System.out.println("Converted " + progress + "/" + size + " blocks ...");
            }
            if (size > 0)
                System.out.println("Converted " + size + " blocks ...");
        }
    }
    
    public static void onAddedTileEntity(BlockEntity te) {
        if (ChiselsAndBitsManager.isInstalled() && ChiselsAndBitsManager.isChiselsAndBitsStructure(te))
            blockEntities.add(te);
    }
    
}
