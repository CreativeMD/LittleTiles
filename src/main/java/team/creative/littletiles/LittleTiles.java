package team.creative.littletiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.littletiles.CustomGuiHandler;
import com.creativemd.littletiles.EntityPlayer;
import com.creativemd.littletiles.IParentTileList;
import com.creativemd.littletiles.NBTTagCompound;
import com.creativemd.littletiles.SideOnly;
import com.creativemd.littletiles.StructureTileList;
import com.creativemd.littletiles.SubContainer;
import com.creativemd.littletiles.SubGui;
import com.creativemd.littletiles.client.gui.SubGuiBlankOMatic;
import com.creativemd.littletiles.client.gui.SubGuiBuilder;
import com.creativemd.littletiles.client.gui.SubGuiDiagnose;
import com.creativemd.littletiles.client.gui.SubGuiExport;
import com.creativemd.littletiles.client.gui.SubGuiImport;
import com.creativemd.littletiles.client.gui.SubGuiParticle;
import com.creativemd.littletiles.client.gui.SubGuiRecipe;
import com.creativemd.littletiles.client.gui.SubGuiRecipeAdvancedSelection;
import com.creativemd.littletiles.client.gui.SubGuiStorage;
import com.creativemd.littletiles.client.gui.SubGuiStructureOverview;
import com.creativemd.littletiles.client.gui.SubGuiWorkbench;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.client.gui.handler.LittleTileGuiHandler;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute.LittleActionPlaceAbsolutePremade;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.block.LittleActionReplace;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw.LittleActionSawRevert;
import com.creativemd.littletiles.common.command.OpenCommand;
import com.creativemd.littletiles.common.container.SubContainerBlankOMatic;
import com.creativemd.littletiles.common.container.SubContainerBuilder;
import com.creativemd.littletiles.common.container.SubContainerDiagnose;
import com.creativemd.littletiles.common.container.SubContainerExport;
import com.creativemd.littletiles.common.container.SubContainerImport;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.container.SubContainerRecipeAdvanced;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.container.SubContainerStructureOverview;
import com.creativemd.littletiles.common.container.SubContainerWorkbench;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.mod.albedo.AlbedoExtension;
import com.creativemd.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import com.creativemd.littletiles.common.mod.warpdrive.TileEntityLittleTilesTransformer;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.structure.type.premade.LittleBlankOMatic;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder;
import com.creativemd.littletiles.common.util.converation.ChiselAndBitsConveration;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionRegistry;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.block.BlockArrow;
import team.creative.littletiles.common.block.BlockFlowingLava;
import team.creative.littletiles.common.block.BlockFlowingWater;
import team.creative.littletiles.common.block.BlockLava;
import team.creative.littletiles.common.block.BlockSignalConverter;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.block.BlockWater;
import team.creative.littletiles.common.block.entity.BESignalConverter;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.block.entity.BETilesRendered;
import team.creative.littletiles.common.block.entity.BETilesTicking;
import team.creative.littletiles.common.block.entity.BETilesTickingRendered;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.entity.EntityAnimation;
import team.creative.littletiles.common.entity.EntitySit;
import team.creative.littletiles.common.entity.EntitySizeHandler;
import team.creative.littletiles.common.entity.EntitySizedTNTPrimed;
import team.creative.littletiles.common.entity.PrimedSizedTnt;
import team.creative.littletiles.common.ingredient.rules.IngredientRules;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGrabber;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.item.ItemLittleRecipeAdvanced;
import team.creative.littletiles.common.item.ItemLittleSaw;
import team.creative.littletiles.common.item.ItemLittleScrewdriver;
import team.creative.littletiles.common.item.ItemLittleUtilityKnife;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.item.ItemMultiTiles;
import team.creative.littletiles.common.item.ItemPremadeStructure;
import team.creative.littletiles.common.packet.LittleActivateDoorPacket;
import team.creative.littletiles.common.packet.LittleBedPacket;
import team.creative.littletiles.common.packet.LittleBlockPacket;
import team.creative.littletiles.common.packet.LittleConsumeRightClickEvent;
import team.creative.littletiles.common.packet.LittleEntityFixControllerPacket;
import team.creative.littletiles.common.packet.LittleEntityRequestPacket;
import team.creative.littletiles.common.packet.LittlePacketTypes;
import team.creative.littletiles.common.packet.LittlePlacedAnimationPacket;
import team.creative.littletiles.common.packet.LittleResetAnimationPacket;
import team.creative.littletiles.common.packet.LittleScrewdriverSelectionPacket;
import team.creative.littletiles.common.packet.LittleSelectionModePacket;
import team.creative.littletiles.common.packet.LittleUpdateOutputPacket;
import team.creative.littletiles.common.packet.LittleUpdateStructurePacket;
import team.creative.littletiles.common.packet.LittleVanillaBlockPacket;
import team.creative.littletiles.common.packet.action.LittleActionMessagePacket;
import team.creative.littletiles.common.packet.item.LittleMirrorPacket;
import team.creative.littletiles.common.packet.item.LittleRotatePacket;
import team.creative.littletiles.common.packet.update.LittleBlockUpdatePacket;
import team.creative.littletiles.common.packet.update.LittleBlocksUpdatePacket;
import team.creative.littletiles.common.packet.update.LittleNeighborUpdatePacket;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.registry.LittleStructureRegistry;
import team.creative.littletiles.common.tile.LittleTile;
import team.creative.littletiles.server.LittleTilesServer;
import team.creative.littletiles.server.NeighborUpdateOrganizer;

@Mod(value = LittleTiles.MODID)
public class LittleTiles {
    
    public static final String MODID = "littletiles";
    public static final String VERSION = "1.6.0";
    
    public static BlockEntityType BE_SIGNALCONVERTER_TYPE;
    public static BlockEntityType BE_TILES_TYPE;
    public static BlockEntityType BE_TILES_TYPE_RENDERED;
    public static BlockEntityType BE_TILES_TYPE_TICKING;
    public static BlockEntityType BE_TILES_TYPE_TICKING_RENDERED;
    public static LittleTilesConfig CONFIG;
    public static final Logger LOGGER = LogManager.getLogger(LittleTiles.MODID);
    public static final CreativeNetwork NETWORK = new CreativeNetwork("1.0", LOGGER, new ResourceLocation(LittleTiles.MODID, "main"));
    
    public static Block blockTile;
    public static Block blockTileTicking;
    public static Block blockTileRendered;
    public static Block blockTileTickingRendered;
    
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
    
    public static Block storageBlock = new Block(BlockBehaviour.Properties.of(Material.WOOD).destroyTime(1.5F).strength(1.5F).sound(SoundType.WOOD)).setRegistryName("storage");
    
    public static Block flowingWater = new BlockFlowingWater(WATER).setRegistryName("colored_water_flowing");
    public static Block whiteFlowingWater = new BlockFlowingWater(WHITE_WATER).setRegistryName("colored_white_water_flowing");
    
    public static Block flowingLava = new BlockFlowingLava(LAVA).setRegistryName("colored_lava_flowing");
    public static Block whiteFlowingLava = new BlockFlowingLava(WHITE_LAVA).setRegistryName("colored_white_lava_flowing");
    
    public static Block singleCable = new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.DECORATION)).setRegistryName("cable_single");
    
    public static Block inputArrow = new BlockArrow().setRegistryName("arrow_input");
    public static Block outputArrow = new BlockArrow().setRegistryName("arrow_output");
    
    public static Block signalConverter = new BlockSignalConverter().setRegistryName("signal_converter");
    
    public static Item hammer;
    public static Item recipe;
    public static Item recipeAdvanced;
    public static Item multiTiles;
    public static Item saw;
    public static Item container;
    public static Item wrench;
    public static Item screwdriver;
    public static Item chisel;
    public static Item colorTube;
    public static Item rubberMallet;
    public static Item utilityKnife;
    public static Item grabber;
    public static Item premade;
    
    public static Item blockIngredient;
    
    public static Item blackColorIngredient;
    public static Item cyanColorIngredient;
    public static Item magentaColorIngredient;
    public static Item yellowColorIngredient;
    
    public static EntityType<PrimedSizedTnt> SIZED_TNT_TYPE;
    public static EntityType<EntitySit> SIT_TYPE;
    
    public static CreativeModeTab littleTab = new CreativeModeTab("littletiles") {
        
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(hammer);
        }
    };
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client);
        MinecraftForge.EVENT_BUS.addListener(this::serverStarting);
    }
    
    private void init(final FMLCommonSetupEvent event) {
        blockTile = new BlockTile(Material.STONE, false, false).setRegistryName("tiles");
        blockTileTicking = new BlockTile(Material.STONE, true, false).setRegistryName("tiles_ticking");
        blockTileRendered = new BlockTile(Material.STONE, false, true).setRegistryName("tiles_rendered");
        blockTileTickingRendered = new BlockTile(Material.STONE, true, true).setRegistryName("tiles_ticking_rendered");
        
        hammer = new ItemLittleHammer().setRegistryName("hammer");
        recipe = new ItemLittleRecipe().setRegistryName("recipe");
        recipeAdvanced = new ItemLittleRecipeAdvanced().setRegistryName("recipeadvanced");
        multiTiles = new ItemMultiTiles().setRegistryName("multiTiles");
        saw = new ItemLittleSaw().setRegistryName("saw");
        container = new ItemLittleBag().setRegistryName("container");
        wrench = new ItemLittleWrench().setRegistryName("wrench");
        screwdriver = new ItemLittleScrewdriver().setRegistryName("screwdriver");
        chisel = new ItemLittleChisel().setRegistryName("chisel");
        colorTube = new ItemLittlePaintBrush().setRegistryName("paint_brush");
        utilityKnife = new ItemLittleUtilityKnife().setRegistryName("utility_knife");
        grabber = new ItemLittleGrabber().setRegistryName("grabber");
        premade = new ItemPremadeStructure().setRegistryName("premade");
        
        blockIngredient = new ItemBlockIngredient().setRegistryName("blockingredient");
        
        blackColorIngredient = new ItemColorIngredient(ColorIngredientType.black).setRegistryName("bottle_black");
        cyanColorIngredient = new ItemColorIngredient(ColorIngredientType.cyan).setRegistryName("bottle_cyan");
        magentaColorIngredient = new ItemColorIngredient(ColorIngredientType.magenta).setRegistryName("bottle_magenta");
        yellowColorIngredient = new ItemColorIngredient(ColorIngredientType.yellow).setRegistryName("bottle_yellow");
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        LittlePacketTypes.init();
        
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> LittleTilesClient::init);
        
        ForgeConfig.SERVER.fullBoundingBoxLadders.set(true);
        
        BE_TILES_TYPE = BlockEntityType.Builder.of(BETiles::new, blockTile).build(null).setRegistryName(MODID, "tiles");
        BE_TILES_TYPE_RENDERED = BlockEntityType.Builder.of(BETilesRendered::new, blockTileRendered).build(null).setRegistryName(MODID, "tiles_rendered");
        BE_TILES_TYPE_TICKING = BlockEntityType.Builder.of(BETilesTicking::new, blockTileTicking).build(null).setRegistryName(MODID, "tiles_ticking");
        BE_TILES_TYPE_TICKING_RENDERED = BlockEntityType.Builder.of(BETilesTickingRendered::new, blockTileTickingRendered).build(null)
                .setRegistryName(MODID, "tiles_ticking_rendered");
        BE_SIGNALCONVERTER_TYPE = BlockEntityType.Builder.of(BESignalConverter::new, signalConverter).build(null).setRegistryName(MODID, "converter");
        
        SIZED_TNT_TYPE = EntityType.Builder.<PrimedSizedTnt>of(PrimedSizedTnt::new, MobCategory.MISC).build("primed_size_tnt");
        SIT_TYPE = EntityType.Builder.<EntitySit>of(EntitySit::new, MobCategory.MISC).build("sit");
        
        GuiHandler.registerGuiHandler("littleStorageStructure", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubGuiStorage((LittleStorage) structure);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubContainerStorage(player, (LittleStorage) structure);
            }
        });
        
        GuiHandler.registerGuiHandler("blankomatic", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubGuiBlankOMatic((LittleBlankOMatic) structure);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubContainerBlankOMatic(player, (LittleBlankOMatic) structure);
            }
        });
        
        GuiHandler.registerGuiHandler("configure", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ILittleTool)
                    return ((ILittleTool) stack.getItem()).getConfigureGUI(player, stack);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ILittleTool)
                    return ((ILittleTool) stack.getItem()).getConfigureContainer(player, stack);
                return null;
            }
        });
        
        GuiHandler.registerGuiHandler("configureadvanced", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ILittleTool)
                    return ((ILittleTool) stack.getItem()).getConfigureGUIAdvanced(player, stack);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                if (stack.getItem() instanceof ILittleTool)
                    return ((ILittleTool) stack.getItem()).getConfigureContainerAdvanced(player, stack);
                return null;
            }
        });
        
        GuiHandler.registerGuiHandler("diagnose", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                UUID uuid = UUID.fromString(nbt.getString("uuid"));
                return new SubGuiDiagnose(uuid, WorldAnimationHandler.client.findAnimation(uuid));
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                return new SubContainerDiagnose(player);
            }
        });
        
        GuiHandler.registerGuiHandler("lt-import", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                return new SubGuiImport();
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                return new SubContainerImport(player);
            }
        });
        
        GuiHandler.registerGuiHandler("lt-export", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                return new SubGuiExport();
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                return new SubContainerExport(player);
            }
        });
        
        GuiHandler.registerGuiHandler("workbench", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                return new SubGuiWorkbench();
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                return new SubContainerWorkbench(player);
            }
        });
        
        GuiHandler.registerGuiHandler("particle", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubGuiParticle((LittleParticleEmitter) structure);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubContainerParticle(player, (LittleParticleEmitter) structure);
            }
        });
        
        GuiHandler.registerGuiHandler("structureoverview", new LittleTileGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, IParentTileList list, LittleTile tile) {
                if (list instanceof StructureTileList)
                    return new SubGuiStructureOverview((StructureTileList) list);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, IParentTileList list, LittleTile tile) {
                if (list instanceof StructureTileList)
                    return new SubContainerStructureOverview(player, (StructureTileList) list);
                return null;
            }
        });
        
        GuiHandler.registerGuiHandler("structureoverview2", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubGuiStructureOverview(structure.mainBlock);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                return new SubContainerStructureOverview(player, structure.mainBlock);
            }
        });
        
        GuiHandler.registerGuiHandler("grabber", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                return ItemLittleGrabber.getMode(stack).getGui(player, stack, ((ILittlePlacer) stack.getItem()).getPositionContext(stack));
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                return ItemLittleGrabber.getMode(stack).getContainer(player, stack);
            }
        });
        
        GuiHandler.registerGuiHandler("recipeadvanced", new CustomGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
                ItemStack stack = player.getHeldItemMainhand();
                if (!((ItemLittleRecipeAdvanced) stack.getItem()).hasLittlePreview(stack))
                    return new SubGuiRecipeAdvancedSelection(stack);
                return new SubGuiRecipe(stack);
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
                return new SubContainerRecipeAdvanced(player, player.getHeldItemMainhand());
            }
        });
        
        GuiHandler.registerGuiHandler("structure_builder", new LittleStructureGuiHandler() {
            
            @Override
            @SideOnly(Side.CLIENT)
            public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleStructureBuilder)
                    return new SubGuiBuilder((LittleStructureBuilder) structure);
                return null;
            }
            
            @Override
            public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleStructure structure) {
                if (structure instanceof LittleStructureBuilder)
                    return new SubContainerBuilder(player, (LittleStructureBuilder) structure);
                return null;
            }
        });
        
        CreativeCorePacket.registerPacket(LittleBlockPacket.class);
        CreativeCorePacket.registerPacket(LittleBlocksUpdatePacket.class);
        CreativeCorePacket.registerPacket(LittleRotatePacket.class);
        CreativeCorePacket.registerPacket(LittleMirrorPacket.class);
        CreativeCorePacket.registerPacket(LittleNeighborUpdatePacket.class);
        CreativeCorePacket.registerPacket(LittleActivateDoorPacket.class);
        CreativeCorePacket.registerPacket(LittleEntityRequestPacket.class);
        CreativeCorePacket.registerPacket(LittleBedPacket.class);
        CreativeCorePacket.registerPacket(LittleVanillaBlockPacket.class);
        CreativeCorePacket.registerPacket(LittleSelectionModePacket.class);
        CreativeCorePacket.registerPacket(LittleBlockUpdatePacket.class);
        CreativeCorePacket.registerPacket(LittleResetAnimationPacket.class);
        CreativeCorePacket.registerPacket(LittlePlacedAnimationPacket.class);
        CreativeCorePacket.registerPacket(LittleActionMessagePacket.class);
        CreativeCorePacket.registerPacket(LittleUpdateStructurePacket.class);
        CreativeCorePacket.registerPacket(LittleEntityFixControllerPacket.class);
        CreativeCorePacket.registerPacket(LittleScrewdriverSelectionPacket.class);
        CreativeCorePacket.registerPacket(LittleUpdateOutputPacket.class);
        CreativeCorePacket.registerPacket(LittleConsumeRightClickEvent.class);
        
        LittleActionRegistry.register(LittleActions.class, LittleActions::new);
        
        LittleAction.registerLittleAction("act", LittleActionActivated.class);
        LittleAction.registerLittleAction("col", LittleActionColorBoxes.class, LittleActionColorBoxesFiltered.class);
        LittleAction.registerLittleAction("deB", LittleActionDestroyBoxes.class, LittleActionDestroyBoxesFiltered.class);
        LittleAction.registerLittleAction("des", LittleActionDestroy.class);
        LittleAction.registerLittleAction("plR", LittleActionPlaceStack.class);
        LittleAction.registerLittleAction("plA", LittleActionPlaceAbsolute.class, LittleActionPlaceAbsolutePremade.class);
        
        LittleAction.registerLittleAction("saw", LittleActionSaw.class, LittleActionSawRevert.class);
        
        LittleAction.registerLittleAction("rep", LittleActionReplace.class);
        
        MinecraftForge.EVENT_BUS.register(new LittleEventHandler());
        MinecraftForge.EVENT_BUS.register(WorldAnimationHandler.class);
        // MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        // Entity
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "sizeTNT"), EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "sit"), EntitySit.class, "sit", 1, this, 250, 250, true);
        EntityRegistry.registerModEntity(new ResourceLocation(MODID, "animation"), EntityAnimation.class, "animation", 2, this, 2000, 250, true);
        
        LittleTilesServer.NEIGHBOR = new NeighborUpdateOrganizer();
        
        proxy.loadSidePost();
        
        if (Loader.isModLoaded("warpdrive"))
            TileEntityLittleTilesTransformer.init();
        
        TheOneProbeManager.init();
        
        if (Loader.isModLoaded("albedo"))
            MinecraftForge.EVENT_BUS.register(AlbedoExtension.class);
        
        MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        MinecraftForge.EVENT_BUS.register(EntitySizeHandler.class);
    }
    
    private void client(final FMLClientSetupEvent event) {
        LittleTilesClient.setup();
    }
    
    @SubscribeEvent
    public static void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().registerAll(BE_TILES_TYPE, BE_TILES_TYPE_TICKING, BE_TILES_TYPE_RENDERED, BE_TILES_TYPE_TICKING_RENDERED, BE_SIGNALCONVERTER_TYPE);
    }
    
    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        event.getRegistry().registerAll(SIZED_TNT_TYPE, SIT_TYPE);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry()
                .registerAll(new Block[] { CLEAN, FLOOR, GRAINY_BIG, GRAINY, GRAINY_LOW, BRICK, BRICK_BIG, BORDERED, CHISELED, BROKEN_BRICK_BIG, CLAY, STRIPS, GRAVEL, SAND, STONE, CORK, WATER, WHITE_WATER, LAVA, WHITE_LAVA, blockTile, blockTileTicking, blockTileRendered, blockTileTickingRendered, storageBlock, flowingWater, whiteFlowingWater, flowingLava, whiteFlowingLava, singleCable, inputArrow, outputArrow, signalConverter });
    }
    
    private static Item createItem(Block block) {
        return new BlockItem(block, new Item.Properties().tab(littleTab)).setRegistryName(block.getRegistryName());
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry()
                .registerAll(hammer, recipe, recipeAdvanced, saw, container, wrench, screwdriver, chisel, colorTube, rubberMallet, multiTiles, utilityKnife, grabber, premade, blockIngredient, blackColorIngredient, cyanColorIngredient, magentaColorIngredient, yellowColorIngredient, createItem(CLEAN), createItem(FLOOR), createItem(GRAINY_BIG), createItem(GRAINY), createItem(GRAINY_LOW), createItem(BRICK), createItem(BRICK_BIG), createItem(BORDERED), createItem(CHISELED), createItem(BROKEN_BRICK_BIG), createItem(CLAY), createItem(STRIPS), createItem(GRAVEL), createItem(SAND), createItem(STONE), createItem(CORK), createItem(WATER), createItem(storageBlock), createItem(signalConverter));
    }
    
    private void serverStarting(final FMLServerStartingEvent event) {
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
            
            for (BlockEntity be : (Set<BlockEntity>) loadedBlockEntities.get(level))
                if (be instanceof BETiles)
                    blocks.add((BETiles) be);
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
            GuiHandler.openGui("lt-export", new NBTTagCompound(), (EntityPlayer) sender.getCommandSenderEntity());
            return 0;
        }));
        
        event.getServer().getCommands().getDispatcher().register(Commands.literal("lt-import").executes((x) -> {
            GuiHandler.openGui("lt-import", new NBTTagCompound(), (EntityPlayer) sender.getCommandSenderEntity());
            return 0;
        }));
        
        event.registerServerCommand(new OpenCommand());
    }
    
}
