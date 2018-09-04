package com.creativemd.littletiles;

import java.util.List;

import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
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
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative;
import com.creativemd.littletiles.common.action.block.LittleActionReplace;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone.LittleActionGlowstoneRevert;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw.LittleActionSawRevert;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.api.blocks.DefaultBlockHandler;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockLTFlowingLava;
import com.creativemd.littletiles.common.blocks.BlockLTFlowingWater;
import com.creativemd.littletiles.common.blocks.BlockLTFlowingWater.LittleFlowingWaterPreview;
import com.creativemd.littletiles.common.blocks.BlockLTParticle;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockStorageTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ItemBlockColored;
import com.creativemd.littletiles.common.blocks.ItemBlockFlowingLava;
import com.creativemd.littletiles.common.blocks.ItemBlockFlowingWater;
import com.creativemd.littletiles.common.blocks.ItemBlockTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockLTFlowingLava.LittleFlowingLavaPreview;
import com.creativemd.littletiles.common.command.ExportCommand;
import com.creativemd.littletiles.common.command.ImportCommand;
import com.creativemd.littletiles.common.command.OpenCommand;
import com.creativemd.littletiles.common.config.IGCMLoader;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.container.SubContainerExport;
import com.creativemd.littletiles.common.container.SubContainerImport;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.container.SubContainerRecipeAdvanced;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.container.SubContainerWorkbench;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.events.LittleDoorHandler;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.gui.SubGuiExport;
import com.creativemd.littletiles.common.gui.SubGuiImport;
import com.creativemd.littletiles.common.gui.SubGuiParticle;
import com.creativemd.littletiles.common.gui.SubGuiRecipeAdvanced;
import com.creativemd.littletiles.common.gui.SubGuiStorage;
import com.creativemd.littletiles.common.gui.SubGuiWorkbench;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemLittleGrabber;
import com.creativemd.littletiles.common.items.ItemLittleSaw;
import com.creativemd.littletiles.common.items.ItemLittleScrewdriver;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemPremadeStructure;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRecipeAdvanced;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.packet.LittleBedPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleDoorInteractPacket;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleNeighborUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.packet.LittleSlidingDoorPacket;
import com.creativemd.littletiles.common.packet.LittleTileUpdatePacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.structure.LittleStorage;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.premade.LittleStructurePremade;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTicking;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTilesTickingRendered;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.LittleTileTE;
import com.creativemd.littletiles.common.tiles.advanced.LittleTileParticle;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreviewHandler;
import com.creativemd.littletiles.common.utils.grid.LittleGridContext;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.server.LittleTilesServer;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles",acceptedMinecraftVersions="",guiFactory="com.creativemd.littletiles.client.LittleTilesSettings")
@Mod.EventBusSubscriber
public class LittleTiles {
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "1.5.0";
	
	public static CreativeTabs littleTab = new CreativeTabs("littletiles") {
		
		@Override
		public ItemStack getTabIconItem() {
			return new ItemStack(hammer);
		}
	};
	
	public static BlockTile blockTileNoTicking = (BlockTile) new BlockTile(Material.ROCK, false, false).setRegistryName("BlockLittleTiles");
	public static BlockTile blockTileTicking = (BlockTile) new BlockTile(Material.ROCK, true, false).setRegistryName("BlockLittleTilesTicking");
	public static BlockTile blockTileNoTickingRendered = (BlockTile) new BlockTile(Material.ROCK, false, true).setRegistryName("BlockLittleTilesRendered");
	public static BlockTile blockTileTickingRendered = (BlockTile) new BlockTile(Material.ROCK, true, true).setRegistryName("BlockLittleTilesTickingRendered");
	
	public static Block coloredBlock = new BlockLTColored().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock").setHardness(1.5F);
	public static Block transparentColoredBlock = new BlockLTTransparentColored().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock").setHardness(0.3F);
	public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile").setHardness(1.5F);
	public static Block particleBlock = new BlockLTParticle().setRegistryName("LTParticleBlock").setUnlocalizedName("LTParticleBlock").setHardness(1.5F);
	
	public static Block flowingWater = new BlockLTFlowingWater(BlockLTTransparentColored.EnumType.water).setRegistryName("LTFlowingWater").setUnlocalizedName("LTFlowingWater").setHardness(0.3F);
	public static Block whiteFlowingWater = new BlockLTFlowingWater(BlockLTTransparentColored.EnumType.white_water).setRegistryName("LTWhiteFlowingWater").setUnlocalizedName("LTWhiteFlowingWater").setHardness(0.3F);
	
	public static Block flowingLava = new BlockLTFlowingLava(BlockLTColored.EnumType.lava).setRegistryName("LTFlowingLava").setUnlocalizedName("LTFlowingLava").setHardness(0.3F);
	public static Block whiteFlowingLava = new BlockLTFlowingLava(BlockLTColored.EnumType.white_lava).setRegistryName("LTWhiteFlowingLava").setUnlocalizedName("LTWhiteFlowingLava").setHardness(0.3F);
	
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
	
	private void removeMissingProperties(String path, ConfigCategory category, List<String> allowedNames)
	{
		for(ConfigCategory child : category.getChildren())
			removeMissingProperties(path + (path.isEmpty() ? "" : ".") + category.getName(), child, allowedNames);
		for(String propertyName : category.getPropertyOrder())
		{
			String name = path + (path.isEmpty() ? "" : ".") + propertyName;
			if(!allowedNames.contains(name))
				category.remove(propertyName);
		}
	}
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		event.getModMetadata().version = version;
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		LittleGridContext.loadGrid(config.getInt("minSize", "core", 1, 1, Integer.MAX_VALUE, "The minimum grid size possible. ATTENTION! This needs be equal for every client & server. Backup your world."),
				config.getInt("defaultSize", "core", 16, 1, Integer.MAX_VALUE, "Needs to be part of the row. ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!"),
				config.getInt("scale", "core", 6, 1, Integer.MAX_VALUE, "How many grids there are. ATTENTION! This needs be equal for every client & server. Make sure that it is enough for the defaultSize to exist."),
				config.getInt("exponent", "core", 2, 1, Integer.MAX_VALUE, "minSize ^ (exponent * scale). ATTENTION! This needs be equal for every client & server. Default is two -> (1, 2, 4, 8, 16, 32 etc.)."));
		List<String> allowedPropertyNames = LittleTilesConfig.getConfigProperties();
		for(String categoryName : config.getCategoryNames())
			removeMissingProperties(categoryName, config.getCategory(categoryName), allowedPropertyNames);
		config.save();
		proxy.loadSidePre();
		
		hammer = new ItemHammer().setUnlocalizedName("LTHammer").setRegistryName("hammer");
		recipe = new ItemRecipe().setUnlocalizedName("LTRecipe").setRegistryName("recipe");
		recipeAdvanced = new ItemRecipeAdvanced().setUnlocalizedName("LTRecipeAdvanced").setRegistryName("recipeadvanced");
		multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles").setRegistryName("multiTiles");
		saw = new ItemLittleSaw().setUnlocalizedName("LTSaw").setRegistryName("saw");
		container = new ItemTileContainer().setUnlocalizedName("LTContainer").setRegistryName("container");
		wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench").setRegistryName("wrench");
		screwdriver = new ItemLittleScrewdriver().setUnlocalizedName("LTScrewdriver").setRegistryName("screwdriver");
		chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel").setRegistryName("chisel");
		colorTube = new ItemColorTube().setUnlocalizedName("LTColorTube").setRegistryName("colorTube");
		rubberMallet = new ItemRubberMallet().setUnlocalizedName("LTRubberMallet").setRegistryName("rubberMallet");
		utilityKnife = new ItemUtilityKnife().setUnlocalizedName("LTUtilityKnife").setRegistryName("utilityKnife");
		grabber = new ItemLittleGrabber().setUnlocalizedName("LTGrabber").setRegistryName("grabber");
		premade = new ItemPremadeStructure().setUnlocalizedName("LTPremade").setRegistryName("premade");
		
		LittleStructure.initStructures();
	}
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event) {
		event.getRegistry().registerAll(coloredBlock, transparentColoredBlock, blockTileNoTicking, blockTileTicking, blockTileNoTickingRendered, blockTileTickingRendered, storageBlock, particleBlock,
				flowingWater, whiteFlowingWater, flowingLava, whiteFlowingLava);
	}
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(hammer, recipe, recipeAdvanced, saw, container, wrench, screwdriver, chisel, colorTube, rubberMallet, multiTiles, utilityKnife, grabber, premade,
				new ItemBlock(storageBlock).setRegistryName(storageBlock.getRegistryName()), new ItemBlock(particleBlock).setRegistryName(particleBlock.getRegistryName()),
				new ItemBlockColored(coloredBlock, coloredBlock.getRegistryName()).setRegistryName(coloredBlock.getRegistryName()),
				new ItemBlockTransparentColored(transparentColoredBlock, transparentColoredBlock.getRegistryName()).setRegistryName(transparentColoredBlock.getRegistryName()),
				new ItemBlockTiles(blockTileNoTicking, blockTileNoTicking.getRegistryName()).setRegistryName(blockTileNoTicking.getRegistryName()),
				new ItemBlockTiles(blockTileTicking, blockTileTicking.getRegistryName()).setRegistryName(blockTileTicking.getRegistryName()),
				new ItemBlockTiles(blockTileNoTickingRendered, blockTileNoTickingRendered.getRegistryName()).setRegistryName(blockTileNoTickingRendered.getRegistryName()),
				new ItemBlockTiles(blockTileTickingRendered, blockTileTickingRendered.getRegistryName()).setRegistryName(blockTileTickingRendered.getRegistryName()),
				new ItemBlockFlowingWater(flowingWater, flowingWater.getRegistryName()).setRegistryName(flowingWater.getRegistryName()),
				new ItemBlockFlowingWater(whiteFlowingWater, whiteFlowingWater.getRegistryName()).setRegistryName(whiteFlowingWater.getRegistryName()),
				new ItemBlockFlowingLava(flowingLava, flowingLava.getRegistryName()).setRegistryName(flowingLava.getRegistryName()),
				new ItemBlockFlowingLava(whiteFlowingLava, whiteFlowingLava.getRegistryName()).setRegistryName(whiteFlowingLava.getRegistryName()));
		
		proxy.loadSide();
	}
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		ForgeModContainer.fullBoundingBoxLadders = true;
		
		GameRegistry.register(hammer);
		GameRegistry.register(recipe);
		GameRegistry.register(recipeAdvanced);
		GameRegistry.register(grabber);
		GameRegistry.register(saw);
		GameRegistry.register(container);
		GameRegistry.register(wrench);
		GameRegistry.register(screwdriver);
		GameRegistry.register(chisel);
		GameRegistry.register(colorTube);
		GameRegistry.register(rubberMallet);
		GameRegistry.register(premade);
		
		//GameRegistry.registerBlock(coloredBlock, "LTColoredBlock");
		GameRegistry.register(coloredBlock);
		GameRegistry.register(new ItemBlockColored(coloredBlock, coloredBlock.getRegistryName()).setRegistryName(coloredBlock.getRegistryName()));
		
		GameRegistry.register(transparentColoredBlock);
		GameRegistry.register(new ItemBlockTransparentColored(transparentColoredBlock, transparentColoredBlock.getRegistryName()).setRegistryName(transparentColoredBlock.getRegistryName()));
		
		GameRegistry.register(blockTileTicking);
		GameRegistry.register(new ItemBlockTiles(blockTileTicking, blockTileTicking.getRegistryName()).setRegistryName(blockTileTicking.getRegistryName()));
		
		GameRegistry.register(blockTileNoTicking);
		GameRegistry.register(new ItemBlockTiles(blockTileNoTicking, blockTileNoTicking.getRegistryName()).setRegistryName(blockTileNoTicking.getRegistryName()));
		
		GameRegistry.register(blockTileTickingRendered);
		GameRegistry.register(new ItemBlockTiles(blockTileTickingRendered, blockTileTickingRendered.getRegistryName()).setRegistryName(blockTileTickingRendered.getRegistryName()));
		
		GameRegistry.register(blockTileNoTickingRendered);
		GameRegistry.register(new ItemBlockTiles(blockTileNoTickingRendered, blockTileNoTickingRendered.getRegistryName()).setRegistryName(blockTileNoTickingRendered.getRegistryName()));

		GameRegistry.registerWithItem(storageBlock);
		GameRegistry.registerWithItem(particleBlock);
		
		GameRegistry.register(multiTiles);
		GameRegistry.register(utilityKnife);
		
		GameRegistry.register(flowingWater);
		GameRegistry.register(whiteFlowingWater);
		GameRegistry.register(flowingLava);
		GameRegistry.register(whiteFlowingLava);
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		GameRegistry.registerTileEntity(TileEntityLittleTilesTicking.class, "LittleTilesTileEntityTicking");
		GameRegistry.registerTileEntity(TileEntityLittleTilesRendered.class, "LittleTilesTileEntityRendered");
		GameRegistry.registerTileEntity(TileEntityLittleTilesTickingRendered.class, "LittleTilesTileEntityTickingRendered");
		GameRegistry.registerTileEntity(TileEntityParticle.class, "LittleTilesParticle");
		
		LittleTile.registerLittleTile(LittleTileBlock.class, "BlockTileBlock", LittleTilePreviewHandler.defaultHandler);
		LittleTile.registerLittleTile(LittleTileTE.class, "BlockTileEntity", LittleTilePreviewHandler.defaultHandler);
		LittleTile.registerLittleTile(LittleTileBlockColored.class, "BlockTileColored", LittleTilePreviewHandler.defaultHandler);
		
		LittleTile.registerLittleTile(LittleTileParticle.class, "BlockTileParticle", LittleTilePreviewHandler.defaultHandler);
		
		LittleTilePreview.registerPreviewType("water", LittleFlowingWaterPreview.class);
		LittleTilePreview.registerPreviewType("lava", LittleFlowingLavaPreview.class);
		
		GuiHandler.registerGuiHandler("littleStorageStructure", new LittleGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if(tile.isStructureBlock && tile.structure instanceof LittleStorage)
					return new SubGuiStorage((LittleStorage) tile.structure);
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if(tile.isStructureBlock && tile.structure instanceof LittleStorage)
					return new SubContainerStorage(player, (LittleStorage) tile.structure);
				return null;
			}
		});
		
		GuiHandler.registerGuiHandler("littleparticle", new LittleGuiHandler() {
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if(tile instanceof LittleTileParticle)
					return new SubGuiParticle((TileEntityParticle) ((LittleTileParticle) tile).getTileEntity());
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt, LittleTile tile) {
				if(tile instanceof LittleTileParticle)
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
				if(iTile != null)
					return iTile.getConfigureGUI(player, stack);
				else if(stack.getItem() instanceof ISpecialBlockSelector)
					return ((ISpecialBlockSelector) stack.getItem()).getConfigureGUI(player, stack);
				return null;
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				return new SubContainerConfigure(player, player.getHeldItemMainhand());
			}
		});
		
		GuiHandler.registerGuiHandler("chisel", new CustomGuiHandler(){
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
				return new SubGuiChisel(player.getHeldItemMainhand());
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				return new SubContainerConfigure(player, player.getHeldItemMainhand());
			}
		});
		
		GuiHandler.registerGuiHandler("grabber", new CustomGuiHandler(){
			
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
				return new SubGuiRecipeAdvanced(player.getHeldItemMainhand());
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				return new SubContainerRecipeAdvanced(player, player.getHeldItemMainhand(), new BlockPos(nbt.getInteger("posX"), nbt.getInteger("posY"), nbt.getInteger("posZ")));
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
		

		CreativeCorePacket.registerPacket(LittleBlockPacket.class, "LittleBlock");
		CreativeCorePacket.registerPacket(LittleRotatePacket.class, "LittleRotate");
		CreativeCorePacket.registerPacket(LittleFlipPacket.class, "LittleFlip");
		CreativeCorePacket.registerPacket(LittleNeighborUpdatePacket.class, "LittleNeighbor");
		CreativeCorePacket.registerPacket(LittleDoorInteractPacket.class, "LittleDoor");
		CreativeCorePacket.registerPacket(LittleSlidingDoorPacket.class, "LittleSlidingDoor");
		CreativeCorePacket.registerPacket(LittleEntityRequestPacket.class, "EntityRequest");
		CreativeCorePacket.registerPacket(LittleBedPacket.class, "LittleBed");
		CreativeCorePacket.registerPacket(LittleTileUpdatePacket.class, "TileUpdate");
		CreativeCorePacket.registerPacket(LittleVanillaBlockPacket.class, "VanillaBlock");
		
		LittleAction.registerLittleAction("com", LittleActionCombined.class);
		
		LittleAction.registerLittleAction("act", LittleActionActivated.class);
		LittleAction.registerLittleAction("col", LittleActionColorBoxes.class, LittleActionColorBoxesFiltered.class);
		LittleAction.registerLittleAction("deB", LittleActionDestroyBoxes.class, LittleActionDestroyBoxesFiltered.class);
		LittleAction.registerLittleAction("des", LittleActionDestroy.class);
		LittleAction.registerLittleAction("plR", LittleActionPlaceRelative.class);
		LittleAction.registerLittleAction("plA", LittleActionPlaceAbsolute.class, LittleActionPlaceAbsolutePremade.class);
		
		LittleAction.registerLittleAction("glo", LittleActionGlowstone.class, LittleActionGlowstoneRevert.class);
		LittleAction.registerLittleAction("saw", LittleActionSaw.class, LittleActionSawRevert.class);
		
		LittleAction.registerLittleAction("rep", LittleActionReplace.class);
		
		MinecraftForge.EVENT_BUS.register(new LittleEvent());
		MinecraftForge.EVENT_BUS.register(LittleDoorHandler.server = new LittleDoorHandler(Side.SERVER));
		//MinecraftForge.EVENT_BUS.register(ChiselAndBitsConveration.class);
		
		//Recipes
		GameRegistry.addRecipe(new ItemStack(recipe, 5),  new Object[]
				{
				"XAX", "AXA", "XAX", 'X', Items.PAPER
				});
		GameRegistry.addRecipe(new ItemStack(recipeAdvanced, 5),  new Object[]
				{
				"XAX", "AXA", "XAX", 'X', Items.PAPER, 'A', Items.REDSTONE
				});
		
		GameRegistry.addRecipe(new ItemStack(hammer),  new Object[]
				{
				"XXX", "ALA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(container),  new Object[]
				{
				"XXX", "XHX", "XXX", 'X', Items.IRON_INGOT, 'H', hammer
				});
		
		GameRegistry.addRecipe(new ItemStack(saw),  new Object[]
				{
				"AXA", "AXA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(wrench),  new Object[]
				{
				"AXA", "ALA", "ALA", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(rubberMallet),  new Object[]
				{
				"XXX", "XLX", "ALA", 'X', Blocks.WOOL, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(colorTube),  new Object[]
				{
				"XXX", "XLX", "XXX", 'X', Items.DYE, 'L', Items.IRON_INGOT
				});
		
		GameRegistry.addRecipe(new ItemStack(utilityKnife),  new Object[]
				{
				"XAA", "AXA", "AAL", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(chisel),  new Object[]
				{
				"XAA", "ALA", "AAL", 'X', Items.IRON_INGOT, 'L', new ItemStack(Items.DYE, 1, 4)
				});

		GameRegistry.addRecipe(new ItemStack(grabber),  new Object[]
				{
					"LLL", "WWW", "LLL", 'W', new ItemStack(Blocks.WOOL, 1, 11), 'L', new ItemStack(Items.DYE, 1, 4)
				});
		
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 0),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 1),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.SANDSTONE
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 2),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.COBBLESTONE
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 3),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.DIRT
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 4),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.GRAVEL
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 5),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.BRICK_BLOCK
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 6),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.STONE
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 7),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', new ItemStack(Blocks.STONEBRICK, 1, 0)
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 8),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', new ItemStack(Blocks.STONEBRICK, 1, 3)
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 9),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', new ItemStack(Blocks.STONEBRICK, 1, 2)
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 10),  new Object[]
				{
				"XXX", "XAX", "XXX", 'X', Blocks.QUARTZ_BLOCK, 'A', Blocks.HARDENED_CLAY
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 9, 11),  new Object[]
				{
				"XGX", "GBG", "XGX", 'G', Items.GLOWSTONE_DUST, 'B', new ItemStack(coloredBlock, 1, 0)
				});
		GameRegistry.addRecipe(new ItemStack(coloredBlock, 8, 13),  new Object[]
				{
				"XXX", "XGX", "XXX", 'G', new ItemStack(Items.DYE, 1, 15), 'X', Blocks.PLANKS
				});
		
		GameRegistry.addRecipe(new ItemStack(transparentColoredBlock, 5, 0),  new Object[]
				{
				"SXS", "XGX", "SXS", 'S', new ItemStack(Blocks.STAINED_GLASS, 1, 0), 'G', Blocks.GLASS
				});
		GameRegistry.addRecipe(new ItemStack(transparentColoredBlock, 5, 1),  new Object[]
				{
				"SXS", "XSX", "SXS", 'S', new ItemStack(Blocks.STAINED_GLASS, 1, 0)
				});
		GameRegistry.addRecipe(new ItemStack(transparentColoredBlock, 3, 2),  new Object[]
				{
				"SSS", 'S', new ItemStack(Blocks.STAINED_GLASS, 1, 0)
				});
		GameRegistry.addRecipe(new ItemStack(transparentColoredBlock, 2, 3),  new Object[]
				{
				"SS", 'S', new ItemStack(Blocks.STAINED_GLASS, 1, 0)
				});
		GameRegistry.addRecipe(new ItemStack(transparentColoredBlock, 1, 4),  new Object[]
				{
				"S", 'S', new ItemStack(Blocks.STAINED_GLASS, 1, 0)
				});
		
		GameRegistry.addRecipe(new ItemStack(particleBlock),  new Object[]
				{
				"XGX", "GLG", "XGX", 'X', Items.COAL, 'L', Items.DYE, 'G', Items.GUNPOWDER
				});
		
		//Water block
		GameRegistry.addShapelessRecipe(new ItemStack(transparentColoredBlock, 1, BlockLTTransparentColored.EnumType.water.ordinal()), Items.WATER_BUCKET);
		
		GameRegistry.addShapelessRecipe(new ItemStack(transparentColoredBlock, 1, BlockLTTransparentColored.EnumType.white_water.ordinal()), new ItemStack(transparentColoredBlock, 1, BlockLTTransparentColored.EnumType.water.ordinal()), Items.MILK_BUCKET);
		
		GameRegistry.addShapelessRecipe(new ItemStack(coloredBlock, 1, BlockLTColored.EnumType.lava.ordinal()), Items.LAVA_BUCKET);
		
		GameRegistry.addRecipe(new ItemStack(storageBlock, 1),  new Object[]
				{
				"C", 'C', Blocks.CHEST
				});
		
		GameRegistry.addRecipe(LittleStructurePremade.getPremadeStack("exporter"),  new Object[]
				{
				"PPP", "PWP", "PPP", 'P', Blocks.PLANKS, 'W', LittleTiles.wrench
				});
		
		GameRegistry.addRecipe(LittleStructurePremade.getPremadeStack("exporter"),  new Object[]
				{
				"PXP", "ILI", "IRI", 'P', Blocks.PLANKS, 'X', Blocks.IRON_BARS, 'I', Items.IRON_INGOT, 'L', LittleTiles.recipe, 'R', Items.REDSTONE
				});
		
		GameRegistry.addRecipe(LittleStructurePremade.getPremadeStack("importer"),  new Object[]
				{
				"PXP", "ILI", "IRI", 'P', Blocks.PLANKS, 'X', Blocks.GLASS, 'I', Items.IRON_INGOT, 'L', LittleTiles.recipe, 'R', Items.REDSTONE
				});
		
		//Entity
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "sizeTNT"), EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
		
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "doorAnimation"), EntityDoorAnimation.class, "doorAnimation", 1, this, 2000, 250, true);
		
		proxy.loadSide();
		
		DefaultBlockHandler.initVanillaBlockHandlers();

		if(Loader.isModLoaded("igcm"))
			IGCMLoader.initIGCM();
    }
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ExportCommand());
		event.registerServerCommand(new ImportCommand());
		event.registerServerCommand(new OpenCommand());
	}
}
