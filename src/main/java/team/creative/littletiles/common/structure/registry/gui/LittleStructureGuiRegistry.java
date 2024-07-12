package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.animation.AnimationTimeline;
import team.creative.littletiles.common.structure.animation.AnimationTimeline.AnimationEventEntry;
import team.creative.littletiles.common.structure.animation.event.ChildDoorEvent;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.animation.LittleDoor;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiRegistry {
    
    private static final NamedTree<LittleStructureGui> TREE = new NamedTree<>();
    private static final HashMap<LittleStructureType, LittleStructureGui> BY_TYPE = new HashMap<>();
    private static final HashMap<LittleStructureType, Consumer<GuiTreeItemStructure>> BY_TYPE_FINALIZER = new HashMap<>();
    private static final List<BiFunction<LittleStructureType, LittleGroup, LittleStructureGui>> SPECIAL = new ArrayList<>();
    
    public static void registerTreeOnly(String id, LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> gui) {
        registerTreeOnly(new LittleStructureGui(id, type, gui));
    }
    
    public static void registerTreeOnly(LittleStructureGui gui) {
        TREE.add(gui.id(), gui);
    }
    
    public static void register(BiFunction<LittleStructureType, LittleGroup, LittleStructureGui> special) {
        SPECIAL.add(special);
    }
    
    public static void register(String id, LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        register(new LittleStructureGui(id, type, factory));
    }
    
    public static void register(LittleStructureGui gui) {
        TREE.add(gui.id(), gui);
        BY_TYPE.put(gui.type(), gui);
    }
    
    public static void register(LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        register(new LittleStructureGui(type.id, type, factory));
    }
    
    public static void registerByType(LittleStructureGui gui) {
        BY_TYPE.put(gui.type(), gui);
    }
    
    public static void registerFinalizer(Consumer<GuiTreeItemStructure> finalizer, LittleStructureType... types) {
        for (int i = 0; i < types.length; i++)
            BY_TYPE_FINALIZER.put(types[i], finalizer);
    }
    
    public static Consumer<GuiTreeItemStructure> getFinalizer(LittleStructureType type) {
        return BY_TYPE_FINALIZER.get(type);
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
        
        factory = new LittleStructureGui(type.id, type, LittleStructureGuiNotFound::new, false);
        BY_TYPE.put(type, factory);
        return factory;
    }
    
    public static LittleStructureType get(String id) {
        return LittleStructureRegistry.REGISTRY.getOrThrow(id);
    }
    
    private static void controlledByTransition(GuiTreeItemStructure item, BitSet set, AnimationTimeline timeline) {
        if (timeline == null)
            return;
        for (AnimationEventEntry entry : timeline.allEvents())
            if (entry.getEvent() instanceof ChildDoorEvent event)
                set.set(event.childId);
    }
    
    static {
        register(new LittleStructureGui("none", null, LittleStructureGuiNone::new, false));
        register("simple.fixed", get("fixed"), LittleStructureGuiDefault::new);
        register("simple.ladder", get("ladder"), LittleStructureGuiDefault::new);
        register("simple.bed", get("bed"), LittleBedGui::new);
        register("simple.chair", get("chair"), LittleStructureGuiDefault::new);
        register("simple.storage", get("storage"), LittleStorageGui::new);
        register("simple.noclip", get("noclip"), LittleNoClipGui::new);
        register("simple.light", get("light"), LittleLightGui::new);
        register("simple.message", get("message"), LittleMessageGui::new);
        
        LittleStructureType axis = get("axis");
        LittleStructureType sliding = get("sliding");
        LittleStructureType advanced = get("door");
        LittleStructureType activator = get("activator");
        register("door.axis", axis, LittleDoorAxisGui::new);
        register("door.sliding", sliding, LittleDoorSlidingGui::new);
        register("door.advanced", advanced, LittleDoorAdvancedGui::new);
        register("door.activator", activator, LittleDoorActivatorGui::new);
        
        registerFinalizer(x -> {
            LittleDoor door = (LittleDoor) x.structure;
            BitSet set = new BitSet();
            controlledByTransition(x, set, door.getTransition("opening"));
            controlledByTransition(x, set, door.getTransition("closing"));
            for (int i = 0; i < x.itemsCount(); i++)
                if (((GuiTreeItemStructure) x.getItem(i)).structure instanceof LittleDoor child)
                    child.activateParent = set.get(i);
        }, axis, sliding, advanced, activator);
        
        register((x, y) -> {
            if (x instanceof LittlePremadeType) {
                LittleStructureGui gui = new LittleStructureGui(x.id, x, LittleStructurePremadeGui::new);
                registerByType(gui);
                return gui;
            }
            return null;
        });
    }
    
}
