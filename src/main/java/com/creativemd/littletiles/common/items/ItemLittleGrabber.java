package com.creativemd.littletiles.common.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.common.utils.Rotation;
import com.creativemd.creativecore.gui.container.SubContainer;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.gui.opener.GuiHandler;
import com.creativemd.creativecore.gui.opener.IGuiCreator;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.container.SubContainerChisel;
import com.creativemd.littletiles.common.container.SubContainerGrabber;
import com.creativemd.littletiles.common.gui.SubGuiChisel;
import com.creativemd.littletiles.common.gui.SubGuiGrabber;
import com.creativemd.littletiles.common.items.ItemLittleGrabber.PlacePreviewMode;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlock;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.preview.LittleTilePreview;
import com.creativemd.littletiles.common.tiles.vec.LittleTileBox;
import com.creativemd.littletiles.common.tiles.vec.LittleTileSize;
import com.creativemd.littletiles.common.tiles.vec.LittleTileVec;
import com.creativemd.littletiles.common.utils.geo.DragShape;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import scala.tools.nsc.doc.model.Def;
import scala.tools.nsc.transform.patmat.Solving.Solver.Lit;

public class ItemLittleGrabber extends Item implements ICreativeRendered, ILittleTile {
	
	public ItemLittleGrabber()
	{
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
    {
        return 0F;
    }
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player)
    {
        return false;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return getMode(stack).getRenderingCubes(stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType)
	{
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		IBakedModel model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":grabber_background", "inventory"));
		ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
		
		mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
		
		if(cameraTransformType == TransformType.GUI)
		{
			GlStateManager.scale(0.9, 0.9, 0.9);
			
			GrabberMode mode = getMode(stack);
			List<LittleTilePreview> previews = mode.getPreviews(stack);
			if(mode.renderBlockSeparately(stack) && previews.size() == 1)
			{
				LittleTilePreview preview = previews.get(0);
				ItemStack blockStack = new ItemStack(preview.getPreviewBlock(), 1, preview.getPreviewBlockMeta());
				model =  mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); //getItemModelMesher().getItemModel(blockStack);
				if(!(model instanceof CreativeBakedModel))
					ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
				
				GlStateManager.disableDepth();
				GlStateManager.pushMatrix();
		        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		        
		        
				try {
					if (model.isBuiltInRenderer())
		            {
		                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		                GlStateManager.enableRescaleNormal();
		                TileEntityItemStackRenderer.instance.renderByItem(blockStack);
		            }else{
						Color color = preview.hasColor() ? ColorUtils.IntToRGBA(preview.getColor()) : ColorUtils.IntToRGBA(ColorUtils.WHITE);
						color.setAlpha(255);
						ReflectionHelper.findMethod(RenderItem.class, "renderModel", "func_191967_a", IBakedModel.class, int.class, ItemStack.class).invoke(mc.getRenderItem(), model, ColorUtils.RGBAToInt(color), blockStack);
		            }
		        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
				
				
				GlStateManager.popMatrix();
			
			}
			
			GlStateManager.enableDepth();
			
		}
		
		GlStateManager.popMatrix();
		
	}
	
	@Override
	public boolean hasLittlePreview(ItemStack stack)
	{
		return true;
	}
	
	@Override
	public List<LittleTilePreview> getLittlePreview(ItemStack stack)
	{
		return getMode(stack).getPreviews(stack);
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, List<LittleTilePreview> previews)
	{
		PlacePreviewMode.setPreview(stack, previews);
	}
	
	@Override
	public LittleStructure getLittleStructure(ItemStack stack)
	{
		return null;
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack)
	{
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result)
	{
		getMode(stack).onClickBlock(player, stack, result);
	}
	
	@Override
	public boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {
		return getMode(stack).onMouseWheelClickBlock(player, stack, result);
	}
	
	public static GrabberMode getMode(String name)
	{
		GrabberMode mode = GrabberMode.modes.get(name);
		if(mode != null)
			return mode;
		return GrabberMode.defaultMode;
	}
	
	public static GrabberMode getMode(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		GrabberMode mode = GrabberMode.modes.get(stack.getTagCompound().getString("mode"));
		if(mode != null)
			return mode;
		return GrabberMode.defaultMode;
	}
	
	public static void setMode(ItemStack stack, GrabberMode mode)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setString("mode", mode.name);
	}
	
	public static GrabberMode[] getModes()
	{
		return GrabberMode.modes.values().toArray(new GrabberMode[0]);
	}
	
	public static int indexOf(GrabberMode mode)
	{
		GrabberMode[] modes = getModes();
		for (int i = 0; i < modes.length; i++) {
			if(modes[i] == mode)
				return i;
		}
		return -1;
	}
	
	public static abstract class GrabberMode {
		
		public static HashMap<String, GrabberMode> modes = new HashMap<>();
		
		public static PixelMode pixelMode = new PixelMode();
		public static PlacePreviewMode placePreviewMode = new PlacePreviewMode();
		
		public static GrabberMode defaultMode = pixelMode;
		
		public final String name;
		public final String title;
		
		public GrabberMode(String name)
		{
			this.name = name;
			this.title = "grabber.mode." + name;
			modes.put(name, this);
		}
		
		public String getLocalizedName()
		{
			return I18n.translateToLocal(title);
		}
		
		@SideOnly(Side.CLIENT)
		public void onClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result)
		{
			GuiHandler.openGui("grabber", new NBTTagCompound(), player);
		}
		
		@SideOnly(Side.CLIENT)
		public abstract boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result);
		
		@SideOnly(Side.CLIENT)
		public abstract List<RenderCubeObject> getRenderingCubes(ItemStack stack);
		
		@SideOnly(Side.CLIENT)
		public abstract boolean renderBlockSeparately(ItemStack stack);
		
		@SideOnly(Side.CLIENT)
		public abstract SubGuiGrabber getGui(EntityPlayer player, ItemStack stack);
		public abstract SubContainerGrabber getContainer(EntityPlayer player, ItemStack stack);
		
		public abstract List<LittleTilePreview> getPreviews(ItemStack stack);
		public abstract void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state);
		public abstract void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, BlockPos pos, NBTTagCompound nbt);
		
	}
	
	public static class PixelMode extends GrabberMode {

		public PixelMode() {
			super("pixel");
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {
			IBlockState state = player.world.getBlockState(result.getBlockPos());
			if(SubContainerGrabber.isBlockValid(state.getBlock()))
			{
				PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
				return true;
			}
			else if(state.getBlock() instanceof BlockTile)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("secondMode", LittleAction.isUsingSecondMode(player));
				PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
				return true;
			}
			return false;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack)
		{
			return new SubGuiGrabber(this, stack, 140, 140)
			{
				public LittleTileSize size;
				public boolean isColored = false;
				
				@Override
				public void createControls() {
					super.createControls();
					LittleTilePreview preview = getPreview(stack);
					size = preview.box.getSize();
					
					controls.add(new GuiSteppedSlider("sizeX", 25, 30, 50, 14, size.sizeX, 1, LittleTile.gridSize));
					controls.add(new GuiSteppedSlider("sizeY", 25, 50, 50, 14, size.sizeY, 1, LittleTile.gridSize));
					controls.add(new GuiSteppedSlider("sizeZ", 25, 70, 50, 14, size.sizeZ, 1, LittleTile.gridSize));
					
					Color color = ColorUtils.IntToRGBA(preview.getColor());
					color.setAlpha(255);
					controls.add(new GuiColorPicker("picker", 0, 95, color));
					
					GuiAvatarLabel label = new GuiAvatarLabel("", 110, 40, 0, null);
					label.name = "avatar";
					label.height = 60;
					label.avatarSize = 32;
					controls.add(label);
					updateLabel();
				}
				
				public void updateLabel()
				{
					GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
					
					LittleTilePreview preview = getPreview(stack);
					
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					preview.setColor(ColorUtils.RGBAToInt(picker.color));
					preview.box.set(0, 0, 0, size.sizeX, size.sizeY, size.sizeZ);
					
					label.avatar = new AvatarItemStack(ItemBlockTiles.getStackFromPreview(preview));
				}
				
				@CustomEventSubscribe
				public void onSlotChange(SlotChangeEvent event)
				{
					ItemStack slotStack = container.getSlots().get(0).getStack();
					Block block = Block.getBlockFromItem(slotStack.getItem());
					if(block == LittleTiles.blockTile)
					{
						List<LittleTilePreview> previews = ((ILittleTile) slotStack.getItem()).getLittlePreview(slotStack);
						if(previews.size() > 0)
						{
							int colorInt = previews.get(0).getColor();
							Vec3i color = ColorUtils.IntToRGB(colorInt);
							if(colorInt == -1)
								color = new Vec3i(255, 255, 255);
							
							GuiColorPicker picker = (GuiColorPicker) get("picker");
							picker.color.set(color.getX(), color.getY(), color.getZ());
						}
					}
					updateLabel();
				}
				
				@CustomEventSubscribe
				public void onClick(GuiControlClickEvent event)
				{
					if(event.source.is("sliced"))
						updateLabel();
				}
				
				@CustomEventSubscribe
				public void onChange(GuiControlChangedEvent event)
				{
					size.sizeX = (int) ((GuiSteppedSlider) get("sizeX")).value;
					size.sizeY = (int) ((GuiSteppedSlider) get("sizeY")).value;
					size.sizeZ = (int) ((GuiSteppedSlider) get("sizeZ")).value;
					updateLabel();
				}

				@Override
				public void saveChanges() {
					LittleTilePreview preview = getPreview(stack);
					preview.box.set(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, size.sizeX, size.sizeY, size.sizeZ);
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					preview.setColor(ColorUtils.RGBAToInt(picker.color));
					setPreview(stack, preview);
				}
			};
		}
		
		@Override
		public SubContainerGrabber getContainer(EntityPlayer player, ItemStack stack) {
			return new SubContainerGrabber(player, stack);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes(ItemStack stack) {
			return Collections.emptyList();
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean renderBlockSeparately(ItemStack stack) {
			return true;
		}

		@Override
		public List<LittleTilePreview> getPreviews(ItemStack stack) {
			List<LittleTilePreview> previews = new ArrayList<>();
			previews.add(getPreview(stack));
			return previews;
		}

		@Override
		public void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state) {
			LittleTilePreview oldPreview = getPreview(stack);
			LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.box = oldPreview.box;
			setPreview(stack, tile.getPreviewTile());
		}

		@Override
		public void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack,
				BlockPos pos, NBTTagCompound nbt) {
			LittleTilePreview oldPreview = getPreview(stack);
			LittleTilePreview preview = tile.getPreviewTile();
			preview.box = oldPreview.box;
			setPreview(stack, preview);
		}
		
		public static LittleTilePreview getPreview(ItemStack stack)
		{
			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			LittleTilePreview preview = null;
			if(stack.getTagCompound().hasKey("preview"))
				preview = LittleTilePreview.loadPreviewFromNBT(stack.getTagCompound().getCompoundTag("preview"));
			else
			{
				IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
				LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileBlockColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound().getInteger("color")) : new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
				tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, 1, 1, 1);
				preview = tile.getPreviewTile();
				setPreview(stack, preview);
			}
			return preview;
		}
		
		public static void setPreview(ItemStack stack, LittleTilePreview preview)
		{
			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			NBTTagCompound nbt = new NBTTagCompound();	
			preview.writeToNBT(nbt);
			stack.getTagCompound().setTag("preview", nbt);
		}
		
	}
	
	public static class PlacePreviewMode extends GrabberMode {

		public PlacePreviewMode() {
			super("place_preview");
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onMouseWheelClickBlock(EntityPlayer player, ItemStack stack, RayTraceResult result) {
			IBlockState state = player.world.getBlockState(result.getBlockPos());
			if(SubContainerGrabber.isBlockValid(state.getBlock()))
			{
				PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
				return true;
			}
			else if(state.getBlock() instanceof BlockTile)
			{
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("secondMode", LittleAction.isUsingSecondMode(player));
				PacketHandler.sendPacketToServer(new LittleBlockPacket(result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
				return true;
			}
			return false;
		}
		
		@Override
		public void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state)
		{
			LittleTile tile = new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
			List<LittleTilePreview> previews = new ArrayList<>();
			previews.add(tile.getPreviewTile());
			PlacePreviewMode.setPreview(stack, previews);
		}
		
		@Override
		public void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, BlockPos pos, NBTTagCompound nbt)
		{
			List<LittleTilePreview> previews = new ArrayList<>();
			if(nbt.getBoolean("secondMode"))
			{
				for (LittleTile tileFromTE : te.getTiles()) {
					previews.add(tileFromTE.getPreviewTile());
				}
			}
			else
				previews.add(tile.getPreviewTile());
			ItemLittleGrabber.PlacePreviewMode.setPreview(stack, previews);
		}
		
		public static List<LittleTilePreview> getPreview(ItemStack stack)
		{
			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			List<LittleTilePreview> previews = LittleTilePreview.getPreview(stack);
			if(previews.size() == 0)
			{
				IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
				LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileBlockColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound().getInteger("color")) : new LittleTileBlock(state.getBlock(), state.getBlock().getMetaFromState(state));
				tile.box = new LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos, 1, 1, 1);
				LittleTilePreview preview = tile.getPreviewTile();
				
				previews.add(preview);
				setPreview(stack, previews);
			}
			
			return previews;
		}
		
		public static void setPreview(ItemStack stack, List<LittleTilePreview> previews)
		{
			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			LittleTilePreview.savePreviewTiles(previews, stack);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes(ItemStack stack) {
			return LittleTilePreview.getCubes(stack);
		}

		@Override
		@SideOnly(Side.CLIENT)
		public boolean renderBlockSeparately(ItemStack stack) {
			return false;
		}

		@Override
		@SideOnly(Side.CLIENT)
		public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack) {
			return new SubGuiGrabber(this, stack, 140, 140) {

				@Override
				public void saveChanges() {
					
				}
				
			};
		}

		@Override
		public SubContainerGrabber getContainer(EntityPlayer player, ItemStack stack) {
			return new SubContainerGrabber(player, stack);
		}

		@Override
		public List<LittleTilePreview> getPreviews(ItemStack stack) {
			return getPreview(stack);
		}
		
	}
}
