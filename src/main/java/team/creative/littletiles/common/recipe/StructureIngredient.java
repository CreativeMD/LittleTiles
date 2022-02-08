package team.creative.littletiles.common.recipe;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class StructureIngredient extends Ingredient {
    
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
    
    public static class StructureIngredientSerializer implements IIngredientSerializer<StructureIngredient> {
        
        public static final StructureIngredientSerializer INSTANCE = new StructureIngredientSerializer();
        
        @Override
        public StructureIngredient parse(FriendlyByteBuf buffer) {
            return create(buffer.readUtf());
        }
        
        @Override
        public StructureIngredient parse(JsonObject json) {
            if (json.has("structure"))
                return create(json.get("structure").getAsString());
            throw new JsonSyntaxException("Missing 'structure' type!");
        }
        
        public StructureIngredient create(String id) {
            return new StructureIngredient(id);
        }
        
        @Override
        public void write(FriendlyByteBuf buffer, StructureIngredient ingredient) {
            buffer.writeUtf(ingredient.structureType);
        }
        
    }
    
}
