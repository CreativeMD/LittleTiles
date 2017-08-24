package com.creativemd.littletiles;

import java.io.File;

import com.creativemd.creativecore.CreativeCore;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.LittleActionCombined;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.action.block.LittleActionColorBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionDestroy;
import com.creativemd.littletiles.common.action.block.LittleActionDestroyBoxes;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceAbsolute;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceRelative;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone;
import com.creativemd.littletiles.common.action.tool.LittleActionMove;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone.LittleActionGlowstoneRevert;
import com.creativemd.littletiles.common.action.tool.LittleActionMove.LittleActionMoveRevert;
import com.creativemd.littletiles.common.action.tool.LittleActionSaw.LittleActionSawRevert;
import com.creativemd.littletiles.common.api.blocks.DefaultBlockHandler;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockLTParticle;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockStorageTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ItemBlockColored;
import com.creativemd.littletiles.common.blocks.ItemBlockTransparentColored;
import com.creativemd.littletiles.common.command.ExportCommand;
import com.creativemd.littletiles.common.command.ImportCommand;
import com.creativemd.littletiles.common.container.SubContainerChisel;
import com.creativemd.littletiles.common.container.SubContainerExport;
import com.creativemd.littletiles.common.container.SubContainerImport;
import com.creativemd.littletiles.common.container.SubContainerParticle;
import com.creativemd.littletiles.common.container.SubContainerStorage;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.gui.SubGuiExport;
import com.creativemd.littletiles.common.gui.SubGuiImport;
import com.creativemd.littletiles.common.gui.SubGuiParticle;
import com.creativemd.littletiles.common.gui.SubGuiStorage;
import com.creativemd.littletiles.common.gui.handler.LittleGuiHandler;
import com.creativemd.littletiles.common.items.ItemBlockTiles;
import com.creativemd.littletiles.common.items.ItemColorTube;
import com.creativemd.littletiles.common.items.ItemHammer;
import com.creativemd.littletiles.common.items.ItemLittleChisel;
import com.creativemd.littletiles.common.items.ItemLittleSaw;
import com.creativemd.littletiles.common.items.ItemLittleScrewdriver;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
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
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.LittleTileTE;
import com.creativemd.littletiles.common.tiles.advanced.LittleTileParticle;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreviewHandler;
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
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.ModListHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles",acceptedMinecraftVersions="")
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
	
	public static BlockTile blockTile = (BlockTile) new BlockTile(Material.ROCK).setRegistryName("BlockLittleTiles");
	public static Block coloredBlock = new BlockLTColored().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock").setHardness(1.5F);
	public static Block transparentColoredBlock = new BlockLTTransparentColored().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock").setHardness(0.3F);
	public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile").setHardness(1.5F);
	public static Block particleBlock = new BlockLTParticle().setRegistryName("LTParticleBlock").setUnlocalizedName("LTParticleBlock").setHardness(1.5F);
	
	public static Item hammer = new ItemHammer().setUnlocalizedName("LTHammer").setRegistryName("hammer");
	public static Item recipe = new ItemRecipe().setUnlocalizedName("LTRecipe").setRegistryName("recipe");
	public static Item multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles").setRegistryName("multiTiles");
	public static Item saw = new ItemLittleSaw().setUnlocalizedName("LTSaw").setRegistryName("saw");
	public static Item container = new ItemTileContainer().setUnlocalizedName("LTContainer").setRegistryName("container");
	public static Item wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench").setRegistryName("wrench");
	public static Item screwdriver = new ItemLittleScrewdriver().setUnlocalizedName("LTScrewdriver").setRegistryName("screwdriver");
	public static Item chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel").setRegistryName("chisel");
	public static Item colorTube = new ItemColorTube().setUnlocalizedName("LTColorTube").setRegistryName("colorTube");
	public static Item rubberMallet = new ItemRubberMallet().setUnlocalizedName("LTRubberMallet").setRegistryName("rubberMallet");
	public static Item utilityKnife = new ItemUtilityKnife().setUnlocalizedName("LTUtilityKnife").setRegistryName("utilityKnife");
	
	public static boolean invertedShift = false;
	public static boolean hideParticleBlock = false;
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		event.getModMetadata().version = version;
		
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		LittleTile.setGridSize(config.getInt("gridSize", "Core", 16, 1, Integer.MAX_VALUE, "ATTENTION! This needs be equal for every client & server. Backup your world. This will make your tiles either shrink down or increase in size!"));
		invertedShift = config.getBoolean("invertedShift", "Core", invertedShift, "If shift behavior is inverted.");
		hideParticleBlock = config.getBoolean("hideParticleBlock", "Rendering", hideParticleBlock, "Can be used for cinematics");
		config.save();
		proxy.loadSidePre();
	}
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		ForgeModContainer.fullBoundingBoxLadders = true;
		
		GameRegistry.register(hammer);
		GameRegistry.register(recipe);
		GameRegistry.register(saw);
		GameRegistry.register(container);
		GameRegistry.register(wrench);
		GameRegistry.register(screwdriver);
		GameRegistry.register(chisel);
		GameRegistry.register(colorTube);
		GameRegistry.register(rubberMallet);
		
		//GameRegistry.registerBlock(coloredBlock, "LTColoredBlock");
		GameRegistry.register(coloredBlock);
		GameRegistry.register(new ItemBlockColored(coloredBlock, coloredBlock.getRegistryName()).setRegistryName(coloredBlock.getRegistryName()));
		
		GameRegistry.register(transparentColoredBlock);
		GameRegistry.register(new ItemBlockTransparentColored(transparentColoredBlock, transparentColoredBlock.getRegistryName()).setRegistryName(transparentColoredBlock.getRegistryName()));
		
		GameRegistry.register(blockTile);
		GameRegistry.register(new ItemBlockTiles(blockTile, blockTile.getRegistryName()).setRegistryName(blockTile.getRegistryName()));
		
		GameRegistry.registerWithItem(storageBlock);
		GameRegistry.registerWithItem(particleBlock);
		
		GameRegistry.register(multiTiles);
		GameRegistry.register(utilityKnife);
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		GameRegistry.registerTileEntity(TileEntityParticle.class, "LittleTilesParticle");
		
		LittleTile.registerLittleTile(LittleTileBlock.class, "BlockTileBlock", LittleTilePreviewHandler.defaultHandler);
		LittleTile.registerLittleTile(LittleTileTE.class, "BlockTileEntity", LittleTilePreviewHandler.defaultHandler);
		LittleTile.registerLittleTile(LittleTileBlockColored.class, "BlockTileColored", LittleTilePreviewHandler.defaultHandler);
		
		LittleTile.registerLittleTile(LittleTileParticle.class, "BlockTileParticle", LittleTilePreviewHandler.defaultHandler);
		
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
		
		GuiHandler.registerGuiHandler("chisel", new CustomGuiHandler(){
			
			@Override
			@SideOnly(Side.CLIENT)
			public SubGui getGui(EntityPlayer player, NBTTagCompound nbt) {
				return new SubGuiChisel(player.getHeldItemMainhand());
			}
			
			@Override
			public SubContainer getContainer(EntityPlayer player, NBTTagCompound nbt) {
				return new SubContainerChisel(player, player.getHeldItemMainhand());
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
		LittleAction.registerLittleAction("col", LittleActionColorBoxes.class);
		LittleAction.registerLittleAction("deB", LittleActionDestroyBoxes.class);
		LittleAction.registerLittleAction("des", LittleActionDestroy.class);
		LittleAction.registerLittleAction("plR", LittleActionPlaceRelative.class);
		LittleAction.registerLittleAction("plA", LittleActionPlaceAbsolute.class);
		
		LittleAction.registerLittleAction("glo", LittleActionGlowstone.class, LittleActionGlowstoneRevert.class);
		LittleAction.registerLittleAction("rub", LittleActionMove.class, LittleActionMoveRevert.class);
		LittleAction.registerLittleAction("saw", LittleActionSaw.class, LittleActionSawRevert.class);
		
		MinecraftForge.EVENT_BUS.register(new LittleEvent());
		
		LittleStructure.initStructures();
		
		//Recipes
		GameRegistry.addRecipe(new ItemStack(recipe, 5),  new Object[]
				{
				"XAX", "AXA", "XAX", 'X', Items.PAPER
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
		
		GameRegistry.addShapelessRecipe(new ItemStack(coloredBlock, 1, BlockLTColored.EnumType.lava.ordinal()), Items.LAVA_BUCKET);
		
		GameRegistry.addRecipe(new ItemStack(storageBlock, 1),  new Object[]
				{
				"C", 'C', Blocks.CHEST
				});
		
		//Entity
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "sizeTNT"), EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
		
		EntityRegistry.registerModEntity(new ResourceLocation(modid, "doorAnimation"), EntityDoorAnimation.class, "doorAnimation", 1, this, 2000, 250, true);
		
		proxy.loadSide();
		
		DefaultBlockHandler.initVanillaBlockHandlers();
    }
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ExportCommand());
		event.registerServerCommand(new ImportCommand());
	}
}
