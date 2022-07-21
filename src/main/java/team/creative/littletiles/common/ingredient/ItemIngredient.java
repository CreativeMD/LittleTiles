package team.creative.littletiles.common.ingredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.text.TextBuilder;

public class ItemIngredient extends LittleIngredient<ItemIngredient> implements Iterable<ItemIngredientEntry> {
    
    private List<ItemIngredientEntry> content = new ArrayList<>();
    
    public ItemIngredient() {}
    
    public ItemIngredient(CreativeIngredient ingredient) {
        content.add(new ItemIngredientEntry(ingredient, 1));
    }
    
    public ItemIngredient(CreativeIngredient... ingredients) {
        for (int i = 0; i < ingredients.length; i++)
            content.add(new ItemIngredientEntry(ingredients[i], 1));
    }
    
    @Override
    public Iterator<ItemIngredientEntry> iterator() {
        return content.iterator();
    }
    
    @Override
    public ItemIngredient copy() {
        ItemIngredient copy = new ItemIngredient();
        content.forEach((x) -> copy.content.add(x.copy()));
        return copy;
    }
    
    @Override
    public TextBuilder toText() {
        TextBuilder text = new TextBuilder();
        for (ItemIngredientEntry entry : content)
            text.text(entry.toString()).stack(entry.ingredient.getExample());
        return text;
    }
    
    public ItemIngredientEntry add(ItemIngredientEntry ingredient) {
        if (ingredient.isEmpty())
            return null;
        
        for (int i = 0; i < content.size(); i++) {
            ItemIngredientEntry entry = content.get(i);
            if (entry.is(ingredient))
                entry.count += ingredient.count;
        }
        return ingredient;
    }
    
    @Override
    public ItemIngredient add(ItemIngredient ingredient) {
        ItemIngredient remaings = null;
        for (ItemIngredientEntry entry : ingredient.content) {
            ItemIngredientEntry remaing = add(entry);
            if (remaing != null) {
                if (remaings == null)
                    remaings = new ItemIngredient();
                remaings.add(remaing);
            }
        }
        return remaings;
    }
    
    public ItemIngredientEntry sub(ItemIngredientEntry ingredient) {
        if (ingredient.isEmpty())
            return null;
        
        for (int i = content.size() - 1; i >= 0; i--) {
            ItemIngredientEntry entry = content.get(i);
            if (entry.equals(ingredient)) {
                entry.count -= ingredient.count;
                if (entry.count <= 0) {
                    content.remove(i);
                    if (entry.count < 0) {
                        ingredient = entry;
                        ingredient.count = -ingredient.count;
                        continue;
                    }
                }
                
                return null;
            }
        }
        
        return ingredient;
    }
    
    @Override
    public ItemIngredient sub(ItemIngredient ingredient) {
        ItemIngredient remaings = null;
        for (ItemIngredientEntry entry : ingredient.content) {
            ItemIngredientEntry remaing = sub(entry);
            if (remaing != null) {
                if (remaings == null)
                    remaings = new ItemIngredient();
                remaings.add(remaing);
            }
        }
        return remaings;
    }
    
    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }
    
    @Override
    public void scale(int count) {
        for (ItemIngredientEntry entry : content)
            entry.scale(count);
    }
    
    @Override
    public void scaleAdvanced(double scale) {
        for (ItemIngredientEntry entry : content)
            entry.scaleAdvanced(scale);
    }
    
    @Override
    public int getMinimumCount(ItemIngredient other, int availableCount) {
        throw new UnsupportedOperationException();
    }
    
    public List<ItemIngredientEntry> getContent() {
        return content;
    }
    
    @Override
    public void print(TextBuilder text) {
        if (content.size() <= 4)
            for (ItemIngredientEntry entry : content)
                text.stack(entry.ingredient.getExample()).text(" " + entry.count + " " + entry.toString()).newLine();
        else
            for (ItemIngredientEntry entry : content)
                text.stack(entry.ingredient.getExample()).text(" " + entry.count + " ");
    }
    
    @Override
    public String toString() {
        return content.toString();
    }
}
