package team.creative.littletiles.common.mod.chiselsandbits;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.grid.LittleGrid;

public class ChiselAndBitsConveration {
    
    public static ConcurrentLinkedQueue<BlockEntity> blockEntities = new ConcurrentLinkedQueue<>();
    
    @SubscribeEvent
    public static void worldTick(WorldTickEvent event) {
        Level world = event.world;
        if (!world.isClientSide && event.phase == Phase.END) {
            LittleGrid chiselContext = LittleGrid.get(ChiselsAndBitsManager.convertingFrom);
            int progress = 0;
            int size = blockEntities.size();
            if (!blockEntities.isEmpty())
                System.out.println("Attempting to convert " + size + " blocks ...");
            while (!blockEntities.isEmpty()) {
                BlockEntity te = blockEntities.poll();
                List<LittleTile> tiles = ChiselsAndBitsManager.getTiles(te);
                if (tiles != null && tiles.size() > 0) {
                    te.getLevel().setBlockAndUpdate(te.getBlockPos(), BlockTile.getState(false, false));
                    BETiles tileEntity = (BETiles) te.getLevel().getBlockEntity(te.getBlockPos());
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
