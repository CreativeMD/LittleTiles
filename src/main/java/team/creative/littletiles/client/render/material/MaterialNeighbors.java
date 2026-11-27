package team.creative.littletiles.client.render.material;

import java.util.HashSet;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.block.entity.BETiles;

final class MaterialNeighbors {
    private MaterialNeighbors() {}
    static NeighborMaterials<BlockState> ordinary(BlockState state) {
        return NeighborMaterials.of(ConnectedMaterialState.supports(state)
            ? List.of(ConnectedMaterialState.key(state)) : List.of());
    }
    static NeighborMaterials<BlockState> tiles(BETiles tiles) {
        if (tiles == null || !tiles.hasLoaded()) return NeighborMaterials.unavailable();
        var materials = new HashSet<BlockState>();
        for (var pair : tiles.allTiles()) {
            BlockState state = pair.value.getState();
            if (ConnectedMaterialState.supports(state) && pair.value.iterator().hasNext())
                materials.add(ConnectedMaterialState.key(state));
        }
        return NeighborMaterials.of(materials);
    }
}
