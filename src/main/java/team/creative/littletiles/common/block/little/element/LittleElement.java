package team.creative.littletiles.common.block.little.element;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;

public class LittleElement {
    
    public static final Field PROPERTY_ENTRY_TO_STRING_FUNCTION_FIELD = ObfuscationReflectionHelper.findField(StateHolder.class, "f_61110_");
    
    public static LittleElement of(ItemStack stack, int color) throws NotBlockException {
        Block block = Block.byItem(stack.getItem());
        if (block != null && !(block instanceof AirBlock))
            return new LittleElement(block.defaultBlockState(), color);
        throw new NotBlockException();
    }
    
    private BlockState state;
    protected LittleBlock block;
    public final int color;
    
    public LittleElement(LittleElement element) {
        this.state = element.state;
        this.block = element.block;
        this.color = element.color;
    }
    
    public LittleElement(LittleElement element, int color) {
        this.state = element.state;
        this.block = element.block;
        this.color = color;
    }
    
    public LittleElement(BlockState state, int color) {
        setState(state);
        this.color = color;
    }
    
    @Deprecated
    public LittleElement(BlockState state, LittleBlock block, int color) {
        this.state = state;
        this.block = block;
        this.color = color;
    }
    
    public LittleElement(CompoundTag nbt) {
        this(nbt.getString("s"), nbt.contains("c") ? nbt.getInt("c") : ColorUtils.WHITE);
    }
    
    public LittleElement(String name, int color) {
        setState(LittleBlockRegistry.loadState(name));
        if (state.getBlock() instanceof AirBlock)
            this.block = LittleBlockRegistry.getMissing(name);
        this.color = color;
    }
    
    public BlockState getState() {
        return state;
    }
    
    public void setState(BlockState state) {
        if (this.state != state) {
            this.state = state;
            this.block = LittleBlockRegistry.get(state.getBlock());
        }
    }
    
    public LittleBlock getBlock() {
        return block;
    }
    
    public boolean hasColor() {
        return color != ColorUtils.WHITE;
    }
    
    @Override
    public int hashCode() {
        return block.hashCode() + color;
    }
    
    public String getBlockName() {
        if (getState().getBlock() instanceof AirBlock)
            return getBlock().blockName();
        else {
            StringBuilder name = new StringBuilder();
            BlockState state = getState();
            name.append(state.getBlock().getRegistryName());
            if (!state.getValues().isEmpty()) {
                name.append('[');
                try {
                    name.append(state.getValues().entrySet().stream().map((Function<Entry<Property<?>, Comparable<?>>, String>) PROPERTY_ENTRY_TO_STRING_FUNCTION_FIELD.get(null))
                            .collect(Collectors.joining(",")));
                    
                } catch (IllegalArgumentException | IllegalAccessException e) {}
                name.append(']');
            }
            return name.toString();
        }
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("s", getBlockName());
        nbt.putInt("c", color);
        return nbt;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleElement)
            return ((LittleElement) obj).state == state && ((LittleElement) obj).color == color;
        return super.equals(obj);
    }
    
    public boolean is(LittleElement element) {
        return element.state == state && element.block == block && element.color == color;
    }
    
    public static class NotBlockException extends Exception {}
    
}
