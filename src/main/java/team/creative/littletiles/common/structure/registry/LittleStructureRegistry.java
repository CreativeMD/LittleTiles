package team.creative.littletiles.common.structure.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.type.list.Pair;
import team.creative.creativecore.common.util.type.list.PairList;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.ingredient.ItemIngredient;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureAttribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureGuiParser.LittleStructureGuiParserNotFoundHandler;
import team.creative.littletiles.common.structure.registry.StructureIngredientRule.StructureIngredientScalerVolume;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.type.LittleChair;
import team.creative.littletiles.common.structure.type.LittleChair.LittleChairParser;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;
import team.creative.littletiles.common.structure.type.LittleFixedStructure.LittleFixedStructureParser;
import team.creative.littletiles.common.structure.type.LittleItemHolder;
import team.creative.littletiles.common.structure.type.LittleLadder;
import team.creative.littletiles.common.structure.type.LittleLadder.LittleLadderParser;
import team.creative.littletiles.common.structure.type.LittleLight;
import team.creative.littletiles.common.structure.type.LittleLight.LittleLightStructureParser;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure.LittleNoClipStructureParser;
import team.creative.littletiles.common.structure.type.LittleStorage;
import team.creative.littletiles.common.structure.type.LittleStorage.LittleStorageParser;
import team.creative.littletiles.common.structure.type.LittleStorage.LittleStorageType;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;
import team.creative.littletiles.common.structure.type.LittleStructureMessage.LittleMessageStructureParser;
import team.creative.littletiles.common.structure.type.bed.LittleBed;
import team.creative.littletiles.common.structure.type.bed.LittleBed.LittleBedParser;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade;

public class LittleStructureRegistry {
    
    private static HashMap<String, LittleStructureType> structures = new LinkedHashMap<String, LittleStructureType>();
    private static HashMap<Class<? extends LittleStructure>, LittleStructureType> structuresClass = new LinkedHashMap<Class<? extends LittleStructure>, LittleStructureType>();
    
    private static PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> craftables = new PairList<>();
    private static List<LittleStructureGuiParserNotFoundHandler> premadeParsers = new ArrayList<>();
    
    private static LittleStructureType defaultType;
    
    public static LittleStructureType registerStructureType(String id, String category, Class<? extends LittleStructure> classStructure, LittleAttributeBuilder attribute, Class<? extends LittleStructureGuiParser> parser) {
        return registerStructureType(new LittleStructureType(id, category, classStructure, attribute), parser);
    }
    
    public static void registerGuiParser(String id, String category, Class<? extends LittleStructureGuiParser> parser) {
        category = "structure.category." + category;
        PairList<String, Class<? extends LittleStructureGuiParser>> categoryList = craftables.getValue(category);
        if (categoryList == null) {
            categoryList = new PairList<>();
            craftables.add(category, categoryList);
        }
        categoryList.add("structure." + id + ".name", parser);
    }
    
    public static void registerGuiParserNotFoundHandler(LittleStructureGuiParserNotFoundHandler handler) {
        premadeParsers.add(handler);
    }
    
    public static PairList<String, PairList<String, Class<? extends LittleStructureGuiParser>>> getCraftables() {
        return craftables;
    }
    
    public static Class<? extends LittleStructureGuiParser> getParserClass(String id) {
        for (Pair<String, PairList<String, Class<? extends LittleStructureGuiParser>>> pair : craftables) {
            Class<? extends LittleStructureGuiParser> parser = pair.value.getValue(id);
            if (parser != null)
                return parser;
        }
        return null;
    }
    
    public static LittleStructureGuiParser getParser(GuiParent parent, AnimationGuiHandler handler, Class<? extends LittleStructureGuiParser> clazz) {
        try {
            if (clazz == null)
                return null;
            return clazz.getConstructor(GuiParent.class, AnimationGuiHandler.class).newInstance(parent, handler);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static LittleStructureGuiParser getParser(GuiParent parent, AnimationGuiHandler handler, String id) {
        try {
            Class<? extends LittleStructureGuiParser> clazz = getParserClass(id);
            if (clazz == null)
                return null;
            return clazz.getConstructor(GuiParent.class, AnimationGuiHandler.class).newInstance(parent, handler);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static LittleStructureGuiParser getParserNotFound(GuiParent parent, AnimationGuiHandler handler, LittleStructure structure) {
        for (LittleStructureGuiParserNotFoundHandler notFound : premadeParsers) {
            LittleStructureGuiParser parser = notFound.create(structure, parent, handler);
            if (parser != null)
                return parser;
        }
        return null;
    }
    
    public static LittleStructureType registerStructureType(LittleStructureType entry, Class<? extends LittleStructureGuiParser> parser) {
        if (structures.containsKey(entry.id))
            throw new RuntimeException("ID is already taken! id=" + entry.id);
        
        if (parser != null)
            registerGuiParser(entry.id, entry.category, parser);
        
        structures.put(entry.id, entry);
        structuresClass.put(entry.clazz, entry);
        return entry;
    }
    
    public static String getStructureId(Class<? extends LittleStructure> classStructure) {
        LittleStructureType entry = structuresClass.get(classStructure);
        if (entry != null)
            return entry.id;
        return null;
    }
    
    public static Class<? extends LittleStructure> getStructureClass(String id) {
        LittleStructureType entry = structures.get(id);
        if (entry != null)
            return entry.clazz;
        return null;
    }
    
    public static LittleStructureType getStructureType(String id) {
        return structures.getOrDefault(id, defaultType);
    }
    
    public static LittleStructureType getStructureType(Class<? extends LittleStructure> classStructure) {
        return structuresClass.get(classStructure);
    }
    
    public static void initStructures() {
        defaultType = registerStructureType("fixed", "simple", LittleFixedStructure.class, new LittleAttributeBuilder(), LittleFixedStructureParser.class);
        
        registerStructureType("ladder", "simple", LittleLadder.class, new LittleAttributeBuilder().ladder(), LittleLadderParser.class)
                .addIngredient(StructureIngredientRule.LONGEST_SIDE, new StackIngredient(new ItemStack(Blocks.LADDER)));
        
        registerStructureType("bed", "simple", LittleBed.class, new LittleAttributeBuilder(), LittleBedParser.class).addInput("occupied", 1)
                .addIngredient(StructureIngredientRule.SINGLE, new ItemIngredient(CreativeIngredient.parse(ItemTags.BEDS)));
        registerStructureType("chair", "simple", LittleChair.class, new LittleAttributeBuilder(), LittleChairParser.class).addInput("occupied", 1);
        
        registerStructureType(new LittleStorageType("storage", "simple", LittleStorage.class, new LittleAttributeBuilder()).addInput("accessed", 1)
                .addInput("filled", 16), LittleStorageParser.class);
        registerStructureType("noclip", "simple", LittleNoClipStructure.class, new LittleAttributeBuilder().noCollision().collisionListener(), LittleNoClipStructureParser.class)
                .addInput("players", 4).addInput("entities", 4);
        
        registerStructureType("light", "simple", LittleLight.class, new LittleAttributeBuilder().lightEmitter(), LittleLightStructureParser.class)
                .addOutput("enabled", 1, SignalMode.TOGGLE, true).addIngredient(new StructureIngredientScalerVolume(8), new StackIngredient(new ItemStack(Items.GLOWSTONE_DUST)));
        
        registerStructureType("message", "simple", LittleStructureMessage.class, new LittleAttributeBuilder(), LittleMessageStructureParser.class)
                .addOutput("message", 1, SignalMode.EQUAL);
        
        LittleStructureBuilder
                .register(new LittleStructureBuilderType(registerStructureType("item_holder", "simple", LittleItemHolder.class, new LittleAttributeBuilder().extraRendering(), null)
                        .addInput("filled", 1), "frame"));
        
        // LittleDoorBase.initDoors();
        
        LittleStructurePremade.initPremadeStructures();
    }
}
