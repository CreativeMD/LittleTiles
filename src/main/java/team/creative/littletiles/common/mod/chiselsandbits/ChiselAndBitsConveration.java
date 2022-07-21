package team.creative.littletiles.common.mod.chiselsandbits;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent.LevelTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;

public class ChiselAndBitsConveration {
    
    public static ConcurrentLinkedQueue<BlockEntity> blockEntities = new ConcurrentLinkedQueue<>();
    
    @SubscribeEvent
    public static void worldTick(LevelTickEvent event) {
        Level level = event.level;
        if (!level.isClientSide && event.phase == Phase.END) {
            LittleGrid chiselContext = LittleGrid.get(ChiselsAndBitsManager.convertingFrom);
            int progress = 0;
            int size = blockEntities.size();
            if (!blockEntities.isEmpty())
                System.out.println("Attempting to convert " + size + " blocks ...");
            while (!blockEntities.isEmpty()) {
                BlockEntity be = blockEntities.poll();
                LittleGroup tiles = ChiselsAndBitsManager.getGroup(be);
                if (tiles != null && !tiles.isEmpty()) {
                    be.getLevel().setBlockAndUpdate(be.getBlockPos(), BlockTile.getState(false, false));
                    BETiles tileEntity = (BETiles) be.getLevel().getBlockEntity(be.getBlockPos());
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
