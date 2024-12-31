package team.creative.littletiles.mixin.sodium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.SortType;
import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.TranslucentGeometryCollector;

@Mixin(TranslucentGeometryCollector.class)
public interface TranslucentGeometryCollectorAccessor {
    
    @Accessor
    public void setSortType(SortType type);
}
