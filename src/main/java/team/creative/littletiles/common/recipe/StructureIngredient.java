package team.creative.littletiles.common.recipe;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class StructureIngredient extends Ingredient {
    
    public static final Codec<StructureIngredient> CODEC = RecordCodecBuilder.create(b -> b.group(Codec.STRING.fieldOf("structure").forGetter(x -> x.structureType)).apply(b,
        StructureIngredient::new));
    
    public final String structureType;
    
    protected StructureIngredient(String structureType) {
        super(Stream.empty());
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
    
}
