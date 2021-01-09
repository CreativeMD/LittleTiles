package com.creativemd.littletiles.common.api.te;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.common.Optional.Method;

public interface ILittleTileTE {
    
    /** Returns a converted version of the TileEntity.
     * 
     * @param force
     *            if false an exception will be thrown if a tile cannot be
     *            converted, if true it will try to convert it anyway
     * @return VoxelBlob object from C&B
     * @throws Exception
     */
    @Method(modid = "chiselsandbits")
    public Object getVoxelBlob(boolean force) throws Exception;
    
    @Nullable
    public IBlockState getState(AxisAlignedBB box, boolean realistic);
    
}
