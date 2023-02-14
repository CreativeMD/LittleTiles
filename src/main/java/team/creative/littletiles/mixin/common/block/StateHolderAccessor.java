package team.creative.littletiles.mixin.common.block;

import java.util.Map;
import java.util.function.Function;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

@Mixin(StateHolder.class)
public interface StateHolderAccessor {
    
    @Accessor
    public static Function<Map.Entry<Property<?>, Comparable<?>>, String> getPROPERTY_ENTRY_TO_STRING_FUNCTION() {
        throw new UnsupportedOperationException();
    }
    
}
