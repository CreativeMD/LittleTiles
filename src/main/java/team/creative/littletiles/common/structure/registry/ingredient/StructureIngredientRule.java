package team.creative.littletiles.common.structure.registry.ingredient;

import java.util.function.Supplier;

import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.math.vec.LittleVec;

public class StructureIngredientRule implements IStructureIngredientRule {
    
    public static final StructureIngredientScaler SINGLE = x -> 1;
    
    public static final StructureIngredientScaler LONGEST_SIDE = group -> {
        LittleVec vec = group.getSize();
        int side = vec.x;
        if (side < vec.y)
            side = vec.y;
        if (side < vec.z)
            side = vec.z;
        return group.getGrid().toVanillaGrid(side);
    };
    
    public static final StructureIngredientScaler VOLUME = group -> group.getVolume();
    
    public final StructureIngredientScaler scale;
    public final Supplier<LittleIngredient> ingredient;
    
    public StructureIngredientRule(StructureIngredientScaler scale, Supplier<LittleIngredient> ingredient) {
        this.scale = scale;
        this.ingredient = ingredient;
    }
    
    @Override
    public void add(LittleGroup group, LittleIngredients ingredients) {
        double volume = scale.calculate(group);
        if (volume > 0) {
            LittleIngredient toAdd = ingredient.get();
            toAdd.scaleAdvanced(volume);
            ingredients.add(toAdd);
        }
    }
    
    public static interface StructureIngredientScaler {
        
        public double calculate(LittleGroup group);
        
    }
    
    public static class StructureIngredientScalerVolume implements StructureIngredientScaler {
        
        public final double scale;
        
        public StructureIngredientScalerVolume(double scale) {
            this.scale = scale;
        }
        
        @Override
        public double calculate(LittleGroup group) {
            return VOLUME.calculate(group) * scale;
        }
        
    }
    
}
