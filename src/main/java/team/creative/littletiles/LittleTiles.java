package team.creative.littletiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkHolder.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import team.creative.creativecore.common.config.holder.CreativeConfigRegistry;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.client.LittleTilesClient;
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
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.element.LittleElement;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.group.LittleGroupAbsolute;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.entity.EntitySizeHandler;
import team.creative.littletiles.common.entity.LittleEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationEntity;
import team.creative.littletiles.common.entity.animation.LittleAnimationLevel;
import team.creative.littletiles.common.entity.level.LittleLevelEntity;
import team.creative.littletiles.common.grid.LittleGrid;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;
import team.creative.littletiles.common.item.ItemMultiTiles.ExampleStructures;
import team.creative.littletiles.common.item.LittleToolHandler;
import team.creative.littletiles.common.level.handler.LittleAnimationHandlers;
import team.creative.littletiles.common.level.little.LittleSubLevel;
import team.creative.littletiles.common.level.tick.LittleTickers;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import team.creative.littletiles.common.packet.LittlePacketTypes;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
import team.creative.littletiles.common.packet.entity.EntityOriginChanged;
import team.creative.littletiles.common.packet.entity.LittleEntityPhysicPacket;
import team.creative.littletiles.common.packet.entity.animation.LittleAnimationBlocksPacket;
import team.creative.littletiles.common.packet.entity.animation.LittleAnimationInitPacket;
import team.creative.littletiles.common.packet.entity.level.LittleLevelInitPacket;
import team.creative.littletiles.common.packet.entity.level.LittleLevelPacket;
import team.creative.littletiles.common.packet.entity.level.LittleLevelPackets;
import team.creative.littletiles.common.packet.item.MirrorPacket;
import team.creative.littletiles.common.packet.item.RotatePacket;
import team.creative.littletiles.common.packet.item.ScrewdriverSelectionPacket;
import team.creative.littletiles.common.packet.item.SelectionModePacket;
import team.creative.littletiles.common.packet.structure.BedUpdate;
import team.creative.littletiles.common.packet.structure.StructureBlockToEntityPacket;
import team.creative.littletiles.common.packet.structure.StructureEntityToBlockPacket;
import team.creative.littletiles.common.packet.structure.StructureStartAnimationPacket;
import team.creative.littletiles.common.packet.structure.StructureUpdate;
import team.creative.littletiles.common.packet.update.BlockUpdate;
import team.creative.littletiles.common.packet.update.BlocksUpdate;
import team.creative.littletiles.common.packet.update.NeighborUpdate;
import team.creative.littletiles.common.packet.update.OutputUpdate;
import team.creative.littletiles.common.placement.Placement;
import team.creative.littletiles.common.placement.PlacementPreview;
import team.creative.littletiles.common.placement.PlacementResult;
import team.creative.littletiles.common.placement.mode.PlacementMode;
import team.creative.littletiles.common.recipe.StructureIngredient.StructureIngredientSerializer;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.registry.premade.LittlePremadeRegistry;
import team.creative.littletiles.common.structure.relative.StructureAbsolute;
import team.creative.littletiles.common.structure.type.bed.LittleBedEventHandler;
import team.creative.littletiles.common.structure.type.premade.LittleExporter;
import team.creative.littletiles.common.structure.type.premade.LittleImporter;
import team.creative.littletiles.common.structure.type.premade.LittleStructurePremade.LittlePremadeType;
import team.creative.littletiles.mixin.server.level.ChunkMapAccessor;
import team.creative.littletiles.server.LittleTilesServer;

@Mod(LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));
    public static final LittleAnimationHandlers ANIMATION_HANDLERS = new LittleAnimationHandlers();
    public static final LittleTickers TICKERS = new LittleTickers();
    
    public static TagKey<Block> STORAGE_BLOCKS;
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> LittleTilesClient.load(FMLJavaModLoadingContext.get().getModEventBus()));
        
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        
        LittleTilesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::buildContents);
        
        LittlePacketTypes.init();
    }
    
    public void buildContents(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(MODID, "items"), x -> x.title(Component.translatable("itemGroup.littletiles"))
                .icon(() -> new ItemStack(LittleTilesRegistry.HAMMER.get())).displayItems((features, output) -> {
                    for (ExampleStructures example : ExampleStructures.values())
                        if (example.stack != null)
                            output.accept(example.stack);
                        
                    for (LittlePremadeType entry : LittlePremadeRegistry.types())
                        if (entry.showInCreativeTab && !entry.hasCustomTab())
                            output.accept(entry.createItemStack());
                        
                    output.accept(LittleTilesRegistry.HAMMER.get());
                    output.accept(LittleTilesRegistry.CHISEL.get());
                    output.accept(LittleTilesRegistry.BLUEPRINT.get());
                    
                    output.accept(LittleTilesRegistry.BAG.get());
                    output.accept(LittleTilesRegistry.GLOVE.get());
                    
                    output.accept(LittleTilesRegistry.PAINT_BRUSH.get());
                    output.accept(LittleTilesRegistry.SAW.get());
                    output.accept(LittleTilesRegistry.SCREWDRIVER.get());
                    output.accept(LittleTilesRegistry.WRENCH.get());
                    
                    output.accept(LittleTilesRegistry.SIGNAL_CONVERTER.get());
                    output.accept(LittleTilesRegistry.STORAGE_BLOCK.get());
                    
                    output.accept(LittleTilesRegistry.CLEAN.get());
                    output.accept(LittleTilesRegistry.FLOOR.get());
                    output.accept(LittleTilesRegistry.GRAINY_BIG.get());
                    output.accept(LittleTilesRegistry.GRAINY.get());
                    output.accept(LittleTilesRegistry.GRAINY_LOW.get());
                    output.accept(LittleTilesRegistry.BRICK.get());
                    output.accept(LittleTilesRegistry.BRICK_BIG.get());
                    output.accept(LittleTilesRegistry.BORDERED.get());
                    output.accept(LittleTilesRegistry.CHISELED.get());
                    output.accept(LittleTilesRegistry.BROKEN_BRICK_BIG.get());
                    output.accept(LittleTilesRegistry.CLAY.get());
                    output.accept(LittleTilesRegistry.STRIPS.get());
                    output.accept(LittleTilesRegistry.GRAVEL.get());
                    output.accept(LittleTilesRegistry.SAND.get());
                    output.accept(LittleTilesRegistry.STONE.get());
                    output.accept(LittleTilesRegistry.CORK.get());
                    
                    output.accept(LittleTilesRegistry.WATER.get());
                    output.accept(LittleTilesRegistry.WHITE_WATER.get());
                    
                    output.accept(LittleTilesRegistry.LAVA.get());
                    output.accept(LittleTilesRegistry.WHITE_LAVA.get());
                    
                }));
    }
    
    private void init(final FMLCommonSetupEvent event) {
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        
        NETWORK.registerType(ActionMessagePacket.class, ActionMessagePacket::new);
        NETWORK.registerType(VanillaBlockPacket.class, VanillaBlockPacket::new);
        NETWORK.registerType(BlockPacket.class, BlockPacket::new);
        
        NETWORK.registerType(RotatePacket.class, RotatePacket::new);
        NETWORK.registerType(MirrorPacket.class, MirrorPacket::new);
        NETWORK.registerType(SelectionModePacket.class, SelectionModePacket::new);
        NETWORK.registerType(ScrewdriverSelectionPacket.class, ScrewdriverSelectionPacket::new);
        
        NETWORK.registerType(BedUpdate.class, BedUpdate::new);
        NETWORK.registerType(StructureBlockToEntityPacket.class, StructureBlockToEntityPacket::new);
        NETWORK.registerType(StructureEntityToBlockPacket.class, StructureEntityToBlockPacket::new);
        NETWORK.registerType(StructureUpdate.class, StructureUpdate::new);
        NETWORK.registerType(StructureStartAnimationPacket.class, StructureStartAnimationPacket::new);
        
        NETWORK.registerType(NeighborUpdate.class, NeighborUpdate::new);
        NETWORK.registerType(BlockUpdate.class, BlockUpdate::new);
        NETWORK.registerType(BlocksUpdate.class, BlocksUpdate::new);
        NETWORK.registerType(OutputUpdate.class, OutputUpdate::new);
        
        NETWORK.registerType(EntityOriginChanged.class, EntityOriginChanged::new);
        NETWORK.registerType(LittleEntityPhysicPacket.class, LittleEntityPhysicPacket::new);
        
        NETWORK.registerType(LittleAnimationInitPacket.class, LittleAnimationInitPacket::new);
        NETWORK.registerType(LittleAnimationBlocksPacket.class, LittleAnimationBlocksPacket::new);
        
        NETWORK.registerType(LittleLevelPacket.class, LittleLevelPacket::new);
        NETWORK.registerType(LittleLevelPackets.class, LittleLevelPackets::new);
        NETWORK.registerType(LittleLevelInitPacket.class, LittleLevelInitPacket::new);
        
        CreativeConfigRegistry.ROOT.registerValue(MODID, CONFIG = new LittleTilesConfig());
        
        LittleActionRegistry.register(LittleActions.class, LittleActions::new);
        LittleActionRegistry.register(LittleActionPlace.class, LittleActionPlace::new);
        LittleActionRegistry.register(LittleActionActivated.class, LittleActionActivated::new);
        LittleActionRegistry.register(LittleActionColorBoxes.class, LittleActionColorBoxes::new);
        LittleActionRegistry.register(LittleActionColorBoxesFiltered.class, LittleActionColorBoxesFiltered::new);
        LittleActionRegistry.register(LittleActionDestroyBoxes.class, LittleActionDestroyBoxes::new);
        LittleActionRegistry.register(LittleActionDestroyBoxesFiltered.class, LittleActionDestroyBoxesFiltered::new);
        LittleActionRegistry.register(LittleActionDestroy.class, LittleActionDestroy::new);
        
        MinecraftForge.EVENT_BUS.register(new LittleBedEventHandler());
        MinecraftForge.EVENT_BUS.register(LittleAnimationHandlers.class);
        // MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        MinecraftForge.EVENT_BUS.register(new LittleToolHandler());
        
        LittleTilesServer.init(event);
        
        if (ModList.get().isLoaded(TheOneProbeManager.modid))
            TheOneProbeManager.init();
        
        //MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        MinecraftForge.EVENT_BUS.register(EntitySizeHandler.class);
        
        STORAGE_BLOCKS = BlockTags.create(new ResourceLocation(MODID, "storage_blocks"));
        
        CraftingHelper.register(new ResourceLocation(MODID, "structure"), StructureIngredientSerializer.INSTANCE);
        
        LittleTilesGuiRegistry.init();
    }
    
    private void serverStarting(final ServerStartingEvent event) {
        ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-tovanilla").executes((x) -> {
            x.getSource().sendSuccess(Component
                    .literal("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server start <player> <path> [time|ms|s|m|h|d] [loops (-1 -> endless)] " + ChatFormatting.RED + "starts the animation"), false);
            x.getSource().sendSuccess(Component
                    .literal("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server stop <player> " + ChatFormatting.RED + "stops the animation"), false);
            x.getSource()
                    .sendSuccess(Component.literal("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server list " + ChatFormatting.RED + "lists all saved paths"), false);
            x.getSource().sendSuccess(Component
                    .literal("" + ChatFormatting.BOLD + ChatFormatting.YELLOW + "/cam-server remove <name> " + ChatFormatting.RED + "removes the given path"), false);
            
            ServerLevel level = x.getSource().getLevel();
            List<BETiles> blocks = new ArrayList<>();
            
            level.getChunkSource().getLoadedChunksCount();
            for (ChunkHolder holder : ((ChunkMapAccessor) level.getChunkSource().chunkMap).callGetChunks())
                if (holder.getFullStatus() == FullChunkStatus.TICKING)
                    for (BlockEntity be : holder.getTickingChunk().getBlockEntities().values())
                        if (be instanceof BETiles)
                            blocks.add((BETiles) be);
                        
            x.getSource().sendSuccess(Component.literal("Attempting to convert " + blocks.size() + " blocks!"), false);
            int converted = 0;
            int i = 0;
            for (BETiles be : blocks) {
                if (be.convertBlockToVanilla())
                    converted++;
                i++;
                if (i % 50 == 0)
                    x.getSource().sendSuccess(Component.literal("Processed " + i + "/" + blocks.size() + " and converted " + converted), false);
            }
            x.getSource().sendSuccess(Component.literal("Converted " + converted + " blocks"), false);
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-export").executes((x) -> {
            LittleExporter.GUI.open(x.getSource().getPlayerOrException());
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-import").executes((x) -> {
            LittleImporter.GUI.open(x.getSource().getPlayerOrException());
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("level").executes((x) -> {
            try {
                ServerLevel level = x.getSource().getLevel();
                BlockPos pos = BlockPos.containing(x.getSource().getPosition()).above();
                
                LittleEntity entity = new LittleLevelEntity(level, pos);
                
                LittleSubLevel subLevel = entity.getSubLevel();
                LittleGrid grid = LittleGrid.defaultGrid();
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", LittleStructureRegistry.REGISTRY.getDefault().id);
                LittleGroup group = new LittleGroup(nbt, Collections.EMPTY_LIST);
                group.add(grid, new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE), new LittleBox(0, grid.count - 1, 0, grid.count, grid.count, grid.count));
                subLevel.setBlock(pos.above(), Blocks.DIRT.defaultBlockState(), 3);
                PlacementPreview preview = PlacementPreview.load(null, PlacementMode.all, new LittleGroupAbsolute(pos, group), Facing.EAST);
                
                Placement placement = new Placement(null, (Level) subLevel, preview);
                PlacementResult result = placement.place();
                if (result == null)
                    throw new LittleActionException("Could not be placed");
                
                level.addFreshEntity(entity);
                x.getSource().sendSystemMessage(Component.literal("Spawned level"));
            } catch (LittleActionException e) {
                x.getSource().sendFailure(e.getTranslatable());
            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            } catch (Error e) {
                e.printStackTrace();
            }
            
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("animation").executes((x) -> {
            try {
                ServerLevel level = x.getSource().getLevel();
                BlockPos pos = BlockPos.containing(x.getSource().getPosition()).above();
                
                LittleAnimationLevel subLevel = new LittleAnimationLevel(level);
                LittleGrid grid = LittleGrid.defaultGrid();
                CompoundTag nbt = new CompoundTag();
                nbt.putString("id", LittleStructureRegistry.REGISTRY.getDefault().id);
                LittleGroup group = new LittleGroup(nbt, Collections.EMPTY_LIST);
                group.add(grid, new LittleElement(Blocks.STONE.defaultBlockState(), ColorUtils.WHITE), new LittleBox(0, grid.count - 1, 0, grid.count, grid.count, grid.count));
                PlacementPreview preview = PlacementPreview.load(null, PlacementMode.all, new LittleGroupAbsolute(pos, group), Facing.EAST);
                level.addFreshEntity(new LittleAnimationEntity(level, subLevel, new StructureAbsolute(pos, grid.box(), grid), new Placement(null, subLevel, preview)));
                x.getSource().sendSystemMessage(Component.literal("Spawned animation"));
            } catch (LittleActionException e) {
                x.getSource().sendFailure(e.getTranslatable());
            } catch (Error | Exception e) {
                e.printStackTrace();
                throw e;
            }
            return 0;
        }));
        
        /*event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-open").then(Commands.argument("position", BlockPosArgument.blockPos()).executes((x) -> {
            List<LittleDoor> doors = new ArrayList<>();
            
            BlockPos pos = BlockPosArgument.getLoadedBlockPos(x, "position");
            Level level = x.getSource().getLevel();
            
            for (LittleDoor door : findDoors(LittleAnimationHandlers.get(level), new AABB(pos)))
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
                                    x.getSource().sendFailure(Component.translatable("commands.open.notloaded"));
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
            
            for (LittleDoor door : findDoors(LittleAnimationHandlers.get(level), new AABB(pos)))
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
                                    x.getSource().sendFailure(Component.translatable("commands.open.notloaded"));
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
        })));*/
    }
    
    /*public static List<LittleDoor> findDoors(LittleAnimationHandler handler, AABB box) {
        List<LittleDoor> doors = new ArrayList<>();
        for (LittleLevelEntity entity : handler.entities)
            try {
                if (entity.getStructure() instanceof LittleDoor && entity.getBoundingBox().intersects(box) && !doors.contains(entity.getStructure()))
                    doors.add(((LittleDoor) entity.getStructure()).getParentDoor());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return doors;
    }*/
    
    protected boolean checkStructureName(LittleStructure structure, String[] args) {
        for (int i = 0; i < args.length; i++)
            if (structure.name != null && structure.name.equalsIgnoreCase(args[i]))
                return true;
        return false;
    }
    
}
