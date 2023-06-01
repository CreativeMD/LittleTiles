package team.creative.littletiles.common.structure.registry.premade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.item.ItemStack;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.NBTUtils;
import team.creative.littletiles.LittleTiles;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.type.premade.LittleBlankOMatic;
import team.creative.littletiles.common.structure.type.premade.LittleExporter;
import team.creative.littletiles.common.structure.type.premade.LittleImporter;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.LittleStructureTypeParticleEmitter;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;
import team.creative.littletiles.common.structure.type.premade.LittleWorkbench;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalCable;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalCable.LittleStructureTypeCable;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalDisplay;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalInput;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalInput.LittleStructureTypeInput;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalOutput;
import team.creative.littletiles.common.structure.type.premade.signal.LittleSignalOutput.LittleStructureTypeOutput;

public class LittlePremadeRegistry {
    
    private static final List<LittlePremadeType> STRUCTURES = new ArrayList<>();
    private static final HashMap<String, LittlePremadePreview> PREVIEWS = new HashMap<>();
    
    public static void reload() {
        PREVIEWS.clear();
        ItemPremadeStructure.clearCache();
        
        int loaded = 0;
        for (LittlePremadeType type : STRUCTURES) {
            try {
                ItemStack stack = type.createItemStackEmpty();
                CompoundTag structureNBT = new CompoundTag();
                structureNBT.putString("id", type.id);
                CompoundTag nbt = TagParser.parseTag(IOUtils
                        .toString(LittleStructurePremade.class.getClassLoader().getResourceAsStream("data/" + type.modid + "/premade/" + type.id + ".struct"), Charsets.UTF_8));
                
                CompoundTag originalNBT = nbt.contains(LittleGroup.STRUCTURE_KEY) ? nbt.getCompound(LittleGroup.STRUCTURE_KEY) : null;
                nbt.put(LittleGroup.STRUCTURE_KEY, structureNBT);
                if (originalNBT != null)
                    NBTUtils.mergeNotOverwrite(structureNBT, originalNBT);
                stack.setTag(nbt);
                
                LittleGroup previews = LittleGroup.load(stack.getOrCreateTag());
                
                CompoundTag stackNBT = new CompoundTag();
                stackNBT.put(LittleGroup.STRUCTURE_KEY, structureNBT);
                stack.setTag(stackNBT);
                
                PREVIEWS.put(type.id, new LittlePremadePreview(previews, stack));
                loaded++;
            } catch (Exception e) {
                e.printStackTrace();
                LittleTiles.LOGGER.info("Could not load {}. Structure will not be available.", type.id);
            }
        }
        LittleTiles.LOGGER.info("Loaded {} premade structure models", loaded);
    }
    
    public static <T extends LittleStructurePremade> LittlePremadeType register(String id, String modid, Class<T> structureClass, BiFunction<? extends LittlePremadeType, IStructureParentCollection, T> factory) {
        return register(id, modid, structureClass, factory, new LittleAttributeBuilder());
    }
    
    public static <T extends LittleStructurePremade> LittlePremadeType register(String id, String modid, Class<T> structureClass, BiFunction<? extends LittlePremadeType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
        LittlePremadeType type = (LittlePremadeType) LittleStructureRegistry.register(new LittlePremadeType(id, structureClass, factory, attribute, modid));
        STRUCTURES.add(type);
        return type;
    }
    
    public static LittlePremadeType register(LittlePremadeType type) {
        STRUCTURES.add((LittlePremadeType) LittleStructureRegistry.register(type));
        return type;
    }
    
    public static LittleGroup getLittleGroup(String id) {
        LittlePremadePreview type = getPreview(id);
        if (type != null)
            return type.previews;
        return null;
    }
    
    public static LittlePremadePreview getPreview(String id) {
        return PREVIEWS.get(id);
    }
    
    public static Collection<LittlePremadePreview> previews() {
        return PREVIEWS.values();
    }
    
    public static List<LittlePremadeType> types() {
        return STRUCTURES;
    }
    
    public static Set<String> keySet() {
        return PREVIEWS.keySet();
    }
    
    public static LittlePremadeType get(String id) {
        LittleStructureType type = LittleStructureRegistry.REGISTRY.get(id);
        if (type instanceof LittlePremadeType)
            return (LittlePremadeType) type;
        return null;
    }
    
    public static ItemStack tryCreateStack(String id) {
        LittlePremadePreview preview = PREVIEWS.get(id);
        if (preview != null)
            return preview.stack.copy();
        return ItemStack.EMPTY;
    }
    
    public static ItemStack createStack(String id) {
        return PREVIEWS.get(id).stack.copy();
    }
    
    public static void initStructures() {
        register("workbench", LittleTiles.MODID, LittleWorkbench.class, LittleWorkbench::new);
        register("importer", LittleTiles.MODID, LittleImporter.class, LittleImporter::new);
        register("exporter", LittleTiles.MODID, LittleExporter.class, LittleExporter::new);
        register(new LittleStructureTypeParticleEmitter("particle_emitter", LittleParticleEmitter.class, LittleParticleEmitter::new, new LittleAttributeBuilder()
                .ticking(), LittleTiles.MODID)).addOutput("disabled", 1, SignalMode.TOGGLE, true).setFieldDefault("facing", Facing.UP);
        register("blankomatic", LittleTiles.MODID, LittleBlankOMatic.class, LittleBlankOMatic::new);
        
        register(new LittleStructureTypeCable("single_cable1", LittleSignalCable.class, LittleSignalCable::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 1));
        register(new LittleStructureTypeCable("single_cable4", LittleSignalCable.class, LittleSignalCable::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 4));
        register(new LittleStructureTypeCable("single_cable16", LittleSignalCable.class, LittleSignalCable::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 16));
        
        register(new LittleStructureTypeOutput("single_output1", LittleSignalOutput.class, LittleSignalOutput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 1));
        register(new LittleStructureTypeOutput("single_output4", LittleSignalOutput.class, LittleSignalOutput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 4));
        register(new LittleStructureTypeOutput("single_output16", LittleSignalOutput.class, LittleSignalOutput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 16));
        
        register(new LittleStructureTypeInput("single_input1", LittleSignalInput.class, LittleSignalInput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 1));
        register(new LittleStructureTypeInput("single_input4", LittleSignalInput.class, LittleSignalInput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 4));
        register(new LittleStructureTypeInput("single_input16", LittleSignalInput.class, LittleSignalInput::new, new LittleAttributeBuilder()
                .extraRendering(), LittleTiles.MODID, 16));
        
        register("signal_display_16", LittleTiles.MODID, LittleSignalDisplay.class, LittleSignalDisplay::new, new LittleAttributeBuilder().tickRendering())
                .addOutput("pixels", 16, SignalMode.EQUAL, true);
        
        register("structure_builder", LittleTiles.MODID, LittleStructureBuilder.class, LittleStructureBuilder::new);
        
    }
    
}
