package com.creativemd.littletiles.common.mod.chiselsandbits;

import java.util.List;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Loader;

public class ChiselsAndBitsManager {
    
    public static final String chiselsandbitsID = "chiselsandbits";
    
    private static boolean isinstalled = Loader.isModLoaded(chiselsandbitsID);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    /** Keeping the grid size of C&B variable, maybe it does change some time **/
    public static int convertingFrom = 16;
    
    public static LittlePreviews getPreviews(ItemStack stack) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getPreviews(stack);
        return null;
    }
    
    public static boolean isChiselsAndBitsStructure(IBlockState state) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(state);
        return false;
    }
    
    public static boolean isChiselsAndBitsStructure(ItemStack stack) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(stack);
        return false;
    }
    
    public static boolean isChiselsAndBitsStructure(TileEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(te);
        return false;
    }
    
    public static LittlePreviews getPreviews(TileEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getPreviews(te);
        return null;
    }
    
    public static List<LittleTile> getTiles(TileEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getTiles(te);
        return null;
    }
    
    public static Object getVoxelBlob(TileEntityLittleTiles te, boolean force) throws Exception {
        if (!LittleTiles.CONFIG.general.allowConverationToChiselsAndBits)
            throw new Exception("Disabled");
        return ChiselsAndBitsInteractor.getVoxelBlob(te, force);
    }
}
