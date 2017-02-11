package com.creativemd.littletiles;

import com.creativemd.creativecore.common.entity.EntitySit;
import com.creativemd.creativecore.common.packet.CreativeCorePacket;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.opener.CustomGuiHandler;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.littletiles.common.blocks.BlockLTColored;
import com.creativemd.littletiles.common.blocks.BlockLTParticle;
import com.creativemd.littletiles.common.blocks.BlockLTTransparentColored;
import com.creativemd.littletiles.common.blocks.BlockStorageTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.blocks.ItemBlockColored;
import com.creativemd.littletiles.common.blocks.ItemBlockTransparentColored;
import com.creativemd.littletiles.common.blocks.LittleTileParticle;
import com.creativemd.littletiles.common.command.ExportCommand;
import com.creativemd.littletiles.common.command.ImportCommand;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntitySizedTNTPrimed;
import com.creativemd.littletiles.common.events.LittleEvent;
import com.creativemd.littletiles.common.gui.SubContainerExport;
import com.creativemd.littletiles.common.gui.SubContainerImport;
import com.creativemd.littletiles.common.gui.SubContainerParticle;
import com.creativemd.littletiles.common.gui.SubContainerStorage;
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
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.items.ItemMultiTiles;
import com.creativemd.littletiles.common.items.ItemRecipe;
import com.creativemd.littletiles.common.items.ItemRubberMallet;
import com.creativemd.littletiles.common.items.ItemTileContainer;
import com.creativemd.littletiles.common.items.ItemUtilityKnife;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockVanillaPacket;
import com.creativemd.littletiles.common.packet.LittleDoorInteractPacket;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleNeighborUpdatePacket;
import com.creativemd.littletiles.common.packet.LittlePlacePacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.structure.LittleStorage;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.creativemd.littletiles.common.utils.LittleTile;
import com.creativemd.littletiles.common.utils.LittleTileBlock;
import com.creativemd.littletiles.common.utils.LittleTileBlockColored;
import com.creativemd.littletiles.common.utils.LittleTileTileEntity;
import com.creativemd.littletiles.common.utils.sorting.LittleTileSortingList;
import com.creativemd.littletiles.server.LittleTilesServer;
import com.google.common.base.Function;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.internal.FMLMessage.EntitySpawnMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = LittleTiles.modid, version = LittleTiles.version, name = "LittleTiles",acceptedMinecraftVersions="")
public class LittleTiles {
	
	@Instance(LittleTiles.modid)
	public static LittleTiles instance = new LittleTiles();
	
	@SidedProxy(clientSide = "com.creativemd.littletiles.client.LittleTilesClient", serverSide = "com.creativemd.littletiles.server.LittleTilesServer")
	public static LittleTilesServer proxy;
	
	public static final String modid = "littletiles";
	public static final String version = "1.3.0";
	
	public static BlockTile blockTile = (BlockTile) new BlockTile(Material.ROCK).setRegistryName("BlockLittleTiles");
	public static Block coloredBlock = new BlockLTColored().setRegistryName("LTColoredBlock").setUnlocalizedName("LTColoredBlock");
	public static Block transparentColoredBlock = new BlockLTTransparentColored().setRegistryName("LTTransparentColoredBlock").setUnlocalizedName("LTTransparentColoredBlock");
	public static Block storageBlock = new BlockStorageTile().setRegistryName("LTStorageBlockTile").setUnlocalizedName("LTStorageBlockTile");
	public static Block particleBlock = new BlockLTParticle().setRegistryName("LTParticleBlock").setUnlocalizedName("LTParticleBlock");
	
	public static Item hammer = new ItemHammer().setUnlocalizedName("LTHammer");
	public static Item recipe = new ItemRecipe().setUnlocalizedName("LTRecipe");
	public static Item multiTiles = new ItemMultiTiles().setUnlocalizedName("LTMultiTiles");
	public static Item saw = new ItemLittleSaw().setUnlocalizedName("LTSaw");
	public static Item container = new ItemTileContainer().setUnlocalizedName("LTContainer");
	public static Item wrench = new ItemLittleWrench().setUnlocalizedName("LTWrench");
	public static Item chisel = new ItemLittleChisel().setUnlocalizedName("LTChisel");
	public static Item colorTube = new ItemColorTube().setUnlocalizedName("LTColorTube");
	public static Item rubberMallet = new ItemRubberMallet().setUnlocalizedName("LTRubberMallet");
	public static Item utilityKnife = new ItemUtilityKnife().setUnlocalizedName("LTUtilityKnife");
	
	@EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		LittleTile.setGridSize(config.getInt("gridSize", "Core", 16, 1, Integer.MAX_VALUE, "ATTENTION! This needs be equal for every client. Backup your world. This will make your tiles either shrink down or increase in size!"));
		config.save();
		
		proxy.loadSidePre();
	}
	
	@EventHandler
    public void Init(FMLInitializationEvent event)
    {
		ForgeModContainer.fullBoundingBoxLadders = true;
		
		GameRegistry.registerItem(hammer, "hammer");
		GameRegistry.registerItem(recipe, "recipe");
		GameRegistry.registerItem(saw, "saw");
		GameRegistry.registerItem(container, "container");
		GameRegistry.registerItem(wrench, "wrench");
		GameRegistry.registerItem(chisel, "chisel");
		GameRegistry.registerItem(colorTube, "colorTube");
		GameRegistry.registerItem(rubberMallet, "rubberMallet");
		
		//GameRegistry.registerBlock(coloredBlock, "LTColoredBlock");
		GameRegistry.registerBlock(coloredBlock, ItemBlockColored.class);
		GameRegistry.registerBlock(transparentColoredBlock, ItemBlockTransparentColored.class);
		GameRegistry.registerBlock(blockTile, ItemBlockTiles.class);
		GameRegistry.registerBlock(storageBlock);
		GameRegistry.registerBlock(particleBlock);
		
		GameRegistry.registerItem(multiTiles, "multiTiles");
		GameRegistry.registerItem(utilityKnife, "utilityKnife");
		
		GameRegistry.registerTileEntity(TileEntityLittleTiles.class, "LittleTilesTileEntity");
		GameRegistry.registerTileEntity(TileEntityParticle.class, "LittleTilesParticle");
		
		LittleTile.registerLittleTile(LittleTileBlock.class, "BlockTileBlock");
		LittleTile.registerLittleTile(LittleTileTileEntity.class, "BlockTileEntity");
		LittleTile.registerLittleTile(LittleTileBlockColored.class, "BlockTileColored");
		
		LittleTile.registerLittleTile(LittleTileParticle.class, "BlockTileParticle");
		
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
		
		CreativeCorePacket.registerPacket(LittlePlacePacket.class, "LittlePlace");
		CreativeCorePacket.registerPacket(LittleBlockPacket.class, "LittleBlock");
		CreativeCorePacket.registerPacket(LittleRotatePacket.class, "LittleRotate");
		CreativeCorePacket.registerPacket(LittleFlipPacket.class, "LittleFlip");
		CreativeCorePacket.registerPacket(LittleNeighborUpdatePacket.class, "LittleNeighbor");
		CreativeCorePacket.registerPacket(LittleBlockVanillaPacket.class, "LittleVanillaBlock");
		CreativeCorePacket.registerPacket(LittleDoorInteractPacket.class, "LittleDoor");
		CreativeCorePacket.registerPacket(LittleEntityRequestPacket.class, "EntityRequest");
		
		//FMLCommonHandler.instance().bus().register(new LittleEvent());
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
		
		GameRegistry.addShapelessRecipe(new ItemStack(transparentColoredBlock, 1, BlockLTColored.EnumType.lava.ordinal()), Items.LAVA_BUCKET);
		
		GameRegistry.addRecipe(new ItemStack(storageBlock, 1),  new Object[]
				{
				"C", 'C', Blocks.CHEST
				});
		
		//Entity
		EntityRegistry.registerModEntity(EntitySizedTNTPrimed.class, "sizedTNT", 0, this, 250, 250, true);
		
		EntityRegistry.registerModEntity(EntityAnimation.class, "animation", 1, this, 2000, 250, true);
		
		proxy.loadSide();
    }
	
	@EventHandler
	public void serverStarting(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new ExportCommand());
		event.registerServerCommand(new ImportCommand());
	}
	
	@EventHandler
    public void LoadComplete(FMLLoadCompleteEvent event)
    {
		LittleTileSortingList.initVanillaBlocks();
    }
}
