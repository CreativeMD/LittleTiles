package team.creative.littletiles.common.item;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.api.ingredient.ILittleIngredientInventory;
import team.creative.littletiles.common.ingredient.ColorIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.LittleInventory;

public class ItemColorIngredient extends Item implements ILittleIngredientInventory {
    
    public static int states = 6;
    
    public ColorIngredientType type;
    
    public ItemColorIngredient(ColorIngredientType type) {
        super(new Item.Properties().tab(LittleTiles.LITTLE_TAB).stacksTo(1));
        this.type = type;
    }
    
    public static ItemStack generateItemStack(ColorIngredientType type, int value) {
        ItemStack stack;
        switch (type) {
        case black:
            stack = new ItemStack(LittleTiles.BLACK_COLOR);
            break;
        case cyan:
            stack = new ItemStack(LittleTiles.CYAN_COLOR);
            break;
        case magenta:
            stack = new ItemStack(LittleTiles.MAGENTA_COLOR);
            break;
        case yellow:
            stack = new ItemStack(LittleTiles.YELLOW_COLOR);
            break;
        default:
            stack = ItemStack.EMPTY;
            break;
        }
        
        if (!stack.isEmpty()) {
            ColorIngredient color = new ColorIngredient();
            type.setIngredient(color, value);
            ((ItemColorIngredient) stack.getItem()).setInventory(stack, new LittleIngredients(color), null);
        }
        
        return stack;
    }
    
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        return false;
    }
    
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack me, ItemStack other, Slot p_150894_, ClickAction action, Player player, SlotAccess slot) {
        return false;
    }
    
    public ColorIngredient loadIngredient(ItemStack stack) {
        if (stack.hasTag()) {
            ColorIngredient ingredient = new ColorIngredient();
            type.setIngredient(ingredient, stack.getTag().getInt("value"));
            
            switch (type) {
            case black:
                ingredient.setLimit(ColorIngredient.bottleSize, 0, 0, 0);
                break;
            case cyan:
                ingredient.setLimit(0, ColorIngredient.bottleSize, 0, 0);
                break;
            case magenta:
                ingredient.setLimit(0, 0, ColorIngredient.bottleSize, 0);
                break;
            case yellow:
                ingredient.setLimit(0, 0, 0, ColorIngredient.bottleSize);
                break;
            default:
                break;
            }
            return ingredient;
        }
        return null;
    }
    
    public void saveIngredient(ItemStack stack, ColorIngredient color) {
        stack.getOrCreateTag().putInt("value", type.getIngredient(color));
    }
    
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {}
    
    @Override
    public boolean isComplex() {
        return true;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        ColorIngredient entry = loadIngredient(stack);
        if (entry != null)
            tooltip.add(type.print(entry));
    }
    
    @Override
    public LittleIngredients getInventory(ItemStack stack) {
        ColorIngredient color = loadIngredient(stack);
        if (color != null) {
            return new LittleIngredients(color) {
                @Override
                protected boolean canAddNewIngredients() {
                    return false;
                }
                
                @Override
                protected boolean removeEmptyIngredients() {
                    return false;
                }
            };
        }
        return null;
    }
    
    @Override
    public void setInventory(ItemStack stack, LittleIngredients ingredients, LittleInventory inventory) {
        ColorIngredient color = ingredients.get(ColorIngredient.class);
        if (color != null && type.getIngredient(color) > 0) {
            saveIngredient(stack, color);
            double stateSize = (double) ColorIngredient.bottleSize / states;
            int state = Math.min(5, (int) (type.getIngredient(color) / stateSize));
            stack.setDamageValue(state);
            return;
        }
        
        stack.setTag(null);
        stack.setCount(0);
    }
    
    @Override
    public boolean shouldBeMerged() {
        return true;
    }
    
    public static enum ColorIngredientType {
        black {
            @Override
            public int getIngredient(ColorIngredient ingredient) {
                return ingredient.black;
            }
            
            @Override
            public void setIngredient(ColorIngredient ingredient, int value) {
                ingredient.black = value;
            }
            
            @Override
            public Component print(ColorIngredient ingredient) {
                return new TextComponent(ingredient.getBlackDescription());
            }
        },
        cyan {
            @Override
            public int getIngredient(ColorIngredient ingredient) {
                return ingredient.cyan;
            }
            
            @Override
            public void setIngredient(ColorIngredient ingredient, int value) {
                ingredient.cyan = value;
            }
            
            @Override
            public Component print(ColorIngredient ingredient) {
                return new TextComponent(ingredient.getCyanDescription());
            }
        },
        magenta {
            @Override
            public int getIngredient(ColorIngredient ingredient) {
                return ingredient.magenta;
            }
            
            @Override
            public void setIngredient(ColorIngredient ingredient, int value) {
                ingredient.magenta = value;
            }
            
            @Override
            public Component print(ColorIngredient ingredient) {
                return new TextComponent(ingredient.getMagentaDescription());
            }
        },
        yellow {
            @Override
            public int getIngredient(ColorIngredient ingredient) {
                return ingredient.yellow;
            }
            
            @Override
            public void setIngredient(ColorIngredient ingredient, int value) {
                ingredient.yellow = value;
            }
            
            @Override
            public Component print(ColorIngredient ingredient) {
                return new TextComponent(ingredient.getYellowDescription());
            }
        };
        
        public abstract int getIngredient(ColorIngredient ingredient);
        
        public abstract void setIngredient(ColorIngredient ingredient, int value);
        
        public abstract Component print(ColorIngredient ingredient);
        
        public static ColorIngredientType getType(String type) {
            switch (type.toLowerCase()) {
            case "black":
                return ColorIngredientType.black;
            case "cyan":
                return ColorIngredientType.cyan;
            case "magenta":
                return ColorIngredientType.magenta;
            case "yellow":
                return ColorIngredientType.yellow;
            default:
                return null;
            }
        }
    }
}