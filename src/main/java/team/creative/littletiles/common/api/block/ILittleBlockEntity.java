package team.creative.littletiles.common.api.block;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

public interface ILittleBlockEntity {
    
    /** Returns a converted version of the TileEntity.
     * 
     * @param force
     *            if false an exception will be thrown if a tile cannot be
     *            converted, if true it will try to convert it anyway
     * @return VoxelBlob object from C&B
     * @throws Exception
     */
    public Object getVoxelBlob(boolean force) throws Exception;
    
    @Nullable
    public Block getState(AABB box, boolean realistic);
    
}
