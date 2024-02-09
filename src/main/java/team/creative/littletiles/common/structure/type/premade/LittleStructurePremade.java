package team.creative.littletiles.common.structure.type.premade;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;

public abstract class LittleStructurePremade extends LittleStructure {
    
    public LittleStructurePremade(LittlePremadeType type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public ItemStack getStructureDrop() throws CorruptedConnectionException, NotYetConnectedException {
        ItemStack stack = LittlePremadeRegistry.createStack(type.id);
        
        checkConnections();
        BlockPos pos = getMinPos(getStructurePos().mutable());
        
        CompoundTag structureNBT = new CompoundTag();
        this.savePreview(structureNBT, pos);
        
        if (!stack.hasTag())
            stack.setTag(new CompoundTag());
        stack.getTag().put(LittleGroup.STRUCTURE_KEY, structureNBT);
        
        if (name != null) {
            CompoundTag display = new CompoundTag();
            display.putString("Name", name);
            stack.getTag().put("display", display);
        }
        return stack;
    }
    
    public static class LittlePremadeType extends LittleStructureType {
        
        public final String modid;
        public boolean showInCreativeTab = true;
        public boolean snapToGrid = true;
        
        public <T extends LittleStructurePremade> LittlePremadeType(String id, Class<T> structureClass, BiFunction<? extends LittlePremadeType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid) {
            super(id, structureClass, factory, attribute.premade());
            this.modid = modid;
        }
        
        public boolean hasCustomTab() {
            return false;
        }
        
        public ItemStack createItemStackEmpty() {
            return new ItemStack(LittleTilesRegistry.PREMADE.get());
        }
        
        public ItemStack createItemStack() {
            ItemStack stack = createItemStackEmpty();
            CompoundTag structureNBT = new CompoundTag();
            structureNBT.putString("id", id);
            CompoundTag stackNBT = new CompoundTag();
            stackNBT.put(LittleGroup.STRUCTURE_KEY, structureNBT);
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
        
        public boolean canSnapToGrid() {
            return true;
        }
        
        @Override
        public boolean tileCountAsIngredient(LittleGroup group) {
            return false;
        }
        
        @Override
        public void addIngredients(LittleGroup group, LittleIngredients ingredients) {
            super.addIngredients(group, ingredients);
            ingredients.add(new StackIngredient(createItemStack()));
        }
        
    }
}
