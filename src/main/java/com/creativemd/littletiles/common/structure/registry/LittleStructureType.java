package com.creativemd.littletiles.common.structure.registry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.littletiles.client.render.tile.LittleRenderBox;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.directional.StructureDirectionalField;
import com.creativemd.littletiles.common.structure.registry.StructureIngredientRule.StructureIngredientScaler;
import com.creativemd.littletiles.common.structure.signal.input.InternalSignalInput;
import com.creativemd.littletiles.common.structure.signal.logic.SignalMode;
import com.creativemd.littletiles.common.structure.signal.output.InternalSignalOutput;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredient;
import com.creativemd.littletiles.common.util.ingredient.LittleIngredients;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleStructureType {
    
    public final String id;
    public final String category;
    public final Class<? extends LittleStructure> clazz;
    public final int attribute;
    public final List<StructureDirectionalField> directional;
    public final List<InternalComponent> inputs = new ArrayList<>();
    public final List<InternalComponentOutput> outputs = new ArrayList<>();
    protected List<IStructureIngredientRule> ingredientRules = null;
    
    public LittleStructureType(String id, String category, Class<? extends LittleStructure> structureClass, int attribute) {
        this.id = id;
        this.category = category;
        this.clazz = structureClass;
        this.attribute = attribute;
        
        this.directional = new ArrayList<>();
        for (Field field : structureClass.getFields())
            if (field.isAnnotationPresent(StructureDirectional.class))
                directional.add(new StructureDirectionalField(field, field.getAnnotation(StructureDirectional.class)));
    }
    
    public InternalSignalInput[] createInputs(LittleStructure structure) {
        if (inputs.isEmpty())
            return null;
        InternalSignalInput[] result = new InternalSignalInput[inputs.size()];
        for (int i = 0; i < result.length; i++) {
            InternalComponent component = inputs.get(i);
            result[i] = new InternalSignalInput(structure, component);
        }
        return result;
    }
    
    public InternalSignalOutput[] createOutputs(LittleStructure structure) {
        if (outputs.isEmpty())
            return null;
        InternalSignalOutput[] result = new InternalSignalOutput[outputs.size()];
        for (int i = 0; i < result.length; i++) {
            InternalComponentOutput component = outputs.get(i);
            result[i] = new InternalSignalOutput(structure, component);
        }
        return result;
    }
    
    public LittleStructureType addInput(String name, int bandwidth) {
        inputs.add(new InternalComponent(name, bandwidth, inputs.size()));
        return this;
    }
    
    public LittleStructureType addOutput(String name, int bandwidth, SignalMode defaultMode, boolean sync) {
        outputs.add(new InternalComponentOutput(name, bandwidth, inputs.size(), defaultMode, sync));
        return this;
    }
    
    public LittleStructureType addOutput(String name, int bandwidth, SignalMode defaultMode) {
        return addOutput(name, bandwidth, defaultMode, false);
    }
    
    public LittleStructureType addIngredient(StructureIngredientScaler scale, Supplier<LittleIngredient> ingredient) {
        return addIngredient(new StructureIngredientRule(scale, ingredient));
    }
    
    public LittleStructureType addIngredient(IStructureIngredientRule rule) {
        if (ingredientRules == null)
            ingredientRules = new ArrayList<>();
        ingredientRules.add(rule);
        return this;
    }
    
    public LittleStructure createStructure(StructureTileList mainBlock) {
        try {
            return clazz.getConstructor(LittleStructureType.class, IStructureTileList.class).newInstance(this, mainBlock);
        } catch (Exception e) {
            throw new RuntimeException("Invalid structure type " + id, e);
        }
    }
    
    @SideOnly(Side.CLIENT)
    public List<LittleRenderBox> getPositingCubes(World world, BlockPos pos, ItemStack stack) {
        return null;
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof LittleStructureType && ((LittleStructureType) object).clazz == this.clazz;
    }
    
    @Override
    public String toString() {
        return clazz.toString();
    }
    
    public boolean canOnlyBePlacedByItemStack() {
        return false;
    }
    
    public void addIngredients(LittlePreviews previews, LittleIngredients ingredients) {
        if (ingredientRules != null)
            for (IStructureIngredientRule rule : ingredientRules)
                rule.add(previews, ingredients);
            
    }
    
    public void finializePreview(LittlePreviews previews) {
        LittleGridContext context = getMinContext(previews);
        if (context.size > previews.getContext().size)
            previews.convertTo(context);
    }
    
    public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
        if (directional.isEmpty())
            return new ArrayList<>();
        
        List<PlacePreview> placePreviews = new ArrayList<>();
        
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            PlacePreview tile = getPlacePreview(value, field, previews);
            if (tile == null)
                continue;
            
            if (field.getContext(value).size < previews.getContext().size)
                tile.convertTo(field.getContext(value), previews.getContext());
            
            placePreviews.add(tile);
        }
        return placePreviews;
    }
    
    protected PlacePreview getPlacePreview(Object value, StructureDirectionalField type, LittlePreviews previews) {
        return type.getPlacePreview(value, previews);
    }
    
    public LittleGridContext getMinContext(LittlePreviews previews) {
        LittleGridContext context = LittleGridContext.getMin();
        
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            field.convertToSmallest(value);
            LittleGridContext fieldContext = field.getContext(value);
            if (fieldContext == null)
                continue;
            
            context = LittleGridContext.max(context, fieldContext);
            field.save(previews.structureNBT, value);
        }
        return context;
    }
    
    public Object loadDirectional(LittlePreviews previews, String key) {
        for (StructureDirectionalField field : directional)
            if (field.key.equals(key))
                return field.create(previews.structureNBT);
        return null;
    }
    
    public LittleStructureType setFieldDefault(String key, Object defaultValue) {
        for (StructureDirectionalField field : directional)
            if (field.key.equals(key)) {
                field.setDefault(defaultValue);
                break;
            }
        return this;
    }
    
    public void move(LittleStructure structure, LittleGridContext context, LittleVec offset) {
        for (StructureDirectionalField field : directional) {
            Object value = field.get(structure);
            value = field.move(value, context, offset);
            field.set(structure, value);
        }
    }
    
    public void move(LittlePreviews previews, LittleGridContext context, LittleVec offset) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            value = field.move(value, context, offset);
            field.save(previews.structureNBT, value);
        }
    }
    
    public void flip(LittlePreviews previews, LittleGridContext context, Axis axis, LittleVec doubledCenter) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            value = field.flip(value, context, axis, doubledCenter);
            field.save(previews.structureNBT, value);
        }
    }
    
    public void rotate(LittlePreviews previews, LittleGridContext context, Rotation rotation, LittleVec doubledCenter) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            value = field.rotate(value, context, rotation, doubledCenter);
            field.save(previews.structureNBT, value);
        }
    }
    
    public void advancedScale(LittlePreviews previews, int from, int to) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(previews.structureNBT);
            field.advancedScale(value, from, to);
            field.save(previews.structureNBT, value);
        }
    }
    
    public static class InternalComponent {
        
        public final String identifier;
        public final int bandwidth;
        public final int index;
        
        public InternalComponent(String identifier, int bandwidth, int index) {
            this.identifier = identifier;
            this.bandwidth = bandwidth;
            this.index = index;
        }
        
        public boolean is(String name) {
            return identifier.equals(name);
        }
        
    }
    
    public static class InternalComponentOutput extends InternalComponent {
        
        public final SignalMode defaultMode;
        public final boolean syncToClient;
        
        public InternalComponentOutput(String identifier, int bandwidth, int index, SignalMode defaultMode, boolean syncToClient) {
            super(identifier, bandwidth, index);
            this.defaultMode = defaultMode;
            this.syncToClient = syncToClient;
        }
        
    }
    
}