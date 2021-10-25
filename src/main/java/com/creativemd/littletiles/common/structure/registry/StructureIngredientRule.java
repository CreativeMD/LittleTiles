package com.creativemd.littletiles.common.structure.registry;

import java.util.function.Supplier;

import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;

public class StructureIngredientRule implements IStructureIngredientRule {
    
    public static final StructureIngredientScaler SINGLE = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittlePreviews previews) {
            return 1;
        }
    };
    
    public static final StructureIngredientScaler LONGEST_SIDE = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittlePreviews previews) {
            LittleVec vec = previews.getSize();
            int side = vec.x;
            if (side < vec.y)
                side = vec.y;
            if (side < vec.z)
                side = vec.z;
            return previews.getContext().toVanillaGrid(side);
        }
    };
    
    public static final StructureIngredientScaler VOLUME = new StructureIngredientScaler() {
        
        @Override
        public double calculate(LittlePreviews previews) {
            return previews.getVolume();
        }
    };
    
    public final StructureIngredientScaler scale;
    public final Supplier<LittleIngredient> ingredient;
    
    public StructureIngredientRule(StructureIngredientScaler scale, Supplier<LittleIngredient> ingredient) {
        this.scale = scale;
        this.ingredient = ingredient;
    }
    
    @Override
    public void add(LittlePreviews previews, LittleIngredients ingredients) {
        double volume = scale.calculate(previews);
        if (volume > 0) {
            LittleIngredient toAdd = ingredient.get();
            toAdd.scaleAdvanced(volume);
            ingredients.add(toAdd);
        }
    }
    
    public static abstract class StructureIngredientScaler {
        
        public abstract double calculate(LittlePreviews previews);
        
    }
    
    public static class StructureIngredientScalerVolume extends StructureIngredientScaler {
        
        public final double scale;
        
        public StructureIngredientScalerVolume(double scale) {
            this.scale = scale;
        }
        
        @Override
        public double calculate(LittlePreviews previews) {
            return VOLUME.calculate(previews) * scale;
        }
        
    }
    
}
