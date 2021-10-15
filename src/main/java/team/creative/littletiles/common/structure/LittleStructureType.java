package team.creative.littletiles.common.structure;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Axis;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.littletiles.client.render.tile.LittleRenderBox;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.block.little.tile.parent.StructureParentCollection;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.LittleIngredient;
import team.creative.littletiles.common.ingredient.LittleIngredients;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.math.vec.LittleVecGrid;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxRelative;
import team.creative.littletiles.common.structure.LittleStructureAttribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectional;
import team.creative.littletiles.common.structure.directional.StructureDirectionalField;
import team.creative.littletiles.common.structure.registry.IStructureIngredientRule;
import team.creative.littletiles.common.structure.registry.StructureIngredientRule;
import team.creative.littletiles.common.structure.registry.StructureIngredientRule.StructureIngredientScaler;
import team.creative.littletiles.common.structure.signal.input.InternalSignalInput;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.output.InternalSignalOutput;

public class LittleStructureType {
    
    public final String id;
    public final String category;
    public final Class<? extends LittleStructure> clazz;
    public final int attribute;
    public final List<StructureDirectionalField> directional;
    public final List<InternalComponent> inputs = new ArrayList<>();
    public final List<InternalComponentOutput> outputs = new ArrayList<>();
    protected List<IStructureIngredientRule> ingredientRules = null;
    
    public LittleStructureType(String id, String category, Class<? extends LittleStructure> structureClass, LittleAttributeBuilder attribute) {
        this.id = id;
        this.category = category;
        this.clazz = structureClass;
        this.attribute = attribute.build();
        
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
    
    public LittleStructureType addIngredient(StructureIngredientScaler scale, LittleIngredient ingredient) {
        return addIngredient(new StructureIngredientRule(scale, ingredient));
    }
    
    public LittleStructureType addIngredient(IStructureIngredientRule rule) {
        if (ingredientRules == null)
            ingredientRules = new ArrayList<>();
        ingredientRules.add(rule);
        return this;
    }
    
    public LittleStructure createStructure(StructureParentCollection mainBlock) {
        try {
            return clazz.getConstructor(LittleStructureType.class, IStructureParentCollection.class).newInstance(this, mainBlock);
        } catch (Exception e) {
            throw new RuntimeException("Invalid structure type " + id, e);
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public List<LittleRenderBox> getPositingCubes(Level level, BlockPos pos, ItemStack stack) {
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
    
    public void addIngredients(LittleGroup group, LittleIngredients ingredients) {
        if (ingredientRules != null)
            for (IStructureIngredientRule rule : ingredientRules)
                rule.add(group, ingredients);
            
    }
    
    public void finializePreview(LittleGroup group) {
        LittleGrid grid = getMinContext(group);
        if (grid.count > group.getGrid().count)
            group.convertTo(grid);
    }
    
    public List<LittlePlaceBox> getSpecialTiles(LittleGroup group) {
        if (directional.isEmpty())
            return new ArrayList<>();
        
        List<LittlePlaceBox> placePreviews = new ArrayList<>();
        
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            LittlePlaceBoxRelative tile = getPlaceBox(value, field, group);
            if (tile == null)
                continue;
            
            if (field.getGrid(value).count < group.getGrid().count)
                tile.convertTo(field.getGrid(value), group.getGrid());
            
            placePreviews.add(tile);
        }
        return placePreviews;
    }
    
    protected LittlePlaceBoxRelative getPlaceBox(Object value, StructureDirectionalField type, LittleGroup previews) {
        return type.getPlaceBox(value, previews);
    }
    
    public LittleGrid getMinContext(LittleGroup group) {
        LittleGrid context = LittleGrid.min();
        
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            field.convertToSmallest(value);
            LittleGrid fieldContext = field.getGrid(value);
            if (fieldContext == null)
                continue;
            
            context = LittleGrid.max(context, fieldContext);
            field.save(group.getStructureTag(), value);
        }
        return context;
    }
    
    public Object loadDirectional(LittleGroup group, String key) {
        for (StructureDirectionalField field : directional)
            if (field.key.equals(key))
                return field.create(group.getStructureTag());
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
    
    public void move(LittleStructure structure, LittleVecGrid offset) {
        for (StructureDirectionalField field : directional) {
            Object value = field.get(structure);
            value = field.move(value, offset);
            field.set(structure, value);
        }
    }
    
    public void move(LittleGroup group, LittleVecGrid offset) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            value = field.move(value, offset);
            field.save(group.getStructureTag(), value);
        }
    }
    
    public void mirror(LittleGroup group, LittleGrid context, Axis axis, LittleVec doubledCenter) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            value = field.mirror(value, context, axis, doubledCenter);
            field.save(group.getStructureTag(), value);
        }
    }
    
    public void rotate(LittleGroup group, LittleGrid context, Rotation rotation, LittleVec doubledCenter) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            value = field.rotate(value, context, rotation, doubledCenter);
            field.save(group.getStructureTag(), value);
        }
    }
    
    public void advancedScale(LittleGroup group, int from, int to) {
        for (StructureDirectionalField field : directional) {
            Object value = field.create(group.getStructureTag());
            field.advancedScale(value, from, to);
            field.save(group.getStructureTag(), value);
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