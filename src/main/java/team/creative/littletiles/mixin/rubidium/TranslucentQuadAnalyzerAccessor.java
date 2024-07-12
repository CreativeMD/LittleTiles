package team.creative.littletiles.mixin.rubidium;

import org.embeddedt.embeddium.impl.render.chunk.sorting.TranslucentQuadAnalyzer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import it.unimi.dsi.fastutil.floats.FloatArrayList;

@Mixin(TranslucentQuadAnalyzer.class)
public interface TranslucentQuadAnalyzerAccessor {
    
    @Accessor(remap = false)
    public FloatArrayList getQuadCenters();
    
}
