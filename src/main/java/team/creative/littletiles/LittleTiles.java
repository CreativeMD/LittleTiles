package team.creative.littletiles;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.creativemd.littletiles.CustomGuiHandler;
import com.creativemd.littletiles.EntityPlayer;
import com.creativemd.littletiles.IParentTileList;
import com.creativemd.littletiles.ItemBlock;
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
import com.creativemd.littletiles.common.block.BlockArrow;
import com.creativemd.littletiles.common.block.BlockCable;
import com.creativemd.littletiles.common.block.BlockLTFlowingLava;
import com.creativemd.littletiles.common.block.BlockLTFlowingWater;
import com.creativemd.littletiles.common.block.BlockLittleDyeable;
import com.creativemd.littletiles.common.block.BlockLittleDyeable2;
import com.creativemd.littletiles.common.block.BlockLittleDyeableTransparent;
import com.creativemd.littletiles.common.block.BlockSignalConverter;
import com.creativemd.littletiles.common.block.BlockStorageTile;
import com.creativemd.littletiles.common.block.ItemBlockColored;
import com.creativemd.littletiles.common.block.ItemBlockColored2;
import com.creativemd.littletiles.common.block.ItemBlockFlowingLava;
import com.creativemd.littletiles.common.block.ItemBlockFlowingWater;
import com.creativemd.littletiles.common.block.ItemBlockTransparentColored;
import com.creativemd.littletiles.common.command.ExportCommand;
import com.creativemd.littletiles.common.command.ImportCommand;
import com.creativemd.littletiles.common.command.OpenCommand;
import com.creativemd.littletiles.common.command.ToVanillaCommand;
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
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySit;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.mod.albedo.AlbedoExtension;
import com.creativemd.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import com.creativemd.littletiles.common.mod.warpdrive.TileEntityLittleTilesTransformer;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.structure.type.premade.LittleBlankOMatic;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder;
import com.creativemd.littletiles.common.util.converation.ChiselAndBitsConveration;
import com.creativemd.littletiles.common.util.ingredient.rules.IngredientRules;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlserverevents.FMLServerStartingEvent;
import team.creative.creativecore.common.network.CreativeNetwork;
import team.creative.littletiles.client.LittleTilesClient;
import team.creative.littletiles.common.action.LittleAction;
import team.creative.littletiles.common.action.LittleActionRegistry;
import team.creative.littletiles.common.action.LittleActions;
import team.creative.littletiles.common.api.tool.ILittlePlacer;
import team.creative.littletiles.common.api.tool.ILittleTool;
import team.creative.littletiles.common.block.BlockTile;
import team.creative.littletiles.common.block.entity.BESignalConverter;
import team.creative.littletiles.common.block.entity.BETiles;
import team.creative.littletiles.common.config.LittleTilesConfig;
import team.creative.littletiles.common.item.ItemBlockIngredient;
import team.creative.littletiles.common.item.ItemBlockTiles;
import team.creative.littletiles.common.item.ItemColorIngredient;
import team.creative.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import team.creative.littletiles.common.item.ItemLittleBag;
import team.creative.littletiles.common.item.ItemLittleChisel;
import team.creative.littletiles.common.item.ItemLittleGrabber;
import team.creative.littletiles.common.item.ItemLittleHammer;
import team.creative.littletiles.common.item.ItemLittlePaintBrush;
import team.creative.littletiles.common.item.ItemLittleRecipe;
import team.creative.littletiles.common.item.ItemLittleRecipeAdvanced;
import team.creative.littletiles.common.item.ItemLittleRubberMallet;
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
    
    public static BlockLittleDyeable dyeableBlock = (BlockLittleDyeable) new BlockLittleDyeable().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock")
            .setHardness(1.5F);
    public static BlockLittleDyeable2 dyeableBlock2 = (BlockLittleDyeable2) new BlockLittleDyeable2().setRegistryName("LTColoredBlock2").setUnlocalizedName("LTColoredBlock2")
            .setHardness(1.5F);
    public static Block dyeableBlockTransparent = new BlockLittleDyeableTransparent().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock")
            .setHardness(0.3F);
    public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile").setHardness(1.5F);
    
    public static Block flowingWater = new BlockLTFlowingWater(BlockLittleDyeableTransparent.LittleDyeableTransparent.WATER).setRegistryName("LTFlowingWater")
            .setUnlocalizedName("LTFlowingWater").setHardness(0.3F);
    public static Block whiteFlowingWater = new BlockLTFlowingWater(BlockLittleDyeableTransparent.LittleDyeableTransparent.WHITE_WATER).setRegistryName("LTWhiteFlowingWater")
            .setUnlocalizedName("LTWhiteFlowingWater").setHardness(0.3F);
    
    public static Block flowingLava = new BlockLTFlowingLava(BlockLittleDyeable.LittleDyeableType.LAVA).setRegistryName("LTFlowingLava").setUnlocalizedName("LTFlowingLava")
            .setHardness(0.3F);
    public static Block whiteFlowingLava = new BlockLTFlowingLava(BlockLittleDyeable.LittleDyeableType.WHITE_LAVA).setRegistryName("LTWhiteFlowingLava")
            .setUnlocalizedName("LTWhiteFlowingLava").setHardness(0.3F);
    
    public static Block singleCable = new BlockCable().setRegistryName("ltsinglecable").setUnlocalizedName("ltsinglecable").setHardness(1.5F);
    
    public static Block inputArrow = new BlockArrow().setRegistryName("ltinput").setUnlocalizedName("ltinput").setHardness(1.5F);
    public static Block outputArrow = new BlockArrow().setRegistryName("ltoutput").setUnlocalizedName("ltoutput").setHardness(1.5F);
    
    public static Block signalConverter = new BlockSignalConverter().setRegistryName("signal_converter").setUnlocalizedName("signal_converter").setHardness(1.5F);
    
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
    
    public static CreativeModeTab littleTab = new CreativeModeTab("littletiles") {
        
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(hammer);
        }
    };
    
    public LittleTiles() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client);
        MinecraftForge.EVENT_BUS.addListener(this::server);
    }
    
    private void init(final FMLCommonSetupEvent event) {
        blockTile = new BlockTile(Material.STONE, false, false).setRegistryName("tiles");
        blockTileTicking = new BlockTile(Material.STONE, true, false).setRegistryName("tiles_ticking");
        blockTileRendered = new BlockTile(Material.STONE, false, true).setRegistryName("tiles_rendered");
        blockTileTickingRendered = new BlockTile(Material.STONE, true, true).setRegistryName("tiles_ticking_rendered");
        
        hammer = new ItemLittleHammer().setUnlocalizedName("LTHammer").setRegistryName("hammer");
        recipe = new ItemLittleRecipe().setUnlocalizedName("LTRecipe").setRegistryName("recipe");
        recipeAdvanced = new ItemLittleRecipeAdvanced().setUnlocalizedName("LTRecipeAdvanced").setRegistryName("recipeadvanced");
        multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles").setRegistryName("multiTiles");
        saw = new ItemLittleSaw().setUnlocalizedName("LTSaw").setRegistryName("saw");
        container = new ItemLittleBag().setUnlocalizedName("LTContainer").setRegistryName("container");
        wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench").setRegistryName("wrench");
        screwdriver = new ItemLittleScrewdriver().setUnlocalizedName("LTScrewdriver").setRegistryName("screwdriver");
        chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel").setRegistryName("chisel");
        colorTube = new ItemLittlePaintBrush().setUnlocalizedName("LTColorTube").setRegistryName("colorTube");
        rubberMallet = new ItemLittleRubberMallet().setUnlocalizedName("LTRubberMallet").setRegistryName("rubberMallet");
        utilityKnife = new ItemLittleUtilityKnife().setUnlocalizedName("LTUtilityKnife").setRegistryName("utilityKnife");
        grabber = new ItemLittleGrabber().setUnlocalizedName("LTGrabber").setRegistryName("grabber");
        premade = new ItemPremadeStructure().setUnlocalizedName("LTPremade").setRegistryName("premade");
        
        blockIngredient = new ItemBlockIngredient().setUnlocalizedName("LTBlockIngredient").setRegistryName("blockingredient");
        
        blackColorIngredient = new ItemColorIngredient(ColorIngredientType.black).setUnlocalizedName("LTColorBottleBlack").setRegistryName("bottle_black");
        cyanColorIngredient = new ItemColorIngredient(ColorIngredientType.cyan).setUnlocalizedName("LTColorBottleCyan").setRegistryName("bottle_cyan");
        magentaColorIngredient = new ItemColorIngredient(ColorIngredientType.magenta).setUnlocalizedName("LTColorBottleMagenta").setRegistryName("bottle_magenta");
        yellowColorIngredient = new ItemColorIngredient(ColorIngredientType.yellow).setUnlocalizedName("LTColorBottleYellow").setRegistryName("bottle_yellow");
        
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
        LittlePacketTypes.init();
        
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> LittleTilesClient::init);
        
        ForgeModContainer.fullBoundingBoxLadders = true;
        
        BE_TILES_TYPE = BlockEntityType.Builder.of(BETiles::new, blockTile).build(null).setRegistryName(MODID, "tiles");
        BE_TILES_TYPE_RENDERED = BlockEntityType.Builder.of(BETiles::new, blockTileRendered).build(null).setRegistryName(MODID, "tiles_rendered");
        BE_TILES_TYPE_TICKING = BlockEntityType.Builder.of(BETiles::new, blockTileTicking).build(null).setRegistryName(MODID, "tiles_ticking");
        BE_TILES_TYPE_TICKING_RENDERED = BlockEntityType.Builder.of(BETiles::new, blockTileTickingRendered).build(null).setRegistryName(MODID, "tiles_ticking_rendered");
        BE_SIGNALCONVERTER_TYPE = BlockEntityType.Builder.of(BESignalConverter::new, signalConverter).build(null).setRegistryName(MODID, "converter");
        
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
        EntityRegistry.registerModEntity(new ResourceLocation(modid, "sizeTNT"), EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid, "sit"), EntitySit.class, "sit", 1, this, 250, 250, true);
        EntityRegistry.registerModEntity(new ResourceLocation(modid, "animation"), EntityAnimation.class, "animation", 2, this, 2000, 250, true);
        
        LittleTilesServer.NEIGHBOR = new NeighborUpdateOrganizer();
        
        proxy.loadSidePost();
        
        if (Loader.isModLoaded("warpdrive"))
            TileEntityLittleTilesTransformer.init();
        
        TheOneProbeManager.init();
        
        if (Loader.isModLoaded("albedo"))
            MinecraftForge.EVENT_BUS.register(AlbedoExtension.class);
        
        MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
    }
    
    private void client(final FMLClientSetupEvent event) {
        LittleTilesClient.setup();
    }
    
    @SubscribeEvent
    public static void registerBlockEntities(RegistryEvent.Register<BlockEntityType<?>> event) {
        event.getRegistry().registerAll(BE_TILES_TYPE, BE_SIGNALCONVERTER_TYPE);
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        
        event.getRegistry()
                .registerAll(new Block[] { dyeableBlock, dyeableBlock2, dyeableBlockTransparent, blockTileNoTicking, blockTileTicking, blockTileNoTickingRendered, blockTileTickingRendered, storageBlock, flowingWater, whiteFlowingWater, flowingLava, whiteFlowingLava, singleCable, inputArrow, outputArrow, signalConverter });
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry()
                .registerAll(new Item[] { hammer, recipe, recipeAdvanced, saw, container, wrench, screwdriver, chisel, colorTube, rubberMallet, multiTiles, utilityKnife, grabber, premade, blockIngredient, blackColorIngredient, cyanColorIngredient, magentaColorIngredient, yellowColorIngredient, new ItemBlock(storageBlock)
                        .setRegistryName(storageBlock.getRegistryName()), new ItemBlockColored(dyeableBlock, dyeableBlock.getRegistryName())
                                .setRegistryName(dyeableBlock.getRegistryName()), new ItemBlockColored2(dyeableBlock2, dyeableBlock2.getRegistryName())
                                        .setRegistryName(dyeableBlock2.getRegistryName()), new ItemBlockTransparentColored(dyeableBlockTransparent, dyeableBlockTransparent
                                                .getRegistryName()).setRegistryName(dyeableBlockTransparent
                                                        .getRegistryName()), new ItemBlockTiles(blockTileNoTicking, blockTileNoTicking.getRegistryName())
                                                                .setRegistryName(blockTileNoTicking.getRegistryName()), new ItemBlockTiles(blockTileTicking, blockTileTicking
                                                                        .getRegistryName()).setRegistryName(blockTileTicking
                                                                                .getRegistryName()), new ItemBlockTiles(blockTileNoTickingRendered, blockTileNoTickingRendered
                                                                                        .getRegistryName()).setRegistryName(blockTileNoTickingRendered
                                                                                                .getRegistryName()), new ItemBlockTiles(blockTileTickingRendered, blockTileTickingRendered
                                                                                                        .getRegistryName()).setRegistryName(blockTileTickingRendered
                                                                                                                .getRegistryName()), new ItemBlockFlowingWater(flowingWater, flowingWater
                                                                                                                        .getRegistryName()).setRegistryName(flowingWater
                                                                                                                                .getRegistryName()), new ItemBlockFlowingWater(whiteFlowingWater, whiteFlowingWater
                                                                                                                                        .getRegistryName())
                                                                                                                                                .setRegistryName(whiteFlowingWater
                                                                                                                                                        .getRegistryName()), new ItemBlockFlowingLava(flowingLava, flowingLava
                                                                                                                                                                .getRegistryName())
                                                                                                                                                                        .setRegistryName(flowingLava
                                                                                                                                                                                .getRegistryName()), new ItemBlockFlowingLava(whiteFlowingLava, whiteFlowingLava
                                                                                                                                                                                        .getRegistryName())
                                                                                                                                                                                                .setRegistryName(whiteFlowingLava
                                                                                                                                                                                                        .getRegistryName()), new ItemBlock(singleCable)
                                                                                                                                                                                                                .setRegistryName(singleCable
                                                                                                                                                                                                                        .getRegistryName()), new ItemBlock(inputArrow)
                                                                                                                                                                                                                                .setRegistryName(inputArrow
                                                                                                                                                                                                                                        .getRegistryName()), new ItemBlock(outputArrow)
                                                                                                                                                                                                                                                .setRegistryName(outputArrow
                                                                                                                                                                                                                                                        .getRegistryName()), new ItemBlock(signalConverter)
                                                                                                                                                                                                                                                                .setRegistryName(signalConverter
                                                                                                                                                                                                                                                                        .getRegistryName()) });
        
        proxy.loadSide();
    }
    
    public void server(FMLServerStartingEvent event) {
        event.registerServerCommand(new ExportCommand());
        event.registerServerCommand(new ImportCommand());
        event.registerServerCommand(new OpenCommand());
        event.registerServerCommand(new ToVanillaCommand());
    }
    
}
