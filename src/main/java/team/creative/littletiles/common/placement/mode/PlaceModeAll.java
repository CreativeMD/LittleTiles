package team.creative.littletiles.common.placement.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;

public class PlaceModeAll extends PlaceModeNormal {
    
    public PlaceModeAll(String name, PreviewMode mode) {
        super(name, mode, false);
    }
    
    @Override
    public boolean canPlaceStructures() {
        return true;
    }
    
    @Override
    public PlacementMode place() {
        if (Screen.hasControlDown())
            return PlacementMode.overwrite;
        return super.place();
    }
    
    @Override
    public List<BlockPos> getCoordsToCheck(Set<BlockPos> splittedTiles, BlockPos pos) {
        return new ArrayList<>(splittedTiles);
    }
}
