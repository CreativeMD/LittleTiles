package com.creativemd.littletiles;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.config.holder.CreativeConfigRegistry;
import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.gui.*;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.client.gui.handler.LittleTileGuiHandler;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.block.*;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes.LittleActionColorBoxesFiltered;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes.LittleActionDestroyBoxesFiltered;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute.LittleActionPlaceAbsolutePremade;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw.LittleActionSawRevert;
import com.creativemd.littletiles.common.api.ILittlePlacer;
import com.creativemd.littletiles.common.api.ILittleTool;
import com.creativemd.littletiles.common.block.*;
import com.creativemd.littletiles.common.command.*;
import com.creativemd.littletiles.common.container.*;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySit;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.item.*;
import com.creativemd.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import com.creativemd.littletiles.common.mod.albedo.AlbedoExtension;
import com.creativemd.littletiles.common.mod.lux.LuxExtension;
import com.creativemd.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import com.creativemd.littletiles.common.mod.warpdrive.TileEntityLittleTilesTransformer;
import com.creativemd.littletiles.common.packet.*;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.structure.type.premade.LittleBlankOMatic;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter;
import com.creativemd.littletiles.common.structure.type.premade.LittleStructureBuilder;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.parent.IParentTileList;
import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.*;
import com.creativemd.littletiles.common.util.converation.ChiselAndBitsConveration;
import com.creativemd.littletiles.common.util.ingredient.rules.IngredientRules;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.creativemd.littletiles.server.NeighborUpdateOrganizer;
import com.creativemd.littletiles.server.interact.LittleInteractionHandlerServer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.UUID;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles",
    guiFactory = "com.creativemd.littletiles.client.LittleTilesSettings", dependencies = "required-after:creativecore")
@Mod.EventBusSubscriber
public class LittleTiles {
    
    @SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
    public static LittleTilesServer proxy;
    
    public static final String modid = Tags.ID;
    public static final String version = Tags.VERSION;
    
    public static LittleTilesConfig CONFIG;
    
    public static CreativeTabs littleTab = new CreativeTabs("littletiles") {

        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(hammer);
        }
    };
    
    public static Block blockTileNoTicking;
    public static Block blockTileTicking;
    public static Block blockTileNoTickingRendered;
    public static Block blockTileTickingRendered;
    
    public static BlockLittleDyeable dyeableBlock = (BlockLittleDyeable) new BlockLittleDyeable().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock").setHardness(1.5F);
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
    
    private void removeMissingProperties(String path, ConfigCategory category, List<String> allowedNames) {
        for (ConfigCategory child : category.getChildren())
            removeMissingProperties(path + (path.isEmpty() ? "" : ".") + category.getName(), child, allowedNames);
        for (String propertyName : category.getPropertyOrder()) {
            String name = path + (path.isEmpty() ? "" : ".") + propertyName;
            if (!allowedNames.contains(name))
                category.remove(propertyName);
        }
    }
    
    @EventHandler
    public void PreInit(FMLPreInitializationEvent event) {
        event.getModMetadata().version = version;
        
        CreativeConfigRegistry.ROOT.registerValue(modid, CONFIG = new LittleTilesConfig());
        
        if (!CreativeCore.configHandler.modFileExist(modid, Side.SERVER) && event.getSuggestedConfigurationFile().exists()) {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            config.load();
            CONFIG.core.minSize = config
                .getInt("minSize", "core", 1, 1, Integer.MAX_VALUE, "The minimum grid size possible. ATTENTION! This needs be equal for every client & server. Backup your world.");
            CONFIG.core.defaultSize = config
                .getInt("defaultSize", "core", 16, 1, Integer.MAX_VALUE, "Needs to be part of the row. ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!");
            CONFIG.core.scale = config
                .getInt("scale", "core", 6, 1, Integer.MAX_VALUE, "How many grids there are. ATTENTION! This needs be equal for every client & server. Make sure that it is enough for the defaultSize to exist.");
            CONFIG.core.exponent = config
                .getInt("exponent", "core", 2, 1, Integer.MAX_VALUE, "minSize ^ (exponent * scale). ATTENTION! This needs be equal for every client & server. Default is two -> (1, 2, 4, 8, 16, 32 etc.).");
            config.save();
            CreativeCore.configHandler.save(modid, Side.SERVER);
        }
        proxy.loadSidePre();
        
        blockTileNoTicking = new BlockTile(Material.ROCK, false, false).setRegistryName("BlockLittleTiles");
        blockTileTicking = new BlockTile(Material.ROCK, true, false).setRegistryName("BlockLittleTilesTicking");
        blockTileNoTickingRendered = new BlockTile(Material.ROCK, false, true).setRegistryName("BlockLittleTilesRendered");
        blockTileTickingRendered = new BlockTile(Material.ROCK, true, true).setRegistryName("BlockLittleTilesTickingRendered");
        
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
        
        LittleTileRegistry.initTiles();
        IngredientRules.loadRules();
        LittleStructureRegistry.initStructures();
    }
    
    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry()
            .registerAll(new Block[] { dyeableBlock, dyeableBlock2, dyeableBlockTransparent, blockTileNoTicking, blockTileTicking, blockTileNoTickingRendered, blockTileTickingRendered,
                    storageBlock, flowingWater, whiteFlowingWater, flowingLava, whiteFlowingLava, singleCable, inputArrow, outputArrow, signalConverter });
    }
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(new Item[] { hammer, recipe, recipeAdvanced, saw, container, wrench, screwdriver, chisel, colorTube, rubberMallet, multiTiles, utilityKnife, grabber,
                premade, blockIngredient, blackColorIngredient, cyanColorIngredient, magentaColorIngredient, yellowColorIngredient,
                new ItemBlock(storageBlock).setRegistryName(storageBlock.getRegistryName()),
                new ItemBlockColored(dyeableBlock, dyeableBlock.getRegistryName()).setRegistryName(dyeableBlock.getRegistryName()),
                new ItemBlockColored2(dyeableBlock2, dyeableBlock2.getRegistryName()).setRegistryName(dyeableBlock2.getRegistryName()),
                new ItemBlockTransparentColored(dyeableBlockTransparent, dyeableBlockTransparent.getRegistryName()).setRegistryName(dyeableBlockTransparent.getRegistryName()),
                new ItemBlockTiles(blockTileNoTicking, blockTileNoTicking.getRegistryName()).setRegistryName(blockTileNoTicking.getRegistryName()),
                new ItemBlockTiles(blockTileTicking, blockTileTicking.getRegistryName()).setRegistryName(blockTileTicking.getRegistryName()),
                new ItemBlockTiles(blockTileNoTickingRendered, blockTileNoTickingRendered.getRegistryName()).setRegistryName(blockTileNoTickingRendered.getRegistryName()),
                new ItemBlockTiles(blockTileTickingRendered, blockTileTickingRendered.getRegistryName()).setRegistryName(blockTileTickingRendered.getRegistryName()),
                new ItemBlockFlowingWater(flowingWater, flowingWater.getRegistryName()).setRegistryName(flowingWater.getRegistryName()),
                new ItemBlockFlowingWater(whiteFlowingWater, whiteFlowingWater.getRegistryName()).setRegistryName(whiteFlowingWater.getRegistryName()),
                new ItemBlockFlowingLava(flowingLava, flowingLava.getRegistryName()).setRegistryName(flowingLava.getRegistryName()),
                new ItemBlockFlowingLava(whiteFlowingLava, whiteFlowingLava.getRegistryName()).setRegistryName(whiteFlowingLava.getRegistryName()),
                new ItemBlock(singleCable).setRegistryName(singleCable.getRegistryName()), new ItemBlock(inputArrow).setRegistryName(inputArrow.getRegistryName()),
                new ItemBlock(outputArrow).setRegistryName(outputArrow.getRegistryName()), new ItemBlock(signalConverter).setRegistryName(signalConverter.getRegistryName()) });
        
        proxy.loadSide();
    }
    
    @EventHandler
    public void Init(FMLInitializationEvent event) {
        ForgeModContainer.fullBoundingBoxLadders = true;
        
        GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
        GameRegistry.registerTileEntity(TileEntityLittleTilesTicking.class, "LittleTilesTileEntityTicking");
        GameRegistry.registerTileEntity(TileEntityLittleTilesRendered.class, "LittleTilesTileEntityRendered");
        GameRegistry.registerTileEntity(TileEntityLittleTilesTickingRendered.class, "LittleTilesTileEntityTickingRendered");
        GameRegistry.registerTileEntity(TESignalConverter.class, new ResourceLocation(modid, "signal_converter"));
        
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
        CreativeCorePacket.registerPacket(LittleFlipPacket.class);
        CreativeCorePacket.registerPacket(LittleNeighborUpdatePacket.class);
        CreativeCorePacket.registerPacket(LittleActivateDoorPacket.class);
        CreativeCorePacket.registerPacket(LittleBedPacket.class);
        CreativeCorePacket.registerPacket(LittleVanillaBlockPacket.class);
        CreativeCorePacket.registerPacket(LittleSelectionModePacket.class);
        CreativeCorePacket.registerPacket(LittleBlockUpdatePacket.class);
        CreativeCorePacket.registerPacket(LittleActionMessagePacket.class);
        CreativeCorePacket.registerPacket(LittleUpdateStructurePacket.class);
        CreativeCorePacket.registerPacket(LittleScrewdriverSelectionPacket.class);
        CreativeCorePacket.registerPacket(LittleUpdateOutputPacket.class);
        CreativeCorePacket.registerPacket(LittleInteractionPacket.class);
        
        CreativeCorePacket.registerPacket(LittleAnimationControllerPacket.class);
        CreativeCorePacket.registerPacket(LittleAnimationDestroyPacket.class);
        CreativeCorePacket.registerPacket(LittleAnimationDataPacket.class);
        
        LittleAction.registerLittleAction("com", LittleActionCombined.class);
        
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
        
        proxy.loadSidePost();
        
        if (Loader.isModLoaded("warpdrive"))
            TileEntityLittleTilesTransformer.init();
        
        TheOneProbeManager.init();
        
        if (Loader.isModLoaded("albedo"))
            MinecraftForge.EVENT_BUS.register(AlbedoExtension.class);
        
        if (Loader.isModLoaded("lux"))
            MinecraftForge.EVENT_BUS.register(LuxExtension.class);
        
        MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
        
        LittleTilesServer.NEIGHBOR = new NeighborUpdateOrganizer();
        LittleTilesServer.INTERACTION = new LittleInteractionHandlerServer();
        
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new ExportCommand());
        event.registerServerCommand(new ImportCommand());
        event.registerServerCommand(new OpenCommand());
        event.registerServerCommand(new ToVanillaCommand());
        event.registerServerCommand(new AllBlocksCommand());
    }
}
