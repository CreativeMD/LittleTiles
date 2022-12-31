package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiRegistry {
    
    private static final NamedTree<LittleStructureGui> TREE = new NamedTree<>();
    private static final HashMap<LittleStructureType, LittleStructureGui> BY_TYPE = new HashMap<>();
    private static final List<BiFunction<LittleStructureType, LittleGroup, LittleStructureGui>> SPECIAL = new ArrayList<>();
    
    public static void registerTreeOnly(String id, LittleStructureType type, BiFunction<LittleStructureType, GuiTreeItemStructure, LittleStructureGuiControl> gui) {
        registerTreeOnly(new LittleStructureGui(id, type, gui));
    }
    
    public static void registerTreeOnly(LittleStructureGui gui) {
        TREE.add(gui.id(), gui);
    }
    
    public static void register(BiFunction<LittleStructureType, LittleGroup, LittleStructureGui> special) {
        SPECIAL.add(special);
    }
    
    public static void register(String id, LittleStructureType type, BiFunction<LittleStructureType, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        register(new LittleStructureGui(id, type, factory));
    }
    
    public static void register(LittleStructureGui gui) {
        TREE.add(gui.id(), gui);
        BY_TYPE.put(gui.type(), gui);
    }
    
    public static void register(LittleStructureType type, BiFunction<LittleStructureType, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        register(new LittleStructureGui(type.id, type, factory));
    }
    
    public static void registerByType(LittleStructureGui gui) {
        BY_TYPE.put(gui.type(), gui);
    }
    
    public static Iterable<LittleStructureGui> registered() {
        return TREE;
    }
    
    public static LittleStructureGui get(LittleStructureType type, LittleGroup group) {
        LittleStructureGui factory = BY_TYPE.get(type);
        if (factory != null)
            return factory;
        
        for (int i = 0; i < SPECIAL.size(); i++) {
            factory = SPECIAL.get(i).apply(type, group);
            if (factory != null)
                return factory;
        }
        
        factory = new LittleStructureGui(type.id, type, LittleStructureGuiNotFound::new);
        BY_TYPE.put(type, factory);
        return factory;
    }
    
    public static LittleStructureType get(String id) {
        return LittleStructureRegistry.REGISTRY.getOrThrow(id);
    }
    
    static {
        register("none", null, LittleStructureGuiNone::new);
        register("simple.fixed", get("fixed"), LittleStructureGuiDefault::new);
        register("simple.ladder", get("ladder"), LittleStructureGuiDefault::new);
        register("simple.bed", get("bed"), LittleBedGui::new);
        register("simple.chair", get("chair"), LittleStructureGuiDefault::new);
        register("simple.storage", get("storage"), LittleStorageGui::new);
        register("simple.noclip", get("noclip"), LittleNoClipGui::new);
        register("simple.light", get("light"), LittleLightGui::new);
        register("simple.message", get("message"), LittleMessageGui::new);
        
        LittleStructureType door = get("door");
        LittleStructureGui axisDoor = new LittleStructureGui("door.axis", door, LittleDoorAxisGui::new);
        LittleStructureGui slidingDoor = new LittleStructureGui("door.sliding", door, LittleDoorSlidingGui::new);
        LittleStructureGui advancedDoor = new LittleStructureGui("door.advanced", door, LittleDoorAdvancedGui::new);
        LittleStructureGui activatorDoor = new LittleStructureGui("door.activator", door, LittleDoorActivatorGui::new);
        register((type, group) -> {
            if (type != door)
                return null;
            return switch (group.getStructureTag().getString("parser")) {
                case "axis" -> axisDoor;
                case "sliding" -> slidingDoor;
                case "advanced" -> advancedDoor;
                case "activator" -> activatorDoor;
                default -> null;
            };
        });
        
        registerTreeOnly(axisDoor);
        registerTreeOnly(slidingDoor);
        registerTreeOnly(advancedDoor);
        registerTreeOnly(activatorDoor);
    }
    
}
