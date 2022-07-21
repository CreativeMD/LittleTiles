package team.creative.littletiles.common.ingredient;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.text.TextBuilder;
import team.creative.littletiles.common.action.LittleActionException;

public class NotEnoughIngredientsException extends LittleActionException {
    
    protected LittleIngredients ingredients;
    
    protected NotEnoughIngredientsException(String msg, LittleIngredient ingredient) {
        super(msg);
        this.ingredients = new LittleIngredients();
        this.ingredients.set(ingredient.getClass(), ingredient);
    }
    
    protected NotEnoughIngredientsException(String msg, LittleIngredients ingredients) {
        super(msg);
        this.ingredients = ingredients;
    }
    
    public NotEnoughIngredientsException(LittleIngredient ingredient) {
        this("exception.ingredient.missing", ingredient);
    }
    
    public NotEnoughIngredientsException(ItemStack stack) {
        this(new StackIngredient());
        ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
    }
    
    public NotEnoughIngredientsException(LittleIngredients ingredients) {
        super("exception.ingredient.missing");
        this.ingredients = ingredients;
    }
    
    public LittleIngredients getIngredients() {
        return ingredients;
    }
    
    @Override
    public List<Component> getActionMessage() {
        TextBuilder text = new TextBuilder().translate(getMessage()).newLine();
        for (LittleIngredient ingredient : ingredients)
            ingredient.print(text);
        return text.build();
    }
    
    public static class NotEnoughSpaceException extends NotEnoughIngredientsException {
        
        public NotEnoughSpaceException(LittleIngredient ingredient) {
            super("exception.ingredient.space", ingredient);
        }
        
        public NotEnoughSpaceException(LittleIngredients ingredients) {
            super("exception.ingredient.space", ingredients);
        }
        
        public NotEnoughSpaceException(ItemStack stack) {
            this(new StackIngredient());
            ingredients.get(StackIngredient.class).add(new StackIngredientEntry(stack, stack.getCount()));
        }
        
    }
}
