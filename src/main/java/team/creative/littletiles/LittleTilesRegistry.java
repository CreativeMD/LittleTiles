package team.creative.littletiles;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import team.creative.littletiles.common.block.entity.BESignalConverter;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETilesRendered;
import team.creative.littletiles.common.block.mc.BlockArrow;
import team.creative.littletiles.common.block.mc.BlockFlowingLava;
import team.creative.littletiles.common.block.mc.BlockFlowingWater;
import team.creative.littletiles.common.block.mc.BlockLava;
import team.creative.littletiles.common.block.mc.BlockSignalConverter;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.block.mc.BlockWater;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.ingredient.BlockIngredientEntry;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemLittleBlueprint;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGlove;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleSaw;
import team.creative.littletiles.common.item.ItemLittleScrewdriver;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemMultiTiles.ExampleStructures;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.recipe.PremadeShapedRecipeSerializer;
import team.creative.littletiles.common.recipe.StructureIngredient;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;

public class LittleTilesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, LittleTiles.MODID);
    
    public static final Holder<Item> HAMMER = ITEMS.register("hammer", () -> new ItemLittleHammer());
    public static final Holder<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new ItemLittleBlueprint());
    public static final Holder<Item> ITEM_TILES = ITEMS.register("multi_tiles", () -> new ItemMultiTiles());
    public static final Holder<Item> SAW = ITEMS.register("saw", () -> new ItemLittleSaw());
    public static final Holder<Item> BAG = ITEMS.register("bag", () -> new ItemLittleBag());
    public static final Holder<Item> WRENCH = ITEMS.register("wrench", () -> new ItemLittleWrench());
    public static final Holder<Item> SCREWDRIVER = ITEMS.register("screwdriver", () -> new ItemLittleScrewdriver());
    public static final Holder<Item> CHISEL = ITEMS.register("chisel", () -> new ItemLittleChisel());
    public static final Holder<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new ItemLittlePaintBrush());
    public static final Holder<Item> GLOVE = ITEMS.register("glove", () -> new ItemLittleGlove());
    public static final Holder<Item> PREMADE = ITEMS.register("premade", () -> new ItemPremadeStructure());
    
    public static final Holder<Item> BLOCK_INGREDIENT = ITEMS.register("blockingredient", () -> new ItemBlockIngredient());
    
    public static final Holder<Item> BLACK_COLOR = ITEMS.register("bottle_black", () -> new ItemColorIngredient(ColorIngredientType.black));
    public static final Holder<Item> CYAN_COLOR = ITEMS.register("bottle_cyan", () -> new ItemColorIngredient(ColorIngredientType.cyan));
    public static final Holder<Item> MAGENTA_COLOR = ITEMS.register("bottle_magenta", () -> new ItemColorIngredient(ColorIngredientType.magenta));
    public static final Holder<Item> YELLOW_COLOR = ITEMS.register("bottle_yellow", () -> new ItemColorIngredient(ColorIngredientType.yellow));
    
    // DATA COMPONENTS
    
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, LittleTiles.MODID);
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> DATA = DATA_COMPONENTS.register("tool_config", x -> DataComponentType
            .<CustomData>builder().persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR_AMOUNT = DATA_COMPONENTS.register("color_amount", x -> DataComponentType
            .<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COLOR = DATA_COMPONENTS.register("color", x -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockIngredientEntry>> BLOCK_INGREDIENT_ENTRY = DATA_COMPONENTS.register("block_ingredient",
        x -> DataComponentType.<BlockIngredientEntry>builder().persistent(BlockIngredientEntry.CODEC).networkSynchronized(BlockIngredientEntry.STREAM_CODEC).build());
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, LittleTiles.MODID);
    
    public static final Holder<Block> BLOCK_TILES = BLOCKS.register("tiles", () -> new BlockTile(false, false));
    public static final Holder<Block> BLOCK_TILES_TICKING = BLOCKS.register("tiles_ticking", () -> new BlockTile(true, false));
    public static final Holder<Block> BLOCK_TILES_RENDERED = BLOCKS.register("tiles_rendered", () -> new BlockTile(false, true));
    public static final Holder<Block> BLOCK_TILES_TICKING_RENDERED = BLOCKS.register("tiles_ticking_rendered", () -> new BlockTile(true, true));
    
    public static final Holder<Block> CLEAN = register("colored_clean", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> FLOOR = register("colored_floor", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY_BIG = register("colored_grainy_big", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY = register("colored_grainy", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAINY_LOW = register("colored_grainy_low", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BRICK = register("colored_brick", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BRICK_BIG = register("colored_brick_big", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BORDERED = register("colored_bordered", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> CHISELED = register("colored_chiseled", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> BROKEN_BRICK_BIG = register("colored_broken_brick_big", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(
        MapColor.SNOW)));
    public static final Holder<Block> CLAY = register("colored_clay", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> STRIPS = register("colored_strips", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> GRAVEL = register("colored_gravel", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> SAND = register("colored_sand", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> STONE = register("colored_stone", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    public static final Holder<Block> CORK = register("colored_cork", () -> new Block(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)));
    
    public static final Holder<Block> WATER = register("colored_water", () -> new BlockWater(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.COLOR_BLUE)
            .noCollission()));
    
    public static final Holder<Block> LAVA = register("colored_lava", () -> new BlockLava(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.COLOR_RED)
            .noCollission()));
    public static final Holder<Block> WHITE_LAVA = register("colored_white_lava", () -> new BlockLava(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).mapColor(MapColor.SNOW)
            .noCollission().lightLevel(x -> 15)));
    
    public static final Holder<Block> STORAGE_BLOCK = register("storage", () -> new Block(BlockBehaviour.Properties.of().sound(SoundType.WOOD).destroyTime(1.5F).strength(1.5F)));
    
    public static final Holder<Block> FLOWING_WATER = BLOCKS.register("colored_water_flowing", () -> new BlockFlowingWater(WATER));
    
    public static final Holder<Block> FLOWING_LAVA = BLOCKS.register("colored_lava_flowing", () -> new BlockFlowingLava(LAVA));
    public static final Holder<Block> WHITE_FLOWING_LAVA = BLOCKS.register("colored_white_lava_flowing", () -> new BlockFlowingLava(WHITE_LAVA));
    
    public static final Holder<Block> SINGLE_CABLE = BLOCKS.register("cable_single", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().strength(1.5F, 6.0F).sound(
        SoundType.WOOD)));
    
    public static final Holder<Block> INPUT_ARROW = BLOCKS.register("arrow_input", () -> new BlockArrow());
    public static final Holder<Block> OUTPUT_ARROW = BLOCKS.register("arrow_output", () -> new BlockArrow());
    
    public static final Holder<Block> SIGNAL_CONVERTER = register("signal_converter", () -> new BlockSignalConverter());
    
    public static final Holder<Block> MISSING = register("missing", () -> new Block(BlockBehaviour.Properties.of()));
    
    private static <T extends Block> DeferredHolder<Block, ? extends T> register(String name, Supplier<? extends T> sup) {
        var ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.value(), new Item.Properties()));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LittleTiles.MODID);
    
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BETiles>> BE_TILES_TYPE = registerBlockEntity("tiles", () -> BlockEntityType.Builder.of(BETiles::new,
        BLOCK_TILES.value(), BLOCK_TILES_TICKING.value()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BETilesRendered>> BE_TILES_TYPE_RENDERED = registerBlockEntity("tiles_rendered",
        () -> BlockEntityType.Builder.of(BETilesRendered::new, BLOCK_TILES_RENDERED.value(), BLOCK_TILES_TICKING_RENDERED.value()));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BESignalConverter>> BE_SIGNALCONVERTER_TYPE = registerBlockEntity("converter",
        () -> BlockEntityType.Builder.of(BESignalConverter::new, SIGNAL_CONVERTER.value()));
    
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntity(String name, Supplier<BlockEntityType.Builder<T>> sup) {
        return BLOCK_ENTITIES.register(name, () -> sup.get().build(Util.fetchChoiceType(References.BLOCK_ENTITY, name)));
    }
    
    // ENTITIES
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, LittleTiles.MODID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<PrimedSizedTnt>> SIZED_TNT_TYPE = ENTITIES.register("primed_size_tnt", () -> EntityType.Builder.<PrimedSizedTnt>of(
        PrimedSizedTnt::new, MobCategory.MISC).build("primed_size_tnt"));
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySit>> SIT_TYPE = ENTITIES.register("sit", () -> EntityType.Builder.<EntitySit>of(EntitySit::new,
        MobCategory.MISC).build("sit"));
    
    public static final DeferredHolder<EntityType<?>, EntityType<LittleAnimationEntity>> ENTITY_ANIMATION = ENTITIES.register("litte_animation", () -> EntityType.Builder
            .<LittleAnimationEntity>of(LittleAnimationEntity::new, MobCategory.MISC).build("litte_animation"));
    public static final DeferredHolder<EntityType<?>, EntityType<LittleLevelEntity>> ENTITY_LEVEL = ENTITIES.register("little_level", () -> EntityType.Builder
            .<LittleLevelEntity>of(LittleLevelEntity::new, MobCategory.MISC).build("little_level"));
    
    // DIMENSION
    
    public static final ResourceKey FAKE_DIMENSION = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.tryBuild(LittleTiles.MODID, "fake"));
    
    // Recipe Serializer
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, LittleTiles.MODID);
    
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<? extends CraftingRecipe>> PREMADE_RECIPES = RECIPE_SERIALIZERS.register("crafting_shaped_premade",
        PremadeShapedRecipeSerializer::new);
    
    // CREATIVE_TAB
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LittleTiles.MODID);
    
    public static final Holder<CreativeModeTab> TAB = CREATIVE_TABS.register("items", () -> CreativeModeTab.builder().icon(() -> new ItemStack(LittleTilesRegistry.HAMMER.value()))
            .title(Component.translatable("creative_tab.littletiles")).displayItems((features, output) -> {
                for (ExampleStructures example : ExampleStructures.values())
                    if (example.stack != null)
                        output.accept(example.stack);
                    
                for (LittlePremadeType entry : LittlePremadeRegistry.types())
                    if (entry.showInCreativeTab && !entry.hasCustomTab())
                        output.accept(entry.createItemStack());
                    
                output.accept(LittleTilesRegistry.HAMMER.value());
                output.accept(LittleTilesRegistry.CHISEL.value());
                output.accept(LittleTilesRegistry.BLUEPRINT.value());
                
                output.accept(LittleTilesRegistry.BAG.value());
                output.accept(LittleTilesRegistry.GLOVE.value());
                
                output.accept(LittleTilesRegistry.PAINT_BRUSH.value());
                output.accept(LittleTilesRegistry.SAW.value());
                output.accept(LittleTilesRegistry.SCREWDRIVER.value());
                output.accept(LittleTilesRegistry.WRENCH.value());
                
                output.accept(LittleTilesRegistry.SIGNAL_CONVERTER.value());
                output.accept(LittleTilesRegistry.STORAGE_BLOCK.value());
                
                output.accept(LittleTilesRegistry.CLEAN.value());
                output.accept(LittleTilesRegistry.FLOOR.value());
                output.accept(LittleTilesRegistry.GRAINY_BIG.value());
                output.accept(LittleTilesRegistry.GRAINY.value());
                output.accept(LittleTilesRegistry.GRAINY_LOW.value());
                output.accept(LittleTilesRegistry.BRICK.value());
                output.accept(LittleTilesRegistry.BRICK_BIG.value());
                output.accept(LittleTilesRegistry.BORDERED.value());
                output.accept(LittleTilesRegistry.CHISELED.value());
                output.accept(LittleTilesRegistry.BROKEN_BRICK_BIG.value());
                output.accept(LittleTilesRegistry.CLAY.value());
                output.accept(LittleTilesRegistry.STRIPS.value());
                output.accept(LittleTilesRegistry.GRAVEL.value());
                output.accept(LittleTilesRegistry.SAND.value());
                output.accept(LittleTilesRegistry.STONE.value());
                output.accept(LittleTilesRegistry.CORK.value());
                
                output.accept(LittleTilesRegistry.WATER.value());
                
                output.accept(LittleTilesRegistry.LAVA.value());
                output.accept(LittleTilesRegistry.WHITE_LAVA.value());
                
            }).build());
    
    // INGREDIENT_TYPES
    
    public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, LittleTiles.MODID);
    
    public static final DeferredHolder<IngredientType<?>, IngredientType<StructureIngredient>> STRUCTURE_INGREDIENT_TYPE = INGREDIENT_TYPES.register("structure",
        () -> new IngredientType<>(StructureIngredient.CODEC));
    
}
