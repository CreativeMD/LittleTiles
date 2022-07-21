package team.creative.littletiles.common.ingredient;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.littletiles.common.api.block.LittleBlock;

public class BlockIngredientEntry {
    
    public final LittleBlock block;
    public double value;
    
    BlockIngredientEntry(LittleBlock block, double value) {
        this.block = block;
        this.value = value;
    }
    
    public ItemStack getBlockStack() {
        return block.getStack();
    }
    
    @Override
    public int hashCode() {
        return block.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof BlockIngredientEntry && ((BlockIngredientEntry) object).block == this.block;
    }
    
    public BlockState getState() {
        return block.getState();
    }
    
    public boolean is(ItemStack stack) {
        return block.is(stack);
    }
    
    public BlockIngredientEntry copy() {
        return new BlockIngredientEntry(block, value);
    }
    
    public BlockIngredientEntry copy(double value) {
        return new BlockIngredientEntry(this.block, value);
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("block", block.blockName());
        nbt.putDouble("volume", value);
        return nbt;
    }
    
    public boolean isEmpty() {
        return value <= 0;
    }
    
    public void scale(int count) {
        value *= count;
    }
    
    public void scaleAdvanced(double scale) {
        this.value = (int) Math.ceil(this.value * scale);
    }
    
    @Override
    public String toString() {
        return "[" + block.blockName() + "," + value + "]";
    }
    
}
