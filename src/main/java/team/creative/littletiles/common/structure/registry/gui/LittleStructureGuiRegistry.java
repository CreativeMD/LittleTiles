package team.creative.littletiles.common.structure.registry.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.util.type.tree.NamedTree;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;

@OnlyIn(Dist.CLIENT)
public class LittleStructureGuiRegistry {
    
    private static final NamedTree<BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl>> TREE = new NamedTree<>();
    private static final HashMap<LittleStructureType, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl>> BY_TYPE = new HashMap<>();
    private static final List<BiFunction<LittleStructureType, LittleGroup, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl>>> SPECIAL = new ArrayList<>();
    
    public static void registerTreeOnly(String id, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl> gui) {
        TREE.add(id, gui);
    }
    
    public static void register(BiFunction<LittleStructureType, LittleGroup, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl>> special) {
        SPECIAL.add(special);
    }
    
    public static void register(String id, LittleStructureType type, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl> factory) {
        TREE.add(id, factory);
        BY_TYPE.put(type, factory);
    }
    
    public static void register(LittleStructureType type, BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl> factory) {
        BY_TYPE.put(type, factory);
    }
    
    public static BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl> get(LittleStructureType type, LittleGroup group) {
        BiFunction<GuiParent, AnimationGuiHandler, LittleStructureGuiControl> factory = BY_TYPE.get(type);
        if (factory != null)
            return factory;
        
        for (int i = 0; i < SPECIAL.size(); i++) {
            factory = SPECIAL.get(i).apply(type, group);
            if (factory != null)
                return factory;
        }
        
        factory = LittleStructureGuiControlNotFound::new;
        BY_TYPE.put(type, factory);
        return factory;
    }
    
    public static LittleStructureType get(String id) {
        return LittleStructureRegistry.REGISTRY.getOrThrow(id);
    }
    
    static {
        register("simple.fixed", get("fixed"), LittleFixedStructureParser::new);
        register("simple.ladder", get("ladder"), LittleLadderParser::new);
        register("simple.bed", get("bed"), LittleBedParser::new);
        register("simple.chair", get("chair"), LittleChairParser::new);
        register("simple.storage", get("storage"), LittleStorageParser::new);
        register("simple.noclip", get("noclip"), LittleNoClipStructureParser::new);
        register("simple.light", get("light"), LittleLightStructureParser::new);
        register("simple.message", get("message"), LittleMessageStructureParser::new);
        
        LittleStructureType machine = get("machine");
        register((type, group) -> {
            if (type != machine)
                return null;
            switch (group.getStructureTag().getString("parser")) {
                case "axis":
                    return LittleAxisDoorParser::new;
                case "sliding":
                    return LittleSlidingDoorParser::new;
                case "advanced":
                    return LittleAdvancedDoorParser::new;
                case "activator":
                    return LittleDoorActivatorParser::new;
                default:
                    return null;
            }
        });
        
        registerTreeOnly("door.axis", LittleAxisDoorParser::new);
        registerTreeOnly("door.sliding", LittleSlidingDoorParser::new);
        registerTreeOnly("door.advanced", LittleAdvancedDoorParser::new);
        registerTreeOnly("door.activator", LittleDoorActivatorParser::new);
    }
    
}
