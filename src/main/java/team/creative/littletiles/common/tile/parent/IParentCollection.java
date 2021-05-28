package team.creative.littletiles.common.tile.parent;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.littletiles.common.block.TETiles;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.common.world.ILevelProvider;

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
    
    public TETiles getTe();
    
    @Override
    public default World getLevel() {
        TETiles te = getTe();
        if (te.hasLevel())
            return te.getLevel();
        return te.getTempLevel();
    }
    
    public default BlockPos getPos() {
        return getTe().getBlockPos();
    }
    
    public default LittleGrid getGrid() {
        return getTe().getGrid();
    }
    
    @OnlyIn(value = Dist.CLIENT)
    public default LittleRenderBox getTileRenderingCube(LittleTile tile, LittleBox box, LittleGrid context, BlockRenderLayer layer) {
        LittleRenderBox renderBox = box.getRenderingCube(context, tile.block.getBlock(), tile.color);
        if (renderBox != null && isStructure() && LittleStructureAttribute.emissive(getAttribute()))
            renderBox.emissive = true;
        return renderBox;
    }
}
