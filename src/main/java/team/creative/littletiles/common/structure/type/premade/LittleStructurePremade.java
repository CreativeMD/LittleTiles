package team.creative.littletiles.common.structure.type.premade;

import java.util.function.BiFunction;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleTool;
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
        this.savePreview(structureNBT, getStructureLevel().registryAccess(), pos);
        
        var data = ILittleTool.getData(stack);
        data.put(LittleGroup.STRUCTURE_KEY, structureNBT);
        ILittleTool.setData(stack, data);
        
        if (name != null)
            stack.set(DataComponents.ITEM_NAME, Component.literal(name));
        return stack;
    }
    
    public static class LittlePremadeType extends LittleStructureType {
        
        public final String path;
        public final String modid;
        public boolean showInCreativeTab = true;
        public boolean snapToGrid = true;
        public boolean itemTexture = false;
        @OnlyIn(Dist.CLIENT)
        private ModelResourceLocation texture;
        
        public <T extends LittleStructurePremade> LittlePremadeType(String id, String path, Class<T> structureClass, BiFunction<? extends LittlePremadeType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid) {
            super(id, structureClass, factory, attribute.premade());
            this.modid = modid;
            this.path = path;
        }
        
        public <T extends LittleStructurePremade> LittlePremadeType(String id, Class<T> structureClass, BiFunction<? extends LittlePremadeType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid) {
            this(id, "", structureClass, factory, attribute, modid);
        }
        
        public LittlePremadeType setItemTexture() {
            itemTexture = true;
            return this;
        }
        
        @OnlyIn(Dist.CLIENT)
        public ModelResourceLocation getItemTexture() {
            if (texture == null)
                texture = new ModelResourceLocation(ResourceLocation.tryBuild(modid, "item/premade/" + path + "" + id), ModelResourceLocation.STANDALONE_VARIANT);
            return texture;
        }
        
        public boolean hasCustomTab() {
            return false;
        }
        
        public ItemStack createItemStackEmpty() {
            return new ItemStack(LittleTilesRegistry.PREMADE.value());
        }
        
        public ItemStack createItemStack() {
            ItemStack stack = createItemStackEmpty();
            CompoundTag structureNBT = new CompoundTag();
            structureNBT.putString("id", id);
            CompoundTag stackNBT = new CompoundTag();
            stackNBT.put(LittleGroup.STRUCTURE_KEY, structureNBT);
            ILittleTool.setData(stack, stackNBT);
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
        public boolean tilesCountAsIngredient() {
            return false;
        }
        
        @Override
        public void addIngredients(Provider provider, LittleGroup group, LittleIngredients ingredients) {
            super.addIngredients(provider, group, ingredients);
            ingredients.add(new StackIngredient(createItemStack()));
        }
        
    }
}
