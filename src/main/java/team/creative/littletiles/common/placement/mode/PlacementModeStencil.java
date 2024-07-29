package team.creative.littletiles.common.placement.mode;

import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.block.little.tile.LittleTile;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.placement.PlacementContext;
import team.creative.littletiles.common.structure.LittleStructure;

public class PlacementModeStencil extends PlacementMode {
    
    public PlacementModeStencil(PreviewMode mode) {
        super(mode, true);
    }
    
    @Override
    public boolean shouldConvertBlock() {
        return true;
    }
    
    @Override
    public boolean checkAll() {
        return false;
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        return null;
    }
    
    @Override
    public LittleIngredients getBeforePlaceIngredients(HolderLookup.Provider provider, LittleGroup previews) {
        return new LittleIngredients();
    }
    
    @Override
    public boolean placeTile(PlacementContext context, LittleStructure structure, LittleTile tile) throws LittleActionException {
        if (!context.collisionTest)
            return false;
        
        return context.removeTile(tile);
    }
}
