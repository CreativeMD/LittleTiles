package team.creative.littletiles;

import java.util.function.Supplier;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
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
import team.creative.littletiles.common.entity.LittleLevelEntity;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
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
import team.creative.littletiles.common.item.ItemPremadeStructure;

public class LittleTilesRegistry {
    
    // ITEMS
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, LittleTiles.MODID);
    
    public static final RegistryObject<Item> HAMMER = ITEMS.register("hammer", () -> new ItemLittleHammer());
    public static final RegistryObject<Item> BLUEPRINT = ITEMS.register("blueprint", () -> new ItemLittleBlueprint());
    public static final RegistryObject<Item> ITEM_TILES = ITEMS.register("tiles", () -> new ItemMultiTiles());
    public static final RegistryObject<Item> SAW = ITEMS.register("saw", () -> new ItemLittleSaw());
    public static final RegistryObject<Item> BAG = ITEMS.register("bag", () -> new ItemLittleBag());
    public static final RegistryObject<Item> WRENCH = ITEMS.register("wrench", () -> new ItemLittleWrench());
    public static final RegistryObject<Item> SCREWDRIVER = ITEMS.register("screwdriver", () -> new ItemLittleScrewdriver());
    public static final RegistryObject<Item> CHISEL = ITEMS.register("chisel", () -> new ItemLittleChisel());
    public static final RegistryObject<Item> PAINT_BRUSH = ITEMS.register("paint_brush", () -> new ItemLittlePaintBrush());
    public static final RegistryObject<Item> GLOVE = ITEMS.register("glove", () -> new ItemLittleGlove());
    public static final RegistryObject<Item> PREMADE = ITEMS.register("premade", () -> new ItemPremadeStructure());
    
    public static final RegistryObject<Item> BLOCK_INGREDIENT = ITEMS.register("blockingredient", () -> new ItemBlockIngredient());
    
    public static final RegistryObject<Item> BLACK_COLOR = ITEMS.register("bottle_black", () -> new ItemColorIngredient(ColorIngredientType.black));
    public static final RegistryObject<Item> CYAN_COLOR = ITEMS.register("bottle_cyan", () -> new ItemColorIngredient(ColorIngredientType.cyan));
    public static final RegistryObject<Item> MAGENTA_COLOR = ITEMS.register("bottle_magenta", () -> new ItemColorIngredient(ColorIngredientType.magenta));
    public static final RegistryObject<Item> YELLOW_COLOR = ITEMS.register("bottle_yellow", () -> new ItemColorIngredient(ColorIngredientType.yellow));
    
    // BLOCKS
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, LittleTiles.MODID);
    
    public static final RegistryObject<Block> BLOCK_TILES = BLOCKS.register("tiles", () -> new BlockTile(Material.STONE, false, false));
    public static final RegistryObject<Block> BLOCK_TILES_TICKING = BLOCKS.register("tiles_ticking", () -> new BlockTile(Material.STONE, true, false));
    public static final RegistryObject<Block> BLOCK_TILES_RENDERED = BLOCKS.register("tiles_rendered", () -> new BlockTile(Material.STONE, false, true));
    public static final RegistryObject<Block> BLOCK_TILES_TICKING_RENDERED = BLOCKS.register("tiles_ticking_rendered", () -> new BlockTile(Material.STONE, true, true));
    
    public static final RegistryObject<Block> CLEAN = register("colored_clean", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> FLOOR = register("colored_floor", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> GRAINY_BIG = register("colored_grainy_big", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> GRAINY = register("colored_grainy", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> GRAINY_LOW = register("colored_grainy_low", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> BRICK = register("colored_brick", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> BRICK_BIG = register("colored_brick_big", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> BORDERED = register("colored_bordered", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> CHISELED = register("colored_chiseled", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> BROKEN_BRICK_BIG = register("colored_broken_brick_big", () -> new Block(BlockBehaviour.Properties
            .of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> CLAY = register("colored_clay", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> STRIPS = register("colored_strips", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> GRAVEL = register("colored_gravel", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> SAND = register("colored_sand", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> STONE = register("colored_stone", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    public static final RegistryObject<Block> CORK = register("colored_cork", () -> new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)));
    
    public static final RegistryObject<Block> WATER = register("colored_water", () -> new BlockWater(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED)
            .noCollission()));
    public static final RegistryObject<Block> WHITE_WATER = register("colored_white_water", () -> new BlockWater(BlockBehaviour.Properties
            .of(Material.STONE, MaterialColor.COLOR_RED).noCollission()));
    
    public static final RegistryObject<Block> LAVA = register("colored_lava", () -> new BlockLava(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED)
            .noCollission()));
    public static final RegistryObject<Block> WHITE_LAVA = register("colored_white_lava", () -> new BlockLava(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)
            .noCollission().lightLevel((x) -> {
                return 15;
            })));
    
    public static final RegistryObject<Block> STORAGE_BLOCK = register("storage", () -> new Block(BlockBehaviour.Properties.of(Material.WOOD).destroyTime(1.5F).strength(1.5F)
            .sound(SoundType.WOOD)));
    
    public static final RegistryObject<Block> FLOWING_WATER = BLOCKS.register("colored_water_flowing", () -> new BlockFlowingWater(WATER.get()));
    public static final RegistryObject<Block> WHITE_FLOWING_WATER = BLOCKS.register("colored_white_water_flowing", () -> new BlockFlowingWater(WHITE_WATER.get()));
    
    public static final RegistryObject<Block> FLOWING_LAVA = BLOCKS.register("colored_lava_flowing", () -> new BlockFlowingLava(LAVA.get()));
    public static final RegistryObject<Block> WHITE_FLOWING_LAVA = BLOCKS.register("colored_white_lava_flowing", () -> new BlockFlowingLava(WHITE_LAVA.get()));
    
    public static final RegistryObject<Block> SINGLE_CABLE = BLOCKS.register("cable_single", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.DECORATION)));
    
    public static final RegistryObject<Block> INPUT_ARROW = BLOCKS.register("arrow_input", () -> new BlockArrow());
    public static final RegistryObject<Block> OUTPUT_ARROW = BLOCKS.register("arrow_output", () -> new BlockArrow());
    
    public static final RegistryObject<Block> SIGNAL_CONVERTER = register("signal_converter", () -> new BlockSignalConverter());
    
    private static <T extends Block> RegistryObject<T> register(String name, Supplier<? extends T> sup) {
        RegistryObject<T> ret = BLOCKS.register(name, sup);
        ITEMS.register(name, () -> new BlockItem(ret.get(), new Item.Properties().tab(LittleTiles.LITTLE_TAB)));
        return ret;
    }
    
    // BLOCK_ENTITY
    
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, LittleTiles.MODID);
    
    public static final RegistryObject<BlockEntityType<BETiles>> BE_TILES_TYPE = BLOCK_ENTITIES
            .register("tiles", () -> BlockEntityType.Builder.of(BETiles::new, BLOCK_TILES.get(), BLOCK_TILES_TICKING.get()).build(null));
    public static final RegistryObject<BlockEntityType<BETilesRendered>> BE_TILES_TYPE_RENDERED = BLOCK_ENTITIES
            .register("tiles_rendered", () -> BlockEntityType.Builder.of(BETilesRendered::new, BLOCK_TILES_RENDERED.get(), BLOCK_TILES_TICKING_RENDERED.get()).build(null));
    public static final RegistryObject<BlockEntityType<BESignalConverter>> BE_SIGNALCONVERTER_TYPE = BLOCK_ENTITIES
            .register("converter", () -> BlockEntityType.Builder.of(BESignalConverter::new, SIGNAL_CONVERTER.get()).build(null));
    
    // ENTITIES
    
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, LittleTiles.MODID);
    
    public static final RegistryObject<EntityType<PrimedSizedTnt>> SIZED_TNT_TYPE = ENTITIES
            .register("primed_size_tnt", () -> EntityType.Builder.<PrimedSizedTnt>of(PrimedSizedTnt::new, MobCategory.MISC).build("primed_size_tnt"));
    public static final RegistryObject<EntityType<EntitySit>> SIT_TYPE = ENTITIES
            .register("sit", () -> EntityType.Builder.<EntitySit>of(EntitySit::new, MobCategory.MISC).build("sit"));
    
    public static final RegistryObject<EntityType<LittleLevelEntity>> ANIMATION = null;
    
}
