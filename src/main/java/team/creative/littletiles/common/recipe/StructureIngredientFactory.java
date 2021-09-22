package team.creative.littletiles.common.recipe;

import com.creativemd.littletiles.common.structure.type.premade.LittleStructurePremade.LittleStructureTypePremade;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.JsonContext;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

public class StructureIngredientFactory implements IngredientFactory {
    
    @Override
    public Ingredient parse(JsonContext context, JsonObject json) {
        if (json.has("structure")) {
            
            ItemStack stack = new ItemStack(LittleTiles.premade);
            NBTTagCompound nbt = new NBTTagCompound();
            NBTTagCompound structureNBT = new NBTTagCompound();
            String id = json.get("structure").getAsString();
            
            if (!(LittleStructureRegistry.getStructureType(id) instanceof LittleStructureTypePremade))
                throw new JsonSyntaxException("Unkown structure type '" + json.get("structure").getAsString() + "'!");
            
            structureNBT.setString("id", id);
            nbt.setTag("structure", structureNBT);
            stack.setTagCompound(nbt);
            
            return Ingredient.fromStacks(stack.copy());
        }
        throw new JsonSyntaxException("Missing 'structure' type!");
    }
    
}
