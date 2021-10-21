package team.creative.littletiles.common.block.little.element;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;

public class LittleElement {
    
    private BlockState state;
    protected LittleBlock block;
    public final int color;
    
    public LittleElement(LittleElement element) {
        this.state = element.state;
        this.block = element.block;
        this.color = element.color;
    }
    
    public LittleElement(BlockState state, int color) {
        setState(state);
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
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleElement)
            return ((LittleElement) obj).state == state && ((LittleElement) obj).color == color;
        return super.equals(obj);
    }
    
    public boolean is(LittleElement element) {
        return element.state == state && element.color == color;
    }
    
}
