package team.creative.littletiles.client.render.material;

import java.util.Collection;
import java.util.Set;

/** Material presence at a block position, independent of the subsequent LT cut geometry. */
public record NeighborMaterials<T>(boolean available, Set<T> materials) {
    public NeighborMaterials { materials = Set.copyOf(materials); }
    public static <T> NeighborMaterials<T> of(Collection<T> materials) {
        return new NeighborMaterials<>(true, Set.copyOf(materials));
    }
    public static <T> NeighborMaterials<T> unavailable() {
        return new NeighborMaterials<>(false, Set.of());
    }
    public boolean contains(T material) { return available && materials.contains(material); }
}
