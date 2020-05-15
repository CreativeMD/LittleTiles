package com.creativemd.littletiles;

import java.util.List;
import java.util.UUID;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.config.holder.CreativeConfigRegistry;
import com.creativemd.creativecore.common.gui.container.SubContainer;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.littletiles.client.gui.SubGuiDiagnose;
import com.creativemd.littletiles.client.gui.SubGuiExport;
import com.creativemd.littletiles.client.gui.SubGuiImport;
import com.creativemd.littletiles.client.gui.SubGuiParticle;
import com.creativemd.littletiles.client.gui.SubGuiRecipe;
import com.creativemd.littletiles.client.gui.SubGuiRecipeAdvancedSelection;
import com.creativemd.littletiles.client.gui.SubGuiStorage;
import com.creativemd.littletiles.client.gui.SubGuiStructureOverview;
import com.creativemd.littletiles.client.gui.SubGuiWorkbench;
import com.creativemd.littletiles.client.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
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
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone.LittleActionGlowstoneRevert;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw.LittleActionSawRevert;
import com.creativemd.littletiles.common.api.IBoxSelector;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.block.BlockArrow;
import com.creativemd.littletiles.common.block.BlockCable;
import com.creativemd.littletiles.common.block.BlockLTColored;
import com.creativemd.littletiles.common.block.BlockLTColored2;
import com.creativemd.littletiles.common.block.BlockLTFlowingLava;
import com.creativemd.littletiles.common.block.BlockLTFlowingWater;
import com.creativemd.littletiles.common.block.BlockLTParticle;
import com.creativemd.littletiles.common.block.BlockLTTransparentColored;
import com.creativemd.littletiles.common.block.BlockStorageTile;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.block.ItemBlockColored;
import com.creativemd.littletiles.common.block.ItemBlockColored2;
import com.creativemd.littletiles.common.block.ItemBlockFlowingLava;
import com.creativemd.littletiles.common.block.ItemBlockFlowingWater;
import com.creativemd.littletiles.common.block.ItemBlockTransparentColored;
import com.creativemd.littletiles.common.command.ExportCommand;
import com.creativemd.littletiles.common.command.ImportCommand;
import com.creativemd.littletiles.common.command.OpenCommand;
import com.creativemd.littletiles.common.command.ToVanillaCommand;
import com.creativemd.littletiles.common.container.SubContainerDiagnose;
import com.creativemd.littletiles.common.container.SubContainerExport;
import com.creativemd.littletiles.common.container.SubContainerImport;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.container.SubContainerRecipeAdvanced;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.container.SubContainerStructureOverview;
import com.creativemd.littletiles.common.container.SubContainerWorkbench;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.entity.old.EntityOldDoorAnimation;
import com.creativemd.littletiles.common.event.LittleEventHandler;
import com.creativemd.littletiles.common.item.ItemBag;
import com.creativemd.littletiles.common.item.ItemBlockIngredient;
import com.creativemd.littletiles.common.item.ItemBlockTiles;
import com.creativemd.littletiles.common.item.ItemColorIngredient;
import com.creativemd.littletiles.common.item.ItemColorIngredient.ColorIngredientType;
import com.creativemd.littletiles.common.item.ItemColorTube;
import com.creativemd.littletiles.common.item.ItemHammer;
import com.creativemd.littletiles.common.item.ItemLittleChisel;
import com.creativemd.littletiles.common.item.ItemLittleGrabber;
import com.creativemd.littletiles.common.item.ItemLittleSaw;
import com.creativemd.littletiles.common.item.ItemLittleScrewdriver;
import com.creativemd.littletiles.common.item.ItemLittleWrench;
import com.creativemd.littletiles.common.item.ItemMultiTiles;
import com.creativemd.littletiles.common.item.ItemPremadeStructure;
import com.creativemd.littletiles.common.item.ItemRecipe;
import com.creativemd.littletiles.common.item.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.item.ItemRubberMallet;
import com.creativemd.littletiles.common.item.ItemUtilityKnife;
import com.creativemd.littletiles.common.mod.albedo.AlbedoExtension;
import com.creativemd.littletiles.common.mod.theoneprobe.TheOneProbeManager;
import com.creativemd.littletiles.common.mod.warpdrive.TileEntityLittleTilesTransformer;
import com.creativemd.littletiles.common.packet.LittleActionMessagePacket;
import com.creativemd.littletiles.common.packet.LittleActivateDoorPacket;
import com.creativemd.littletiles.common.packet.LittleBedPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleBlocksUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleNeighborUpdatePacket;
import com.creativemd.littletiles.common.packet.LittlePlacedAnimationPacket;
import com.creativemd.littletiles.common.packet.LittleResetAnimationPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.packet.LittleSelectionModePacket;
import com.creativemd.littletiles.common.packet.LittleTileUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.structure.registry.LittleStructureRegistry;
import com.creativemd.littletiles.common.structure.type.LittleStorage;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileParticle;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTicking;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTickingRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.util.converation.ChiselAndBitsConveration;
import com.creativemd.littletiles.common.util.ingredient.rules.IngredientRules;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.world.WorldAnimationHandler;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.creativemd.littletiles.server.NeighborUpdateOrganizer;

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

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles", acceptedMinecraftVersions = "", guiFactory = "com.creativemd.littletiles.client.LittleTilesSettings")
@Mod.EventBusSubscriber
public class LittleTiles {
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "1.5.0";
	
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
	
	public static Block coloredBlock = new BlockLTColored().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock").setHardness(1.5F);
	public static Block coloredBlock2 = new BlockLTColored2().setRegistryName("LTColoredBlock2").setUnlocalizedName("LTColoredBlock2").setHardness(1.5F);
	public static Block transparentColoredBlock = new BlockLTTransparentColored().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock").setHardness(0.3F);
	public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile").setHardness(1.5F);
	public static Block particleBlock = new BlockLTParticle().setRegistryName("LTParticleBlock").setUnlocalizedName("LTParticleBlock").setHardness(1.5F);
	
	public static Block flowingWater = new BlockLTFlowingWater(BlockLTTransparentColored.EnumType.water).setRegistryName("LTFlowingWater").setUnlocalizedName("LTFlowingWater").setHardness(0.3F);
	public static Block whiteFlowingWater = new BlockLTFlowingWater(BlockLTTransparentColored.EnumType.white_water).setRegistryName("LTWhiteFlowingWater").setUnlocalizedName("LTWhiteFlowingWater").setHardness(0.3F);
	
	public static Block flowingLava = new BlockLTFlowingLava(BlockLTColored.EnumType.lava).setRegistryName("LTFlowingLava").setUnlocalizedName("LTFlowingLava").setHardness(0.3F);
	public static Block whiteFlowingLava = new BlockLTFlowingLava(BlockLTColored.EnumType.white_lava).setRegistryName("LTWhiteFlowingLava").setUnlocalizedName("LTWhiteFlowingLava").setHardness(0.3F);
	
	public static Block singleCable = new BlockCable().setRegistryName("ltsinglecable").setUnlocalizedName("ltsinglecable").setHardness(1.5F);
	
	public static Block inputArrow = new BlockArrow().setRegistryName("ltinput").setUnlocalizedName("ltinput").setHardness(1.5F);
	
	public static Block outputArrow = new BlockArrow().setRegistryName("ltoutput").setUnlocalizedName("ltoutput").setHardness(1.5F);
	
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
			CONFIG.core.minSize = config.getInt("minSize", "core", 1, 1, Integer.MAX_VALUE, "The minimum grid size possible. ATTENTION! This needs be equal for every client & server. Backup your world.");
			CONFIG.core.defaultSize = config.getInt("defaultSize", "core", 16, 1, Integer.MAX_VALUE, "Needs to be part of the row. ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!");
			CONFIG.core.scale = config.getInt("scale", "core", 6, 1, Integer.MAX_VALUE, "How many grids there are. ATTENTION! This needs be equal for every client & server. Make sure that it is enough for the defaultSize to exist.");
			CONFIG.core.exponent = config.getInt("exponent", "core", 2, 1, Integer.MAX_VALUE, "minSize ^ (exponent * scale). ATTENTION! This needs be equal for every client & server. Default is two -> (1, 2, 4, 8, 16, 32 etc.).");
			config.save();
			CreativeCore.configHandler.save(modid, Side.SERVER);
		}
		proxy.loadSidePre();
		
		blockTileNoTicking = new BlockTile(Material.ROCK, false, false).setRegistryName("BlockLittleTiles");
		blockTileTicking = new BlockTile(Material.ROCK, true, false).setRegistryName("BlockLittleTilesTicking");
		blockTileNoTickingRendered = new BlockTile(Material.ROCK, false, true).setRegistryName("BlockLittleTilesRendered");
		blockTileTickingRendered = new BlockTile(Material.ROCK, true, true).setRegistryName("BlockLittleTilesTickingRendered");
		
		hammer = new ItemHammer().setUnlocalizedName("LTHammer").setRegistryName("hammer");
		recipe = new ItemRecipe().setUnlocalizedName("LTRecipe").setRegistryName("recipe");
		recipeAdvanced = new ItemRecipeAdvanced().setUnlocalizedName("LTRecipeAdvanced").setRegistryName("recipeadvanced");
		multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles").setRegistryName("multiTiles");
		saw = new ItemLittleSaw().setUnlocalizedName("LTSaw").setRegistryName("saw");
		container = new ItemBag().setUnlocalizedName("LTContainer").setRegistryName("container");
		wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench").setRegistryName("wrench");
		screwdriver = new ItemLittleScrewdriver().setUnlocalizedName("LTScrewdriver").setRegistryName("screwdriver");
		chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel").setRegistryName("chisel");
		colorTube = new ItemColorTube().setUnlocalizedName("LTColorTube").setRegistryName("colorTube");
		rubberMallet = new ItemRubberMallet().setUnlocalizedName("LTRubberMallet").setRegistryName("rubberMallet");
		utilityKnife = new ItemUtilityKnife().setUnlocalizedName("LTUtilityKnife").setRegistryName("utilityKnife");
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
		event.getRegistry().registerAll(coloredBlock, coloredBlock2, transparentColoredBlock, blockTileNoTicking, blockTileTicking, blockTileNoTickingRendered, blockTileTickingRendered, storageBlock, particleBlock, flowingWater, whiteFlowingWater, flowingLava, whiteFlowingLava, singleCable,
		        inputArrow, outputArrow);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(hammer, recipe, recipeAdvanced, saw, container, wrench, screwdriver, chisel, colorTube, rubberMallet, multiTiles, utilityKnife, grabber, premade, blockIngredient, blackColorIngredient, cyanColorIngredient, magentaColorIngredient, yellowColorIngredient,
		        new ItemBlock(storageBlock).setRegistryName(storageBlock.getRegistryName()), new ItemBlock(particleBlock).setRegistryName(particleBlock.getRegistryName()), new ItemBlockColored(coloredBlock, coloredBlock.getRegistryName()).setRegistryName(coloredBlock.getRegistryName()),
		        new ItemBlockColored2(coloredBlock2, coloredBlock2.getRegistryName()).setRegistryName(coloredBlock2.getRegistryName()), new ItemBlockTransparentColored(transparentColoredBlock, transparentColoredBlock.getRegistryName()).setRegistryName(transparentColoredBlock.getRegistryName()),
		        new ItemBlockTiles(blockTileNoTicking, blockTileNoTicking.getRegistryName()).setRegistryName(blockTileNoTicking.getRegistryName()), new ItemBlockTiles(blockTileTicking, blockTileTicking.getRegistryName()).setRegistryName(blockTileTicking.getRegistryName()),
		        new ItemBlockTiles(blockTileNoTickingRendered, blockTileNoTickingRendered.getRegistryName()).setRegistryName(blockTileNoTickingRendered.getRegistryName()),
		        new ItemBlockTiles(blockTileTickingRendered, blockTileTickingRendered.getRegistryName()).setRegistryName(blockTileTickingRendered.getRegistryName()), new ItemBlockFlowingWater(flowingWater, flowingWater.getRegistryName()).setRegistryName(flowingWater.getRegistryName()),
		        new ItemBlockFlowingWater(whiteFlowingWater, whiteFlowingWater.getRegistryName()).setRegistryName(whiteFlowingWater.getRegistryName()), new ItemBlockFlowingLava(flowingLava, flowingLava.getRegistryName()).setRegistryName(flowingLava.getRegistryName()),
		        new ItemBlockFlowingLava(whiteFlowingLava, whiteFlowingLava.getRegistryName()).setRegistryName(whiteFlowingLava.getRegistryName()), new ItemBlock(singleCable).setRegistryName(singleCable.getRegistryName()), new ItemBlock(inputArrow).setRegistryName(inputArrow.getRegistryName()),
		        new ItemBlock(outputArrow).setRegistryName(outputArrow.getRegistryName()));
		
		proxy.loadSide();
	}
	
	@EventHandler
	public void Init(FMLInitializationEvent event) {
		ForgeModContainer.fullBoundingBoxLadders = true;
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		GameRegistry.registerTileEntity(TileEntityLittleTilesTicking.class, "LittleTilesTileEntityTicking");
		GameRegistry.registerTileEntity(TileEntityLittleTilesRendered.class, "LittleTilesTileEntityRendered");
		GameRegistry.registerTileEntity(TileEntityLittleTilesTickingRendered.class, "LittleTilesTileEntityTickingRendered");
		GameRegistry.registerTileEntity(TileEntityParticle.class, "LittleTilesParticle");
		
		GuiHandler.registerGuiHandler("littleStorageStructure", new LittleGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleStorage)
					return new SubGuiStorage((LittleStorage) tile.connection.getStructure(tile.te.getWorld()));
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if (tile.isConnectedToStructure() && tile.connection.getStructure(tile.te.getWorld()) instanceof LittleStorage)
					return new SubContainerStorage(player, (LittleStorage) tile.connection.getStructure(tile.te.getWorld()));
				return null;
			}
		});
		
		GuiHandler.registerGuiHandler("littleparticle", new LittleGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if (tile instanceof LittleTileParticle)
					return new SubGuiParticle((TileEntityParticle) ((LittleTileParticle) tile).getTileEntity());
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if (tile instanceof LittleTileParticle)
					return new SubContainerParticle(player, (TileEntityParticle) ((LittleTileParticle) tile).getTileEntity());
				return null;
			}
		});
		
		GuiHandler.registerGuiHandler("configure", new CustomGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
				ItemStack stack = player.getHeldItemMainhand();
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				if (iTile != null)
					return iTile.getConfigureGUI(player, stack);
				else if (stack.getItem() instanceof IBoxSelector)
					return ((IBoxSelector) stack.getItem()).getConfigureGUI(player, stack);
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				ItemStack stack = player.getHeldItemMainhand();
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				if (iTile != null)
					return iTile.getConfigureContainer(player, stack);
				else if (stack.getItem() instanceof IBoxSelector)
					return ((IBoxSelector) stack.getItem()).getConfigureContainer(player, stack);
				return null;
			}
		});
		
		GuiHandler.registerGuiHandler("configureadvanced", new CustomGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
				ItemStack stack = player.getHeldItemMainhand();
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				if (iTile != null)
					return iTile.getConfigureGUIAdvanced(player, stack);
				else if (stack.getItem() instanceof IBoxSelector)
					return ((IBoxSelector) stack.getItem()).getConfigureGUIAdvanced(player, stack);
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				ItemStack stack = player.getHeldItemMainhand();
				ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
				if (iTile != null)
					return iTile.getConfigureContainerAdvanced(player, stack);
				else if (stack.getItem() instanceof IBoxSelector)
					return ((IBoxSelector) stack.getItem()).getConfigureContainerAdvanced(player, stack);
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
		
		GuiHandler.registerGuiHandler("structureoverview", new LittleGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				return new SubGuiStructureOverview(tile);
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				return new SubContainerStructureOverview(player, tile);
			}
		});
		
		GuiHandler.registerGuiHandler("grabber", new CustomGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
				ItemStack stack = player.getHeldItemMainhand();
				return ItemLittleGrabber.getMode(stack).getGui(player, stack, ((ILittleTile) stack.getItem()).getPositionContext(stack));
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
				if (!((ItemRecipeAdvanced) stack.getItem()).hasLittlePreview(stack))
					return new SubGuiRecipeAdvancedSelection(stack);
				return new SubGuiRecipe(stack);
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				return new SubContainerRecipeAdvanced(player, player.getHeldItemMainhand());
			}
		});
		
		CreativeCorePacket.registerPacket(LittleBlockPacket.class);
		CreativeCorePacket.registerPacket(LittleBlocksUpdatePacket.class);
		CreativeCorePacket.registerPacket(LittleRotatePacket.class);
		CreativeCorePacket.registerPacket(LittleFlipPacket.class);
		CreativeCorePacket.registerPacket(LittleNeighborUpdatePacket.class);
		CreativeCorePacket.registerPacket(LittleActivateDoorPacket.class);
		CreativeCorePacket.registerPacket(LittleEntityRequestPacket.class);
		CreativeCorePacket.registerPacket(LittleBedPacket.class);
		CreativeCorePacket.registerPacket(LittleTileUpdatePacket.class);
		CreativeCorePacket.registerPacket(LittleVanillaBlockPacket.class);
		CreativeCorePacket.registerPacket(LittleSelectionModePacket.class);
		CreativeCorePacket.registerPacket(LittleBlockUpdatePacket.class);
		CreativeCorePacket.registerPacket(LittleResetAnimationPacket.class);
		CreativeCorePacket.registerPacket(LittlePlacedAnimationPacket.class);
		CreativeCorePacket.registerPacket(LittleActionMessagePacket.class);
		
		LittleAction.registerLittleAction("com", LittleActionCombined.class);
		
		LittleAction.registerLittleAction("act", LittleActionActivated.class);
		LittleAction.registerLittleAction("col", LittleActionColorBoxes.class, LittleActionColorBoxesFiltered.class);
		LittleAction.registerLittleAction("deB", LittleActionDestroyBoxes.class, LittleActionDestroyBoxesFiltered.class);
		LittleAction.registerLittleAction("des", LittleActionDestroy.class);
		LittleAction.registerLittleAction("plR", LittleActionPlaceStack.class);
		LittleAction.registerLittleAction("plA", LittleActionPlaceAbsolute.class, LittleActionPlaceAbsolutePremade.class);
		
		LittleAction.registerLittleAction("glo", LittleActionGlowstone.class, LittleActionGlowstoneRevert.class);
		LittleAction.registerLittleAction("saw", LittleActionSaw.class, LittleActionSawRevert.class);
		
		LittleAction.registerLittleAction("rep", LittleActionReplace.class);
		
		MinecraftForge.EVENT_BUS.register(new LittleEventHandler());
		MinecraftForge.EVENT_BUS.register(WorldAnimationHandler.class);
		// MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
		
		// Entity
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "sizeTNT"), EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
		
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "doorAnimation"), EntityOldDoorAnimation.class, "doorAnimation", 1, this, 2000, 250, true);
		
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
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new ExportCommand());
		event.registerServerCommand(new ImportCommand());
		event.registerServerCommand(new OpenCommand());
		event.registerServerCommand(new ToVanillaCommand());
	}
}
