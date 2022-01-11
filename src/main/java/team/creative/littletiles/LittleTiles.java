package team.creative.littletiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.littletiles.common.event.LittleEventHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.util.argument.StringArrayArgumentType;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionActivated;
import team.creative.littletiles.common.action.LittleActionColorBoxes;
import team.creative.littletiles.common.action.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import team.creative.littletiles.common.action.LittleActionDestroy;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes;
import team.creative.littletiles.common.action.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import team.creative.littletiles.common.action.LittleActionException;
import team.creative.littletiles.common.action.LittleActionPlace;
import team.creative.littletiles.common.action.LittleActionRegistry;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.block.entity.BESignalConverter;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.mc.BlockArrow;
import team.creative.littletiles.common.block.mc.BlockFlowingLava;
import team.creative.littletiles.common.block.mc.BlockFlowingWater;
import team.creative.littletiles.common.block.mc.BlockLava;
import team.creative.littletiles.common.block.mc.BlockSignalConverter;
import team.creative.littletiles.common.block.mc.BlockTile;
import team.creative.littletiles.common.block.mc.BlockWater;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.entity.EntitySizeHandler;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiHandler;
import team.creative.littletiles.common.gui.handler.LittleTileGuiHandler;
import team.creative.littletiles.common.gui.premade.GuiExport;
import team.creative.littletiles.common.gui.premade.GuiImport;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;
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
import team.creative.littletiles.common.level.WorldAnimationHandler;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselAndBitsConveration;
import team.creative.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import team.creative.littletiles.common.packet.LittleActivateDoorPacket;
import team.creative.littletiles.common.packet.LittleConsumeRightClickEvent;
import team.creative.littletiles.common.packet.LittleEntityFixControllerPacket;
import team.creative.littletiles.common.packet.LittleEntityRequestPacket;
import team.creative.littletiles.common.packet.LittlePacketTypes;
import team.creative.littletiles.common.packet.LittlePlacedAnimationPacket;
import team.creative.littletiles.common.packet.LittleResetAnimationPacket;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.item.ConfigurePacket;
import team.creative.littletiles.common.packet.item.MirrorPacket;
import team.creative.littletiles.common.packet.item.RotatePacket;
import team.creative.littletiles.common.packet.item.ScrewdriverSelectionPacket;
import team.creative.littletiles.common.packet.item.SelectionModePacket;
import team.creative.littletiles.common.packet.structure.BedUpdate;
import team.creative.littletiles.common.packet.update.BlockUpdate;
import team.creative.littletiles.common.packet.update.BlocksUpdate;
import team.creative.littletiles.common.packet.update.NeighborUpdate;
import team.creative.littletiles.common.packet.update.OutputUpdate;
import team.creative.littletiles.common.packet.update.StructureUpdate;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoor.DoorActivator;
import team.creative.littletiles.server.LittleTilesServer;
import team.creative.littletiles.server.NeighborUpdateOrganizer;
import team.creative.littletiles.server.level.LevelHandlersServer;
import team.creative.littletiles.server.level.LittleAnimationHandlerServer;

@Mod(value = LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    public static final String VERSION = "1.6.0";
    
    public static BlockEntityType BE_SIGNALCONVERTER_TYPE;
    public static BlockEntityType BE_TILES_TYPE;
    public static BlockEntityType BE_TILES_TYPE_RENDERED;
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));
    public static final LevelHandlersServer LEVEL_HANDLERS = new LevelHandlersServer();
    
    public static Block BLOCK_TILES;
    public static Block BLOCK_TILES_TICKING;
    public static Block BLOCK_TILES_RENDERED;
    public static Block BLOCK_TILES_TICKING_RENDERED;
    
    public static Block CLEAN = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_clean");
    public static Block FLOOR = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_floor");
    public static Block GRAINY_BIG = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_grainy_big");
    public static Block GRAINY = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_grainy");
    public static Block GRAINY_LOW = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_grainy_low");
    public static Block BRICK = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_brick");
    public static Block BRICK_BIG = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_brick_big");
    public static Block BORDERED = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_bordered");
    public static Block CHISELED = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_chiseled");
    public static Block BROKEN_BRICK_BIG = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_broken_brick_big");
    public static Block CLAY = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_clay");
    public static Block STRIPS = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_strips");
    public static Block GRAVEL = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_gravel");
    public static Block SAND = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_sand");
    public static Block STONE = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_stone");
    public static Block CORK = new Block(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW)).setRegistryName("colored_cork");
    
    public static Block WATER = new BlockWater(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).noCollission()).setRegistryName("colored_water");
    public static Block WHITE_WATER = new BlockWater(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).noCollission()).setRegistryName("colored_white_water");
    
    public static Block LAVA = new BlockLava(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.COLOR_RED).noCollission()).setRegistryName("colored_lava");
    public static Block WHITE_LAVA = new BlockLava(BlockBehaviour.Properties.of(Material.STONE, MaterialColor.SNOW).noCollission().lightLevel((state) -> {
        return 15;
    })).setRegistryName("colored_white_lava");
    
    public static Block STORAGE_BLOCK = new Block(BlockBehaviour.Properties.of(Material.WOOD).destroyTime(1.5F).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("storage");
    
    public static Block FLOWING_WATER = new BlockFlowingWater(WATER).setRegistryName("colored_water_flowing");
    public static Block WHITE_FLOWING_WATER = new BlockFlowingWater(WHITE_WATER).setRegistryName("colored_white_water_flowing");
    
    public static Block FLOWING_LAVA = new BlockFlowingLava(LAVA).setRegistryName("colored_lava_flowing");
    public static Block WHITE_FLOWING_LAVA = new BlockFlowingLava(WHITE_LAVA).setRegistryName("colored_white_lava_flowing");
    
    public static Block SINGLE_CABLE = new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.DECORATION)).setRegistryName("cable_single");
    
    public static Block INPUT_ARROW = new BlockArrow().setRegistryName("arrow_input");
    public static Block OUTPUT_ARROW = new BlockArrow().setRegistryName("arrow_output");
    
    public static Block SIGNAL_CONVERTER = new BlockSignalConverter().setRegistryName("signal_converter");
    
    public static Tag<Block> STORAGE_BLOCKS;
    
    public static Item HAMMER;
    public static Item BLUEPRINT;
    public static Item ITEM_TILES;
    public static Item SAW;
    public static Item BAG;
    public static Item WRENCH;
    public static Item SCREWDRIVER;
    public static Item CHISEL;
    public static Item PAINT_BRUSH;
    public static Item GLOVE;
    public static Item PREMADE;
    
    public static Item BLOCK_INGREDIENT;
    
    public static Item BLACK_COLOR;
    public static Item CYAN_COLOR;
    public static Item MAGENTA_COLOR;
    public static Item YELLOW_COLOR;
    
    public static EntityType<PrimedSizedTnt> SIZED_TNT_TYPE;
    public static EntityType<EntitySit> SIT_TYPE;
    
    public static CreativeModeTab LITTLE_TAB = new CreativeModeTab("littletiles") {
        
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(HAMMER);
        }
    };
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client);
        
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, this::registerBlockEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(EntityType.class, this::registerEntities);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
    }
    
    private void init(final FMLCommonSetupEvent event) {
        BLOCK_TILES = new BlockTile(Material.STONE, false, false).setRegistryName("tiles");
        BLOCK_TILES_TICKING = new BlockTile(Material.STONE, true, false).setRegistryName("tiles_ticking");
        BLOCK_TILES_RENDERED = new BlockTile(Material.STONE, false, true).setRegistryName("tiles_rendered");
        BLOCK_TILES_TICKING_RENDERED = new BlockTile(Material.STONE, true, true).setRegistryName("tiles_ticking_rendered");
        
        HAMMER = new ItemLittleHammer().setRegistryName("hammer");
        BLUEPRINT = new ItemLittleBlueprint().setRegistryName("blueprint");
        ITEM_TILES = new ItemMultiTiles().setRegistryName("tiles");
        SAW = new ItemLittleSaw().setRegistryName("saw");
        BAG = new ItemLittleBag().setRegistryName("bag");
        WRENCH = new ItemLittleWrench().setRegistryName("wrench");
        SCREWDRIVER = new ItemLittleScrewdriver().setRegistryName("screwdriver");
        CHISEL = new ItemLittleChisel().setRegistryName("chisel");
        PAINT_BRUSH = new ItemLittlePaintBrush().setRegistryName("paint_brush");
        GLOVE = new ItemLittleGlove().setRegistryName("glove");
        PREMADE = new ItemPremadeStructure().setRegistryName("premade");
        
        BLOCK_INGREDIENT = new ItemBlockIngredient().setRegistryName("blockingredient");
        
        BLOCK_INGREDIENT = new ItemColorIngredient(ColorIngredientType.black).setRegistryName("bottle_black");
        CYAN_COLOR = new ItemColorIngredient(ColorIngredientType.cyan).setRegistryName("bottle_cyan");
        MAGENTA_COLOR = new ItemColorIngredient(ColorIngredientType.magenta).setRegistryName("bottle_magenta");
        YELLOW_COLOR = new ItemColorIngredient(ColorIngredientType.yellow).setRegistryName("bottle_yellow");
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        LittlePacketTypes.init();
        
        ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
        
        BE_TILES_TYPE = BlockEntityType.Builder.of((pos, state) -> new BETiles(LittleTiles.BE_TILES_TYPE, pos, state), BLOCK_TILES, BLOCK_TILES_TICKING).build(null)
                .setRegistryName(MODID, "tiles");
        BE_TILES_TYPE_RENDERED = BlockEntityType.Builder
                .of((pos, state) -> new BETiles(LittleTiles.BE_TILES_TYPE_RENDERED, pos, state), BLOCK_TILES_RENDERED, BLOCK_TILES_TICKING_RENDERED).build(null)
                .setRegistryName(MODID, "tiles_rendered");
        BE_SIGNALCONVERTER_TYPE = BlockEntityType.Builder.of(BESignalConverter::new, SIGNAL_CONVERTER).build(null).setRegistryName(MODID, "converter");
        
        SIZED_TNT_TYPE = EntityType.Builder.<PrimedSizedTnt>of(PrimedSizedTnt::new, MobCategory.MISC).build("primed_size_tnt");
        SIT_TYPE = EntityType.Builder.<EntitySit>of(EntitySit::new, MobCategory.MISC).build("sit");
        
        GuiHandler.register("littleStorageStructure", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("blankomatic", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("configure", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("configureadvanced", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("diagnose", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("lt-import", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                return new GuiImport();
            }
        });
        
        GuiHandler.register("lt-export", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                return new GuiExport();
            }
        });
        
        GuiHandler.register("workbench", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("particle", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("structureoverview", new LittleTileGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleTileContext context) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("structureoverview2", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("grabber", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("recipeadvanced", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        GuiHandler.register("structure_builder", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                // TODO Auto-generated method stub
                return null;
            }
        });
        
        NETWORK.registerType(ActionMessagePacket.class, ActionMessagePacket::new);
        NETWORK.registerType(VanillaBlockPacket.class, VanillaBlockPacket::new);
        NETWORK.registerType(BlockPacket.class, BlockPacket::new);
        
        NETWORK.registerType(BedUpdate.class, BedUpdate::new);
        
        NETWORK.registerType(RotatePacket.class, RotatePacket::new);
        NETWORK.registerType(MirrorPacket.class, MirrorPacket::new);
        NETWORK.registerType(SelectionModePacket.class, SelectionModePacket::new);
        NETWORK.registerType(ConfigurePacket.class, ConfigurePacket::new);
        NETWORK.registerType(ScrewdriverSelectionPacket.class, ScrewdriverSelectionPacket::new);
        
        NETWORK.registerType(StructureUpdate.class, StructureUpdate::new);
        NETWORK.registerType(NeighborUpdate.class, NeighborUpdate::new);
        NETWORK.registerType(BlockUpdate.class, BlockUpdate::new);
        NETWORK.registerType(BlocksUpdate.class, BlocksUpdate::new);
        NETWORK.registerType(OutputUpdate.class, OutputUpdate::new);
        
        CreativeCorePacket.registerPacket(LittleActivateDoorPacket.class);
        CreativeCorePacket.registerPacket(LittleEntityRequestPacket.class);
        CreativeCorePacket.registerPacket(LittleResetAnimationPacket.class);
        CreativeCorePacket.registerPacket(LittlePlacedAnimationPacket.class);
        CreativeCorePacket.registerPacket(LittleEntityFixControllerPacket.class);
        CreativeCorePacket.registerPacket(LittleConsumeRightClickEvent.class);
        
        LittleActionRegistry.register(LittleActions.class, LittleActions::new);
        LittleActionRegistry.register(LittleActionPlace.class, LittleActionPlace::new);
        LittleActionRegistry.register(LittleActionActivated.class, LittleActionActivated::new);
        
        LittleAction.registerLittleAction("col", LittleActionColorBoxes.class, LittleActionColorBoxesFiltered.class);
        LittleAction.registerLittleAction("deB", LittleActionDestroyBoxes.class, LittleActionDestroyBoxesFiltered.class);
        LittleAction.registerLittleAction("des", LittleActionDestroy.class);
        
        LittleAction.registerLittleAction("saw", LittleActionSaw.class, LittleActionSawRevert.class);
        
        LittleAction.registerLittleAction("rep", LittleActionReplace.class);
        
        MinecraftForge.EVENT_BUS.register(new LittleEventHandler());
        MinecraftForge.EVENT_BUS.register(WorldAnimationHandler.class);
        // MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        // Entity
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "animation"), EntityAnimation.class, "animation", 2, this, 2000, 250, true);
        
        LittleTilesServer.NEIGHBOR = new NeighborUpdateOrganizer();
        
        TheOneProbeManager.init();
        
        MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        MinecraftForge.EVENT_BUS.register(EntitySizeHandler.class);
        
        LEVEL_HANDLERS.register(LittleAnimationHandlerServer::new);
        
        STORAGE_BLOCKS = BlockTags.createOptional(new ResourceLocation(MODID, "storage_blocks"));
    }
    
    private void client(final FMLClientSetupEvent event) {
        LittleTilesClient.setup(event);
    }
    
    public void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().registerAll(BE_TILES_TYPE, BE_TILES_TYPE_RENDERED, BE_SIGNALCONVERTER_TYPE);
    }
    
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(SIZED_TNT_TYPE, SIT_TYPE);
    }
    
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry()
                .registerAll(new Block[] { CLEAN, FLOOR, GRAINY_BIG, GRAINY, GRAINY_LOW, BRICK, BRICK_BIG, BORDERED, CHISELED, BROKEN_BRICK_BIG, CLAY, STRIPS, GRAVEL, SAND, STONE, CORK, WATER, WHITE_WATER, LAVA, WHITE_LAVA, BLOCK_TILES, BLOCK_TILES_TICKING, BLOCK_TILES_RENDERED, BLOCK_TILES_TICKING_RENDERED, STORAGE_BLOCK, FLOWING_WATER, WHITE_FLOWING_WATER, FLOWING_LAVA, WHITE_FLOWING_LAVA, SINGLE_CABLE, INPUT_ARROW, OUTPUT_ARROW, SIGNAL_CONVERTER });
    }
    
    private static Item createItem(Block block) {
        return new BlockItem(block, new Item.Properties().tab(LITTLE_TAB)).setRegistryName(block.getRegistryName());
    }
    
    public void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry()
                .registerAll(HAMMER, BLUEPRINT, SAW, BAG, WRENCH, SCREWDRIVER, CHISEL, PAINT_BRUSH, ITEM_TILES, GLOVE, PREMADE, BLOCK_INGREDIENT, BLOCK_INGREDIENT, CYAN_COLOR, MAGENTA_COLOR, YELLOW_COLOR, createItem(CLEAN), createItem(FLOOR), createItem(GRAINY_BIG), createItem(GRAINY), createItem(GRAINY_LOW), createItem(BRICK), createItem(BRICK_BIG), createItem(BORDERED), createItem(CHISELED), createItem(BROKEN_BRICK_BIG), createItem(CLAY), createItem(STRIPS), createItem(GRAVEL), createItem(SAND), createItem(STONE), createItem(CORK), createItem(WATER), createItem(STORAGE_BLOCK), createItem(SIGNAL_CONVERTER));
    }
    
    private void serverStarting(final ServerStartingEvent event) {
        Field loadedBlockEntities = ObfuscationReflectionHelper.findField(Level.class, "f_46434_");
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-tovanilla").executes((x) -> {
            x.getSource()
                    .sendSuccess(new TextComponent("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server start <player> <path> [time|ms|s|m|h|d] [loops (-1 -> endless)] " + ChatFormatting.RED + "starts the animation"), false);
            x.getSource()
                    .sendSuccess(new TextComponent("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server stop <player> " + ChatFormatting.RED + "stops the animation"), false);
            x.getSource()
                    .sendSuccess(new TextComponent("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server list " + ChatFormatting.RED + "lists all saved paths"), false);
            x.getSource()
                    .sendSuccess(new TextComponent("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server remove <name> " + ChatFormatting.RED + "removes the given path"), false);
            
            Level level = x.getSource().getLevel();
            List<BETiles> blocks = new ArrayList<>();
            
            try {
                for (BlockEntity be : (Set<BlockEntity>) loadedBlockEntities.get(level))
                    if (be instanceof BETiles)
                        blocks.add((BETiles) be);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
            x.getSource().sendSuccess(new TextComponent("Attempting to convert " + blocks.size() + " blocks!"), false);
            int converted = 0;
            int i = 0;
            for (BETiles be : blocks) {
                if (be.convertBlockToVanilla())
                    converted++;
                i++;
                if (i % 50 == 0)
                    x.getSource().sendSuccess(new TextComponent("Processed " + i + "/" + blocks.size() + " and converted " + converted), false);
            }
            x.getSource().sendSuccess(new TextComponent("Converted " + converted + " blocks"), false);
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-export").executes((x) -> {
            GuiHandler.openGui("lt-export", new CompoundTag(), x.getSource().getPlayerOrException());
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-import").executes((x) -> {
            GuiHandler.openGui("lt-import", new CompoundTag(), x.getSource().getPlayerOrException());
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-open").then(Commands.argument("position", BlockPosArgument.blockPos()).executes((x) -> {
            List<LittleDoor> doors = new ArrayList<>();
            
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(x, "position");
            Level level = x.getSource().getLevel();
            
            for (LittleDoor door : WorldAnimationHandler.getHandler(level).findAnimations(pos))
                doors.add(door);
            
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BETiles) {
                for (LittleStructure structure : ((BETiles) blockEntity).loadedStructures()) {
                    if (structure instanceof LittleDoor) {
                        try {
                            structure = ((LittleDoor) structure).getParentDoor();
                            if (!doors.contains(structure)) {
                                try {
                                    structure.checkConnections();
                                    doors.add((LittleDoor) structure);
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                    x.getSource().sendFailure(new TranslatableComponent("commands.open.notloaded"));
                                }
                            }
                        } catch (LittleActionException e) {}
                    }
                }
            }
            
            for (LittleDoor door : doors) {
                try {
                    door.activate(DoorActivator.COMMAND, null, null, true);
                } catch (LittleActionException e) {}
            }
            return 0;
        })).then(Commands.argument("names", StringArrayArgumentType.stringArray()).executes(x -> {
            List<LittleDoor> doors = new ArrayList<>();
            
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(x, "position");
            Level level = x.getSource().getLevel();
            String[] args = StringArrayArgumentType.getStringArray(x, "names");
            
            for (LittleDoor door : WorldAnimationHandler.getHandler(level).findAnimations(pos))
                if (checkStructureName(door, args))
                    doors.add(door);
                
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BETiles) {
                for (LittleStructure structure : ((BETiles) blockEntity).loadedStructures()) {
                    if (structure instanceof LittleDoor) {
                        try {
                            structure = ((LittleDoor) structure).getParentDoor();
                            if (checkStructureName(structure, args) && !doors.contains(structure)) {
                                try {
                                    structure.checkConnections();
                                    doors.add((LittleDoor) structure);
                                } catch (CorruptedConnectionException | NotYetConnectedException e) {
                                    x.getSource().sendFailure(new TranslatableComponent("commands.open.notloaded"));
                                }
                            }
                        } catch (LittleActionException e) {}
                    }
                }
            }
            
            for (LittleDoor door : doors) {
                try {
                    door.activate(DoorActivator.COMMAND, null, null, true);
                } catch (LittleActionException e) {}
            }
            return 0;
        })));
    }
    
    protected boolean checkStructureName(LittleStructure structure, String[] args) {
        for (int i = 0; i < args.length; i++)
            if (structure.name != null && structure.name.equalsIgnoreCase(args[i]))
                return true;
        return false;
    }
    
}
