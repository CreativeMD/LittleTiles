package team.creative.littletiles.common.block.little.tile.parent;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.level.ILevelProvider;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;

public interface IParentCollection extends Iterable<LittleTile>, ILevelProvider {
    
    public int size();
    
    public int totalSize();
    
    public boolean isStructure();
    
    public default boolean isStructureChildSafe(LittleStructure structure) {
        try {
            return isStructureChild(structure);
        } catch (CorruptedConnectionException | NotYetConnectedException e) {
            return false;
        }
    }
    
    public boolean isStructureChild(LittleStructure structure) throws CorruptedConnectionException, NotYetConnectedException;
    
    public boolean isMain();
    
    public LittleStructure getStructure() throws CorruptedConnectionException, NotYetConnectedException;
    
    public int getAttribute();
    
    public void setAttribute(int attribute);
    
    public boolean isClient();
    
    public BETiles getBE();
    
    @Override
    public default Level getLevel() {
        BETiles te = getBE();
        if (te.hasLevel())
            return te.getLevel();
        throw new IllegalStateException("BlockEntity not loaded yet");
    }
    
    public default BlockPos getPos() {
        return getBE().getBlockPos();
    }
    
    public default LittleGrid getGrid() {
        return getBE().getGrid();
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public default LittleRenderBox getTileRenderingCube(LittleTile tile, LittleBox box, LittleGrid grid) {
        LittleRenderBox renderBox = box.getRenderingCube(grid, tile);
        if (renderBox != null && isStructure() && LittleStructureAttribute.emissive(getAttribute()))
            renderBox.emissive = true;
        return renderBox;
    }
}
