package com.creativemd.littletiles.common.tile.parent;

import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.attribute.LittleStructureAttribute;
import com.creativemd.littletiles.common.structure.exception.CorruptedConnectionException;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IParentTileList extends Iterable<LittleTile> {
    
    public LittleTile first();
    
    public LittleTile last();
    
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
    
    public TileEntityLittleTiles getTe();
    
    public default World getWorld() {
        TileEntityLittleTiles te = getTe();
        if (te.hasWorld())
            return te.getWorld();
        return te.getTempWorld();
    }
    
    public default BlockPos getPos() {
        return getTe().getPos();
    }
    
    public default LittleGridContext getContext() {
        return getTe().getContext();
    }
    
    @SideOnly(Side.CLIENT)
    public default LittleRenderBox getTileRenderingCube(LittleTile tile, LittleGridContext context, BlockRenderLayer layer) {
        LittleRenderBox box = tile.getRenderingCube(context, layer);
        if (box != null && isStructure() && LittleStructureAttribute.emissive(getAttribute()))
            box.emissive = true;
        return box;
    }
}
