package team.creative.littletiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.handler.GuiHandler;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.creativecore.common.util.argument.StringArrayArgumentType;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
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
import team.creative.littletiles.common.animation.entity.LittleLevelEntity;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.entity.EntitySizeHandler;
import team.creative.littletiles.common.gui.GuiStorage;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiHandler;
import team.creative.littletiles.common.gui.handler.LittleTileGuiHandler;
import team.creative.littletiles.common.gui.premade.GuiExport;
import team.creative.littletiles.common.gui.premade.GuiImport;
import team.creative.littletiles.common.gui.structure.GuiBuilder;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;
import team.creative.littletiles.common.item.LittleToolHandler;
import team.creative.littletiles.common.level.LittleAnimationHandler;
import team.creative.littletiles.common.level.LittleAnimationHandlers;
import team.creative.littletiles.common.mod.chiselsandbits.ChiselAndBitsConveration;
import team.creative.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import team.creative.littletiles.common.packet.LittlePacketTypes;
import team.creative.littletiles.common.packet.action.ActionMessagePacket;
import team.creative.littletiles.common.packet.action.BlockPacket;
import team.creative.littletiles.common.packet.action.VanillaBlockPacket;
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
import team.creative.littletiles.common.recipe.StructureIngredient.StructureIngredientSerializer;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.exception.CorruptedConnectionException;
import team.creative.littletiles.common.structure.exception.NotYetConnectedException;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.structure.signal.LittleSignalHandler;
import team.creative.littletiles.common.structure.type.LittleStorage;
import team.creative.littletiles.common.structure.type.bed.LittleBedEventHandler;
import team.creative.littletiles.common.structure.type.door.LittleDoor;
import team.creative.littletiles.common.structure.type.door.LittleDoor.DoorActivator;
import team.creative.littletiles.common.structure.type.premade.LittleStructureBuilder;
import team.creative.littletiles.server.LittleTilesServer;

@Mod(value = LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    public static final String VERSION = "1.6.0";
    
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));
    
    public static Tag<Block> STORAGE_BLOCKS;
    
    public static final CreativeModeTab LITTLE_TAB = new CreativeModeTab("littletiles") {
        
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(LittleTilesRegistry.HAMMER.get());
        }
    };
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(LittleTilesClient::commands));
        
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
        
        LittleTilesRegistry.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        LittleTilesRegistry.ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
    
    private void init(final FMLCommonSetupEvent event) {
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        LittlePacketTypes.init();
        
        GuiHandler.register("storage", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                if (structure instanceof LittleStorage)
                    return new GuiStorage((LittleStorage) structure, player);
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
                if (player.getMainHandItem().getItem() instanceof ILittleTool)
                    return ((ILittleTool) player.getMainHandItem().getItem()).getConfigure(player, ContainerSlotView.mainHand(player));
                return null;
            }
        });
        
        GuiHandler.register("configureadvanced", new GuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt) {
                if (player.getMainHandItem().getItem() instanceof ILittleTool)
                    return ((ILittleTool) player.getMainHandItem().getItem()).getConfigureAdvanced(player, ContainerSlotView.mainHand(player));
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
        
        GuiHandler.register("structure_builder", new LittleStructureGuiHandler() {
            
            @Override
            public GuiLayer create(Player player, CompoundTag nbt, LittleStructure structure) {
                if (structure instanceof LittleStructureBuilder)
                    return new GuiBuilder((LittleStructureBuilder) structure);
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
        NETWORK.registerType(ScrewdriverSelectionPacket.class, ScrewdriverSelectionPacket::new);
        
        NETWORK.registerType(StructureUpdate.class, StructureUpdate::new);
        NETWORK.registerType(NeighborUpdate.class, NeighborUpdate::new);
        NETWORK.registerType(BlockUpdate.class, BlockUpdate::new);
        NETWORK.registerType(BlocksUpdate.class, BlocksUpdate::new);
        NETWORK.registerType(OutputUpdate.class, OutputUpdate::new);
        
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
        MinecraftForge.EVENT_BUS.register(new LittleSignalHandler());
        MinecraftForge.EVENT_BUS.register(new LittleToolHandler());
        
        LittleTilesServer.init(event);
        
        TheOneProbeManager.init();
        
        MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        MinecraftForge.EVENT_BUS.register(EntitySizeHandler.class);
        
        STORAGE_BLOCKS = BlockTags.createOptional(new ResourceLocation(MODID, "storage_blocks"));
        
        CraftingHelper.register(new ResourceLocation(MODID, "structure"), StructureIngredientSerializer.INSTANCE);
    }
    
    private void client(final FMLClientSetupEvent event) {
        LittleTilesClient.setup(event);
    }
    
    private void serverStarting(final ServerStartingEvent event) {
        
        ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
        
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
    
    public static List<LittleDoor> findDoors(LittleAnimationHandler handler, AABB box) {
        List<LittleDoor> doors = new ArrayList<>();
        for (LittleLevelEntity entity : handler.entities)
            try {
                if (entity.getStructure() instanceof LittleDoor && entity.getBoundingBox().intersects(box) && !doors.contains(entity.getStructure()))
                    doors.add(((LittleDoor) entity.getStructure()).getParentDoor());
            } catch (CorruptedConnectionException | NotYetConnectedException e) {}
        return doors;
    }
    
    protected boolean checkStructureName(LittleStructure structure, String[] args) {
        for (int i = 0; i < args.length; i++)
            if (structure.name != null && structure.name.equalsIgnoreCase(args[i]))
                return true;
        return false;
    }
    
}
