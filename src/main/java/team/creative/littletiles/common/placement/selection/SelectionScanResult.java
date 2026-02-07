package team.creative.littletiles.common.placement.selection;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselsAndBitsManager;

public class SelectionScanResult {
    
    public final Level level;
    
    private int blocks;
    
    private int ltBlocks = 0;
    private int ltTiles = 0;
    private LittleGrid minLtGrid = null;
    
    private int cbBlocks = 0;
    private int cbTiles = 0;
    private LittleGrid minCBGrid = null;
    
    private MutableBlockPos min = null;
    private MutableBlockPos max = null;
    
    public SelectionScanResult(Level level) {
        this.level = level;
    }
    
    public boolean hasBlocks() {
        return blocks > 0;
    }
    
    public List<Component> blockInfo() {
        return new TextBuilder().text(blocks + " ").translate("selection.blocks").build();
    }
    
    public boolean hasTiles() {
        return ltBlocks > 0;
    }
    
    public List<Component> ltInfo() {
        return new TextBuilder().text(ltBlocks + " ").translate("gui.blocks").text(", " + ltTiles + " ").translate("gui.tiles").text(", " + minLtGrid.count + " ").translate(
            "gui.grid").build();
    }
    
    public boolean hasCB() {
        return cbBlocks > 0;
    }
    
    public List<Component> cbInfo() {
        return new TextBuilder().text(cbBlocks + " ").translate("gui.blocks").text(", " + cbTiles + " ").translate("gui.tiles").text(", " + minCBGrid.count + " ").translate(
            "gui.grid").build();
    }
    
    public LittleGrid minGrid(boolean includeLT, boolean includeCB) {
        LittleGrid minRequired = LittleGrid.MIN;
        if (includeLT && minLtGrid != null)
            minRequired = LittleGrid.max(minRequired, minLtGrid);
        if (includeCB && minCBGrid != null)
            minRequired = LittleGrid.max(minRequired, minCBGrid);
        return minRequired;
    }
    
    private void addBlockDirectly(Level level, BlockPos pos) {
        BlockEntity te = level.getBlockEntity(pos);
        if (te instanceof BETiles) {
            ltBlocks++;
            ltTiles += ((BETiles) te).tilesCount();
            if (minLtGrid == null)
                minLtGrid = ((BETiles) te).getGrid();
            else
                minLtGrid = LittleGrid.max(minLtGrid, ((BETiles) te).getGrid());
        }
        
        LittleGroup specialPreviews = ChiselsAndBitsManager.getGroup(te);
        if (specialPreviews != null) {
            cbBlocks++;
            cbTiles += specialPreviews.size();
            if (minCBGrid == null)
                minCBGrid = specialPreviews.getGrid();
            else
                minCBGrid = LittleGrid.max(minCBGrid, specialPreviews.getGrid());
        }
        
        if (LittleAction.isBlockValid(level.getBlockState(pos)))
            blocks++;
    }
    
    public void addBlock(BlockPos pos) {
        if (min == null) {
            min = pos.mutable();
            max = pos.mutable();
        } else {
            min.set(Math.min(min.getX(), pos.getX()), Math.min(min.getY(), pos.getY()), Math.min(min.getZ(), pos.getZ()));
            max.set(Math.max(max.getX(), pos.getX()), Math.max(max.getY(), pos.getY()), Math.max(max.getZ(), pos.getZ()));
        }
        addBlockDirectly(level, pos);
    }
    
    protected void addBlocksWorld(Level level, BlockPos pos, BlockPos pos2) {
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        if (min == null) {
            min = new MutableBlockPos(minX, minY, minZ);
            max = new MutableBlockPos(maxX, maxY, maxZ);
        } else {
            min.set(Math.min(min.getX(), minX), Math.min(min.getY(), minY), Math.min(min.getZ(), minZ));
            max.set(Math.max(max.getX(), minX), Math.max(max.getY(), minY), Math.max(max.getZ(), minZ));
        }
        
        MutableBlockPos mutPos = new MutableBlockPos();
        for (int posX = minX; posX <= maxX; posX++)
            for (int posY = minY; posY <= maxY; posY++)
                for (int posZ = minZ; posZ <= maxZ; posZ++)
                    addBlockDirectly(level, mutPos.set(posX, posY, posZ));
    }
    
    public void addBlocks(BlockPos pos, BlockPos pos2) {
        int minX = Math.min(pos.getX(), pos2.getX());
        int minY = Math.min(pos.getY(), pos2.getY());
        int minZ = Math.min(pos.getZ(), pos2.getZ());
        int maxX = Math.max(pos.getX(), pos2.getX());
        int maxY = Math.max(pos.getY(), pos2.getY());
        int maxZ = Math.max(pos.getZ(), pos2.getZ());
        
        addBlocksWorld(level, pos, pos2);
        
        for (LittleEntity entity : LittleTiles.ANIMATION_HANDLERS.get(level).find(new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1)))
            addBlocksWorld((Level) entity.getSubLevel(), pos, pos2);
    }
    
    public Vec3i getSize() {
        return new Vec3i(max.getX() - min.getX(), max.getY() - min.getY(), max.getZ() - min.getZ());
    }
    
}