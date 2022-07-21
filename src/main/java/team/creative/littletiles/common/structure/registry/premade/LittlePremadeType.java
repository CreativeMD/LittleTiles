package team.creative.littletiles.common.structure.registry.premade;

import java.util.function.BiFunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;

public class LittlePremadeType extends LittleStructureType {
    
    public final String modid;
    public boolean showInCreativeTab = true;
    public boolean snapToGrid = true;
    
    public <T extends LittleStructure> LittlePremadeType(String id, Class<T> structureClass, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid) {
        super(id, structureClass, factory, attribute.premade());
        this.modid = modid;
    }
    
    public CreativeModeTab getCustomTab() {
        return null;
    }
    
    public ItemStack createItemStackEmpty() {
        return new ItemStack(LittleTilesRegistry.PREMADE.get());
    }
    
    public ItemStack createItemStack() {
        ItemStack stack = createItemStackEmpty();
        CompoundTag structureNBT = new CompoundTag();
        structureNBT.putString("id", id);
        CompoundTag stackNBT = new CompoundTag();
        stackNBT.put("structure", structureNBT);
        stack.setTag(stackNBT);
        return stack;
    }
    
    public LittlePremadeType setNotShowCreativeTab() {
        this.showInCreativeTab = false;
        return this;
    }
    
    public LittlePremadeType setNotSnapToGrid() {
        this.snapToGrid = false;
        return this;
    }
    
    @Override
    public boolean canOnlyBePlacedByItemStack() {
        return true;
    }
    
}
