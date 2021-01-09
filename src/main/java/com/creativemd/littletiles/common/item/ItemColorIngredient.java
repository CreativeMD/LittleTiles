package com.creativemd.littletiles.common.item;

import java.util.List;

import javax.annotation.Nullable;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.api.ILittleIngredientInventory;
import com.creativemd.littletiles.common.util.ingredient.ColorIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;
import com.creativemd.littletiles.common.util.ingredient.LittleInventory;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemColorIngredient extends Item implements ILittleIngredientInventory {
    
    public static int states = 6;
    
    public ColorIngredientType type;
    
    public ItemColorIngredient(ColorIngredientType type) {
        this.type = type;
        hasSubtypes = true;
        setCreativeTab(LittleTiles.littleTab);
        setMaxStackSize(1);
    }
    
    public static ItemStack generateItemStack(ColorIngredientType type, int value) {
        ItemStack stack;
        switch (type) {
        case black:
            stack = new ItemStack(LittleTiles.blackColorIngredient);
            break;
        case cyan:
            stack = new ItemStack(LittleTiles.cyanColorIngredient);
            break;
        case magenta:
            stack = new ItemStack(LittleTiles.magentaColorIngredient);
            break;
        case yellow:
            stack = new ItemStack(LittleTiles.yellowColorIngredient);
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
    
    public ColorIngredient loadIngredient(ItemStack stack) {
        if (stack.hasTagCompound()) {
            ColorIngredient ingredient = new ColorIngredient();
            type.setIngredient(ingredient, stack.getTagCompound().getInteger("value"));
            
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
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        
        stack.getTagCompound().setInteger("value", type.getIngredient(color));
    }
    
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
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
            double stateSize = (double) ColorIngredient.bottleSize / (double) states;
            int state = Math.min(5, (int) (type.getIngredient(color) / stateSize));
            stack.setItemDamage(state);
            return;
        }
        
        stack.setTagCompound(null);
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
            public String print(ColorIngredient ingredient) {
                return ingredient.getBlackDescription();
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
            public String print(ColorIngredient ingredient) {
                return ingredient.getCyanDescription();
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
            public String print(ColorIngredient ingredient) {
                return ingredient.getMagentaDescription();
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
            public String print(ColorIngredient ingredient) {
                return ingredient.getYellowDescription();
            }
        };
        
        public abstract int getIngredient(ColorIngredient ingredient);
        
        public abstract void setIngredient(ColorIngredient ingredient, int value);
        
        public abstract String print(ColorIngredient ingredient);
        
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