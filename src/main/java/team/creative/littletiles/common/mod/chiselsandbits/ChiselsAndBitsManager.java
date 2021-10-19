package team.creative.littletiles.common.mod.chiselsandbits;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;

public class ChiselsAndBitsManager {
    
    public static final String chiselsandbitsID = "chiselsandbits";
    
    private static boolean isinstalled = ModList.get().isLoaded(chiselsandbitsID);
    
    public static boolean isInstalled() {
        return isinstalled;
    }
    
    /** Keeping the grid size of C&B variable, maybe it does change some time **/
    public static int convertingFrom = 16;
    
    public static LittleGroup getGroup(ItemStack stack) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getGroup(stack);
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
    
    public static boolean isChiselsAndBitsStructure(BlockEntity be) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.isChiselsAndBitsStructure(be);
        return false;
    }
    
    public static LittleGroup getGroup(BlockEntity be) {
        if (isInstalled())
            return ChiselsAndBitsInteractor.getGroup(be);
        return null;
    }
}
