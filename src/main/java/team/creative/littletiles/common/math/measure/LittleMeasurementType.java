package team.creative.littletiles.common.math.measure;

import java.util.List;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2BooleanFunction;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;

public record LittleMeasurementType(Int2BooleanFunction points, Function<List<LittleBoxAbsolute>, LittleMeasurement> factory) {
    
    public static final NamedHandlerRegistry<LittleMeasurementType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static final Int2BooleanFunction TWO_POINTS = x -> x >= 2;
    
    static {
        REGISTRY.register("line", new LittleMeasurementType(TWO_POINTS, LittleMeasurementLine::new));
        REGISTRY.registerDefault("box", new LittleMeasurementType(TWO_POINTS, LittleMeasurementBox::new));
    }
    
    public Component translatable() {
        return Component.translatable("building.measurement." + REGISTRY.getId(this));
    }
}
