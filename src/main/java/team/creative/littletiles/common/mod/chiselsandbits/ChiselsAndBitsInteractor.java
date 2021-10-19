package team.creative.littletiles.common.mod.chiselsandbits;

import java.util.stream.Stream;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.vec.LittleVec;

public class ChiselsAndBitsInteractor {
    
    public static boolean isChiselsAndBitsStructure(BlockState state) {
        return IChiselsAndBitsAPI.getInstance().getConversionManager().getChiseledVariantOf(state).isPresent();
    }
    
    public static boolean isChiselsAndBitsStructure(ItemStack stack) {
        return IChiselsAndBitsAPI.getInstance().getConversionManager().getChiseledVariantOf(stack.getItem()).isPresent();
    }
    
    public static LittleGroup getGroup(Stream<IStateEntryInfo> stream) {
        LittleGroup group = new LittleGroup();
        LittleGrid grid = LittleGrid.get(ChiselsAndBitsManager.convertingFrom);
        stream.forEach(state -> {
            LittleBox box = new LittleBox(new LittleVec(grid, state.getStartPoint()), new LittleVec(grid, state.getEndPoint()));
            group.add(grid, new LittleElement(LittleBlockRegistry.get(state.getState()), ColorUtils.WHITE), box);
        });
        
        group.combine();
        return group;
    }
    
    public static LittleGroup getGroup(ItemStack stack) {
        if (isChiselsAndBitsStructure(stack))
            return getGroup(((IMultiStateItem) stack.getItem()).createItemStack(stack).stream());
        return null;
    }
    
    public static LittleGroup getGroup(BlockEntity te) {
        if (te instanceof IAreaAccessor)
            return getGroup(((IAreaAccessor) te).stream());
        return null;
    }
    
    public static boolean isChiselsAndBitsStructure(BlockEntity te) {
        return te instanceof IAreaAccessor;
    }
    
}
