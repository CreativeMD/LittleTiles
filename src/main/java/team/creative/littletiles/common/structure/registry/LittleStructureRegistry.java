package team.creative.littletiles.common.structure.registry;

import java.util.function.BiFunction;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import team.creative.creativecore.common.util.ingredient.CreativeIngredient;
import team.creative.creativecore.common.util.registry.NamedHandlerRegistry;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.ingredient.ItemIngredient;
import team.creative.littletiles.common.ingredient.StackIngredient;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.LittleStructureType;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.registry.ingredient.StructureIngredientRule;
import team.creative.littletiles.common.structure.registry.ingredient.StructureIngredientRule.StructureIngredientScalerVolume;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.type.LittleChair;
import team.creative.littletiles.common.structure.type.LittleFixedStructure;
import team.creative.littletiles.common.structure.type.LittleItemHolder;
import team.creative.littletiles.common.structure.type.LittleLadder;
import team.creative.littletiles.common.structure.type.LittleLight;
import team.creative.littletiles.common.structure.type.LittleNoClipStructure;
import team.creative.littletiles.common.structure.type.LittleStorage;
import team.creative.littletiles.common.structure.type.LittleStorage.LittleStorageType;
import team.creative.littletiles.common.structure.type.LittleStructureMessage;
import team.creative.littletiles.common.structure.type.animation.LittleActivatorDoor;
import team.creative.littletiles.common.structure.type.animation.LittleAdvancedDoor;
import team.creative.littletiles.common.structure.type.animation.LittleAxisDoor;
import team.creative.littletiles.common.structure.type.animation.LittleDoor.LittleDoorType;
import team.creative.littletiles.common.structure.type.animation.LittleSlidingDoor;
import team.creative.littletiles.common.structure.type.bed.LittleBed;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder.LittleStructureBuilderType;

public class LittleStructureRegistry {
    
    public static final NamedHandlerRegistry<LittleStructureType> REGISTRY = new NamedHandlerRegistry<>(null);
    
    public static <T extends LittleStructure> LittleStructureType register(String id, Class<T> classStructure, BiFunction<LittleStructureType, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute) {
        LittleStructureType type = new LittleStructureType(id, classStructure, factory, attribute);
        REGISTRY.register(id, type);
        return type;
    }
    
    public static <T extends LittleStructure> LittleStructureType register(LittleStructureType type) {
        REGISTRY.register(type.id, type);
        return type;
    }
    
    public static void initStructures() {
        REGISTRY.registerDefault("fixed", new LittleStructureType("fixed", LittleFixedStructure.class, LittleFixedStructure::new, LittleAttributeBuilder.NONE));
        
        register("ladder", LittleLadder.class, LittleLadder::new, new LittleAttributeBuilder().ladder())
                .addIngredient(StructureIngredientRule.LONGEST_SIDE, () -> new StackIngredient(new ItemStack(Blocks.LADDER)));
        
        register("bed", LittleBed.class, LittleBed::new, LittleAttributeBuilder.NONE).addInput("occupied", 1)
                .addIngredient(StructureIngredientRule.SINGLE, () -> new ItemIngredient(CreativeIngredient.parse(ItemTags.BEDS)));
        register("chair", LittleChair.class, LittleChair::new, LittleAttributeBuilder.NONE).addInput("occupied", 1);
        
        register(new LittleStorageType("storage", LittleStorage.class, LittleStorage::new, LittleAttributeBuilder.NONE).addInput("accessed", 1).addInput("filled", 16));
        register("noclip", LittleNoClipStructure.class, LittleNoClipStructure::new, new LittleAttributeBuilder().noCollision().collisionListener()).addInput("players", 4)
                .addInput("entities", 4);
        
        register("light", LittleLight.class, LittleLight::new, new LittleAttributeBuilder().lightEmitter()).addOutput("enabled", 1, SignalMode.TOGGLE, true)
                .addIngredient(new StructureIngredientScalerVolume(8), () -> new StackIngredient(new ItemStack(Items.GLOWSTONE_DUST)));
        
        register("message", LittleStructureMessage.class, LittleStructureMessage::new, LittleAttributeBuilder.NONE).addOutput("message", 1, SignalMode.EQUAL);
        
        LittleStructureBuilder
                .register(new LittleStructureBuilderType(register("item_holder", LittleItemHolder.class, LittleItemHolder::new, new LittleAttributeBuilder().extraRendering())
                        .addInput("filled", 1), "frame"));
        
        register(new LittleDoorType("axis", LittleAxisDoor.class, LittleAxisDoor::new, LittleAttributeBuilder.NONE));
        register(new LittleDoorType("sliding", LittleSlidingDoor.class, LittleSlidingDoor::new, LittleAttributeBuilder.NONE));
        register(new LittleDoorType("door", LittleAdvancedDoor.class, LittleAdvancedDoor::new, LittleAttributeBuilder.NONE));
        register(new LittleDoorType("activator", LittleActivatorDoor.class, LittleActivatorDoor::new, LittleAttributeBuilder.NONE));
        
        LittlePremadeRegistry.initStructures();
    }
}
