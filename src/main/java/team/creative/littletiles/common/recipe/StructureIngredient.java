package team.creative.littletiles.common.recipe;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class StructureIngredient implements ICustomIngredient {
    
    public static final MapCodec<StructureIngredient> CODEC = RecordCodecBuilder.<StructureIngredient>mapCodec(instance -> instance.group(Codec.STRING.fieldOf("structure")
            .forGetter(x -> x.structureType)).apply(instance, StructureIngredient::new));
    
    public final String structureType;
    
    public StructureIngredient(String structureType) {
        this.structureType = structureType;
    }
    
    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack.getItem() instanceof ItemMultiTiles)
            return ItemMultiTiles.getStructure(stack).equals(structureType);
        else if (stack.getItem() instanceof ItemPremadeStructure)
            return ItemPremadeStructure.getPremadeId(stack).equals(structureType);
        return false;
    }
    
    @Override
    public boolean isSimple() {
        return false;
    }
    
    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(ItemPremadeStructure.of(structureType));
    }
    
    @Override
    public IngredientType<?> getType() {
        return LittleTilesRegistry.STRUCTURE_INGREDIENT_TYPE.get();
    }
}
