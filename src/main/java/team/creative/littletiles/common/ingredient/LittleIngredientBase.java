package team.creative.littletiles.common.ingredient;

public abstract class LittleIngredientBase<T extends LittleIngredientBase> {
    
    public abstract T copy();
    
    /** adds the given ingredient (amount + amount)
     * 
     * @param ingredient
     * @return remaining ingredient, null if everything could be add */
    public abstract T add(T ingredient);
    
    /** subtracts the given ingredient (amount - amount)
     * 
     * @param ingredient
     * @return remaining ingredient, null if everything could be subtracted */
    public abstract T sub(T ingredient);
    
    public abstract boolean isEmpty();
    
}
