package com.creativemd.littletiles.common.mod.chiselsandbits;

import java.util.List;

import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTile;

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
    
    public static boolean isChiselsAndBitsStructure(BlockState state) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(state);
        return false;
    }
    
    public static boolean isChiselsAndBitsStructure(ItemStack stack) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(stack);
        return false;
    }
    
    public static boolean isChiselsAndBitsStructure(BlockEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(te);
        return false;
    }
    
    public static LittlePreviews getPreviews(BlockEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getPreviews(te);
        return null;
    }
    
    public static List<LittleTile> getTiles(BlockEntity te) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getTiles(te);
        return null;
    }
    
    public static Object getVoxelBlob(BETiles te, boolean force) throws Exception {
        return ChiselsAndBitsInteractor.getVoxelBlob(te, force);
    }
}
