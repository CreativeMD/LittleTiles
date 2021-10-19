package team.creative.littletiles.common.block.little.element;

import net.minecraft.nbt.CompoundTag;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.common.api.block.LittleBlock;
import team.creative.littletiles.common.block.little.registry.LittleBlockRegistry;

public class LittleElement {
    
    protected LittleBlock block;
    public final int color;
    
    public LittleElement(LittleBlock block, int color) {
        this.block = block;
        this.color = color;
    }
    
    public LittleElement(CompoundTag nbt) {
        this.block = LittleBlockRegistry.get(nbt.getString("b"));
        this.color = nbt.contains("c") ? nbt.getInt("c") : ColorUtils.WHITE;
    }
    
    public LittleBlock getBlock() {
        return block;
    }
    
    @Override
    public int hashCode() {
        return block.hashCode() + color;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LittleElement)
            return ((LittleElement) obj).block == block && ((LittleElement) obj).color == color;
        return super.equals(obj);
    }
    
    public boolean is(LittleElement element) {
        return element.block == block && element.color == color;
    }
    
}
