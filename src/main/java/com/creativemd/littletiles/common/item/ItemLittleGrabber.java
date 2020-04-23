package com.creativemd.littletiles.common.item;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.client.avatar.AvatarItemStack;
import com.creativemd.creativecore.client.rendering.RenderCubeObject;
import com.creativemd.creativecore.client.rendering.model.CreativeBakedModel;
import com.creativemd.creativecore.client.rendering.model.ICreativeRendered;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAvatarLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.gui.custom.GuiStackSelectorAll;
import com.creativemd.creativecore.common.gui.event.container.SlotChangeEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlClickEvent;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.utils.tooltip.TooltipUtils;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.LittleSubGuiUtils;
import com.creativemd.littletiles.client.gui.SubGuiGrabber;
import com.creativemd.littletiles.client.gui.configure.SubGuiConfigure;
import com.creativemd.littletiles.client.gui.configure.SubGuiModeSelector;
import com.creativemd.littletiles.common.action.LittleAction;
import com.creativemd.littletiles.common.action.block.LittleActionReplace;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.block.BlockTile;
import com.creativemd.littletiles.common.container.SubContainerConfigure;
import com.creativemd.littletiles.common.packet.LittleBlockPacket;
import com.creativemd.littletiles.common.packet.LittleBlockPacket.BlockPacketAction;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket;
import com.creativemd.littletiles.common.packet.LittleVanillaBlockPacket.VanillaBlockAction;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.LittleTileColored;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.math.vec.LittleVec;
import com.creativemd.littletiles.common.tile.preview.LittlePreview;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;
import com.creativemd.littletiles.common.tile.registry.LittleTileRegistry;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.util.grid.LittleGridContext;
import com.creativemd.littletiles.common.util.place.PlacementHelper;
import com.creativemd.littletiles.common.util.place.PlacementMode;
import com.creativemd.littletiles.common.util.place.PlacementPosition;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemLittleGrabber extends Item implements ICreativeRendered, ILittleTile {
	
	public ItemLittleGrabber() {
		setCreativeTab(LittleTiles.littleTab);
		hasSubtypes = true;
		setMaxStackSize(1);
	}
	
	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		return 0F;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		getMode(stack).addExtraInformation(stack.getTagCompound(), tooltip);
	}
	
	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public List<RenderCubeObject> getRenderingCubes(IBlockState state, TileEntity te, ItemStack stack) {
		return getMode(stack).getRenderingCubes(stack);
	}
	
	@SideOnly(Side.CLIENT)
	public static IBakedModel model;
	
	@Override
	@SideOnly(Side.CLIENT)
	public void applyCustomOpenGLHackery(ItemStack stack, TransformType cameraTransformType) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		
		model = mc.getRenderItem().getItemModelMesher().getModelManager().getModel(new ModelResourceLocation(LittleTiles.modid + ":grabber_background", "inventory"));
		ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
		
		mc.getRenderItem().renderItem(new ItemStack(Items.PAPER), model);
		
		if (cameraTransformType == TransformType.GUI) {
			GlStateManager.translate(0.1, 0.1, 0);
			GlStateManager.scale(0.7, 0.7, 0.7);
			
			GrabberMode mode = getMode(stack);
			if (mode.renderBlockSeparately(stack)) {
				LittlePreview preview = mode.getSeparateRenderingPreview(stack);
				ItemStack blockStack = new ItemStack(preview.getBlock(), 1, preview.getMeta());
				model = mc.getRenderItem().getItemModelWithOverrides(blockStack, mc.world, mc.player); // getItemModelMesher().getItemModel(blockStack);
				if (!(model instanceof CreativeBakedModel))
					ForgeHooksClient.handleCameraTransforms(model, cameraTransformType, false);
				
				GlStateManager.disableDepth();
				GlStateManager.pushMatrix();
				GlStateManager.translate(-0.5F, -0.5F, -0.5F);
				
				try {
					if (model.isBuiltInRenderer()) {
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
						GlStateManager.enableRescaleNormal();
						TileEntityItemStackRenderer.instance.renderByItem(blockStack);
					} else {
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
	public boolean hasLittlePreview(ItemStack stack) {
		return true;
	}
	
	@Override
	public LittlePreviews getLittlePreview(ItemStack stack) {
		return getMode(stack).getPreviews(stack);
	}
	
	@Override
	public void saveLittlePreview(ItemStack stack, LittlePreviews previews) {
		getMode(stack).setPreviews(previews, stack);
	}
	
	@Override
	public boolean containsIngredients(ItemStack stack) {
		return false;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void onClickAir(EntityPlayer player, ItemStack stack) {
		getMode(stack).onClickBlock(null, player, stack, null);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onClickBlock(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
		getMode(stack).onClickBlock(world, player, stack, result);
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, PlacementPosition position, RayTraceResult result) {
		return getMode(stack).onRightClick(world, player, stack, result);
	}
	
	@Override
	public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
		return getMode(stack).onMouseWheelClickBlock(world, player, stack, result);
	}
	
	@Override
	public PlacementMode getPlacementMode(ItemStack stack) {
		return ItemMultiTiles.currentMode;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUI(EntityPlayer player, ItemStack stack) {
		return ItemLittleGrabber.getMode(stack).getGui(player, stack, ((ILittleTile) stack.getItem()).getPositionContext(stack));
	}
	
	@Override
	public SubContainerConfigure getConfigureContainer(EntityPlayer player, ItemStack stack) {
		return ItemLittleGrabber.getMode(stack).getContainer(player, stack);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public SubGuiConfigure getConfigureGUIAdvanced(EntityPlayer player, ItemStack stack) {
		return new SubGuiModeSelector(stack, ItemMultiTiles.currentContext, ItemMultiTiles.currentMode) {
			
			@Override
			public void saveConfiguration(LittleGridContext context, PlacementMode mode) {
				ItemMultiTiles.currentContext = context;
				ItemMultiTiles.currentMode = mode;
			}
			
		};
	}
	
	@Override
	public LittleGridContext getPositionContext(ItemStack stack) {
		return ItemMultiTiles.currentContext;
	}
	
	public static GrabberMode getMode(String name) {
		GrabberMode mode = GrabberMode.modes.get(name);
		if (mode != null)
			return mode;
		return GrabberMode.defaultMode;
	}
	
	public static GrabberMode getMode(ItemStack stack) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		GrabberMode mode = GrabberMode.modes.get(stack.getTagCompound().getString("mode"));
		if (mode != null)
			return mode;
		return GrabberMode.defaultMode;
	}
	
	public static void setMode(ItemStack stack, GrabberMode mode) {
		if (!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		
		stack.getTagCompound().setString("mode", mode.name);
	}
	
	public static GrabberMode[] getModes() {
		return GrabberMode.modes.values().toArray(new GrabberMode[0]);
	}
	
	public static int indexOf(GrabberMode mode) {
		GrabberMode[] modes = getModes();
		for (int i = 0; i < modes.length; i++) {
			if (modes[i] == mode)
				return i;
		}
		return -1;
	}
	
	public static abstract class GrabberMode {
		
		public static HashMap<String, GrabberMode> modes = new LinkedHashMap<>();
		
		public static PixelMode pixelMode = new PixelMode();
		public static PlacePreviewMode placePreviewMode = new PlacePreviewMode();
		public static ReplaceMode replaceMode = new ReplaceMode();
		
		public static GrabberMode defaultMode = pixelMode;
		
		public final String name;
		public final String title;
		
		public GrabberMode(String name) {
			this.name = name;
			this.title = "grabber.mode." + name;
			modes.put(name, this);
		}
		
		public void addExtraInformation(NBTTagCompound nbt, List<String> tooltip) {
			
		}
		
		public String getLocalizedName() {
			return I18n.translateToLocal(title);
		}
		
		@SideOnly(Side.CLIENT)
		public void onClickBlock(@Nullable World world, EntityPlayer player, ItemStack stack, @Nullable RayTraceResult result) {
			
		}
		
		@SideOnly(Side.CLIENT)
		public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
			return true;
		}
		
		@SideOnly(Side.CLIENT)
		public abstract boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, RayTraceResult result);
		
		@SideOnly(Side.CLIENT)
		public abstract List<RenderCubeObject> getRenderingCubes(ItemStack stack);
		
		@SideOnly(Side.CLIENT)
		public abstract boolean renderBlockSeparately(ItemStack stack);
		
		@SideOnly(Side.CLIENT)
		public abstract SubGuiGrabber getGui(EntityPlayer player, ItemStack stack, LittleGridContext context);
		
		public abstract SubContainerConfigure getContainer(EntityPlayer player, ItemStack stack);
		
		public abstract LittlePreviews getPreviews(ItemStack stack);
		
		public abstract void setPreviews(LittlePreviews previews, ItemStack stack);
		
		public LittlePreview getSeparateRenderingPreview(ItemStack stack) {
			return getPreviews(stack).get(0);
		}
		
		public abstract void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state);
		
		public abstract void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, BlockPos pos, NBTTagCompound nbt);
		
	}
	
	public static abstract class SimpleMode extends GrabberMode {
		
		public SimpleMode(String name) {
			super(name);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
			IBlockState state = world.getBlockState(result.getBlockPos());
			if (LittleAction.isBlockValid(state)) {
				PacketHandler.sendPacketToServer(new LittleVanillaBlockPacket(result.getBlockPos(), VanillaBlockAction.GRABBER));
				return true;
			} else if (state.getBlock() instanceof BlockTile) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("secondMode", LittleAction.isUsingSecondMode(player));
				PacketHandler.sendPacketToServer(new LittleBlockPacket(world, result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
				return true;
			}
			return false;
		}
		
		public LittleGridContext getContext(ItemStack stack) {
			return LittleGridContext.get(stack.getTagCompound());
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
		public LittlePreviews getPreviews(ItemStack stack) {
			LittlePreviews previews = new LittlePreviews(getContext(stack));
			previews.addWithoutCheckingPreview(getPreview(stack));
			return previews;
		}
		
		@Override
		public void setPreviews(LittlePreviews previews, ItemStack stack) {
			setPreview(stack, previews.get(0));
		}
		
		@Override
		public void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state) {
			LittlePreview oldPreview = getPreview(stack);
			LittleTile tile = new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.box = oldPreview.box;
			setPreview(stack, tile.getPreviewTile());
		}
		
		@Override
		public void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, BlockPos pos, NBTTagCompound nbt) {
			LittlePreview oldPreview = getPreview(stack);
			LittlePreview preview = tile.getPreviewTile();
			preview.box = oldPreview.box;
			setPreview(stack, preview);
		}
		
		public static LittlePreview getPreview(ItemStack stack) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			if (stack.getTagCompound().hasKey("preview"))
				return getPreview(stack.getTagCompound());
			
			LittlePreview preview = getPreview(stack.getTagCompound()); // Old way
			setPreview(stack, preview);
			return preview;
		}
		
		public static LittlePreview getPreview(NBTTagCompound nbt) {
			if (nbt.hasKey("preview"))
				return LittleTileRegistry.loadPreview(nbt.getCompoundTag("preview"));
			
			IBlockState state = nbt.hasKey("state") ? Block.getStateById(nbt.getInteger("state")) : Blocks.STONE.getDefaultState();
			LittleTile tile = nbt.hasKey("color") ? new LittleTileColored(state.getBlock(), state.getBlock().getMetaFromState(state), nbt.getInteger("color")) : new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
			tile.box = new LittleBox(0, 0, 0, 1, 1, 1);
			return tile.getPreviewTile();
		}
		
		public static void setPreview(ItemStack stack, LittlePreview preview) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			NBTTagCompound nbt = new NBTTagCompound();
			preview.writeToNBT(nbt);
			stack.getTagCompound().setTag("preview", nbt);
		}
	}
	
	public static class PixelMode extends SimpleMode {
		
		public PixelMode() {
			super("pixel");
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack, LittleGridContext context) {
			return new SubGuiGrabber(this, stack, 140, 140, context) {
				public LittleVec size;
				public boolean isColored = false;
				
				@Override
				public void createControls() {
					super.createControls();
					LittleGridContext oldContext = LittleGridContext.get(stack.getTagCompound());
					LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(stack);
					preview.convertTo(oldContext, context);
					if (preview.box.minX == preview.box.maxX)
						preview.box.maxX++;
					if (preview.box.minY == preview.box.maxY)
						preview.box.maxY++;
					if (preview.box.minZ == preview.box.maxZ)
						preview.box.maxZ++;
					size = preview.box.getSize();
					
					controls.add(new GuiSteppedSlider("sizeX", 25, 20, 50, 10, size.x, 1, context.size));
					controls.add(new GuiSteppedSlider("sizeY", 25, 35, 50, 10, size.y, 1, context.size));
					controls.add(new GuiSteppedSlider("sizeZ", 25, 50, 50, 10, size.z, 1, context.size));
					
					Color color = ColorUtils.IntToRGBA(preview.getColor());
					controls.add(new GuiColorPicker("picker", 0, 70, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
					
					GuiAvatarLabel label = new GuiAvatarLabel("", 110, 20, 0, null);
					label.name = "avatar";
					label.height = 60;
					label.avatarSize = 32;
					controls.add(label);
					
					GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 120, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
					selector.setSelectedForce(preview.getBlockStack());
					controls.add(selector);
					
					updateLabel();
				}
				
				public LittlePreview getPreview(LittleGridContext context) {
					GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
					ItemStack selected = selector.getSelected();
					
					if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
						LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
						tile.box = new LittleBox(0, 0, 0, context.size, context.size, context.size);
						return tile.getPreviewTile();
					} else
						return ItemLittleGrabber.SimpleMode.getPreview(stack);
				}
				
				public void updateLabel() {
					GuiAvatarLabel label = (GuiAvatarLabel) get("avatar");
					
					LittlePreview preview = getPreview(context);
					
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					preview.setColor(ColorUtils.RGBAToInt(picker.color));
					preview.box.set(0, 0, 0, size.x, size.y, size.z);
					
					label.avatar = new AvatarItemStack(ItemBlockTiles.getStackFromPreview(context, preview));
				}
				
				@CustomEventSubscribe
				public void onSlotChange(SlotChangeEvent event) {
					ItemStack slotStack = container.getSlots().get(0).getStack();
					Block block = Block.getBlockFromItem(slotStack.getItem());
					if (block instanceof BlockTile) {
						LittlePreviews previews = ((ILittleTile) slotStack.getItem()).getLittlePreview(slotStack);
						if (previews.size() > 0) {
							int colorInt = previews.get(0).getColor();
							Vec3i color = ColorUtils.IntToRGB(colorInt);
							if (colorInt == -1)
								color = new Vec3i(255, 255, 255);
							
							GuiColorPicker picker = (GuiColorPicker) get("picker");
							picker.color.set(color.getX(), color.getY(), color.getZ());
						}
					}
					updateLabel();
				}
				
				@CustomEventSubscribe
				public void onClick(GuiControlClickEvent event) {
					if (event.source.is("sliced"))
						updateLabel();
				}
				
				@CustomEventSubscribe
				public void onChange(GuiControlChangedEvent event) {
					size.x = (int) ((GuiSteppedSlider) get("sizeX")).value;
					size.y = (int) ((GuiSteppedSlider) get("sizeY")).value;
					size.z = (int) ((GuiSteppedSlider) get("sizeZ")).value;
					updateLabel();
				}
				
				@Override
				public void saveConfiguration() {
					LittlePreview preview = getPreview(context);
					preview.box.set(0, 0, 0, size.x, size.y, size.z);
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					preview.setColor(ColorUtils.RGBAToInt(picker.color));
					setPreview(stack, preview);
					context.set(stack.getTagCompound());
				}
			};
		}
		
		@Override
		public SubContainerConfigure getContainer(EntityPlayer player, ItemStack stack) {
			return new SubContainerConfigure(player, stack);
		}
		
		@Override
		public void addExtraInformation(NBTTagCompound nbt, List<String> tooltip) {
			super.addExtraInformation(nbt, tooltip);
			LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(nbt);
			tooltip.add(TooltipUtils.printRGB(preview.hasColor() ? preview.getColor() : ColorUtils.WHITE));
		}
		
	}
	
	public static class PlacePreviewMode extends GrabberMode {
		
		public PlacePreviewMode() {
			super("place_preview");
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean onMouseWheelClickBlock(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
			IBlockState state = world.getBlockState(result.getBlockPos());
			if (state.getBlock() instanceof BlockTile) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setBoolean("secondMode", LittleAction.isUsingSecondMode(player));
				// nbt.setBoolean("add", GuiScreen.isCtrlKeyDown());
				PacketHandler.sendPacketToServer(new LittleBlockPacket(world, result.getBlockPos(), player, BlockPacketAction.GRABBER, nbt));
				return true;
			}
			return false;
		}
		
		@Override
		public void vanillaBlockAction(World world, ItemStack stack, BlockPos pos, IBlockState state) {
			/* LittleTile tile = new LittleTileBlock(state.getBlock(),
			 * state.getBlock().getMetaFromState(state)); tile.box = new
			 * LittleTileBox(LittleTile.minPos, LittleTile.minPos, LittleTile.minPos,
			 * LittleTile.gridSize, LittleTile.gridSize, LittleTile.gridSize);
			 * List<LittleTilePreview> previews = new ArrayList<>();
			 * previews.add(tile.getPreviewTile()); PlacePreviewMode.setPreview(stack,
			 * previews); */
		}
		
		@Override
		public void littleBlockAction(World world, TileEntityLittleTiles te, LittleTile tile, ItemStack stack, BlockPos pos, NBTTagCompound nbt) {
			LittlePreviews previews = new LittlePreviews(te.getContext());
			if (nbt.getBoolean("secondMode")) {
				for (LittleTile tileFromTE : te)
					previews.addWithoutCheckingPreview(tileFromTE.getPreviewTile());
			} else
				previews.addWithoutCheckingPreview(tile.getPreviewTile());
			ItemLittleGrabber.PlacePreviewMode.setPreview(stack, previews);
		}
		
		public static LittlePreviews getPreview(ItemStack stack) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			LittlePreviews previews = LittlePreview.getPreview(stack);
			if (previews.size() == 0) {
				IBlockState state = stack.getTagCompound().hasKey("state") ? Block.getStateById(stack.getTagCompound().getInteger("state")) : Blocks.STONE.getDefaultState();
				LittleTile tile = stack.getTagCompound().hasKey("color") ? new LittleTileColored(state.getBlock(), state.getBlock().getMetaFromState(state), stack.getTagCompound().getInteger("color")) : new LittleTile(state.getBlock(), state.getBlock().getMetaFromState(state));
				tile.box = new LittleBox(0, 0, 0, 1, 1, 1);
				LittlePreview preview = tile.getPreviewTile();
				
				previews.addWithoutCheckingPreview(preview);
				setPreview(stack, previews);
			}
			
			return previews;
		}
		
		public static void setPreview(ItemStack stack, LittlePreviews previews) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			LittlePreview.savePreview(previews, stack);
		}
		
		public static BlockPos getOrigin(ItemStack stack) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			return new BlockPos(stack.getTagCompound().getInteger("ox"), stack.getTagCompound().getInteger("oy"), stack.getTagCompound().getInteger("oz"));
		}
		
		public static void setOrigin(ItemStack stack, BlockPos pos) {
			if (!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());
			
			stack.getTagCompound().setInteger("ox", pos.getX());
			stack.getTagCompound().setInteger("oy", pos.getY());
			stack.getTagCompound().setInteger("oz", pos.getZ());
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public List<RenderCubeObject> getRenderingCubes(ItemStack stack) {
			return LittlePreview.getCubes(stack);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean renderBlockSeparately(ItemStack stack) {
			return false;
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack, LittleGridContext context) {
			return new SubGuiGrabber(this, stack, 140, 140, context) {
				
				@Override
				public void saveConfiguration() {
				
				}
				
			};
		}
		
		@Override
		public SubContainerConfigure getContainer(EntityPlayer player, ItemStack stack) {
			return new SubContainerConfigure(player, stack);
		}
		
		@Override
		public LittlePreviews getPreviews(ItemStack stack) {
			return getPreview(stack);
		}
		
		@Override
		public void setPreviews(LittlePreviews previews, ItemStack stack) {
			PlacePreviewMode.setPreview(stack, previews);
		}
		
	}
	
	public static class ReplaceMode extends SimpleMode {
		
		public ReplaceMode() {
			super("replace");
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public SubGuiGrabber getGui(EntityPlayer player, ItemStack stack, LittleGridContext context) {
			return new SubGuiGrabber(this, stack, 140, 140, context) {
				
				@Override
				public void createControls() {
					LittlePreview preview = ItemLittleGrabber.SimpleMode.getPreview(stack);
					
					Color color = ColorUtils.IntToRGBA(preview.getColor());
					controls.add(new GuiColorPicker("picker", 0, 70, color, LittleTiles.CONFIG.isTransparencyEnabled(getPlayer()), LittleTiles.CONFIG.getMinimumTransparency(getPlayer())));
					
					GuiStackSelectorAll selector = new GuiStackSelectorAll("preview", 0, 120, 112, getPlayer(), LittleSubGuiUtils.getCollector(getPlayer()), true);
					selector.setSelectedForce(preview.getBlockStack());
					controls.add(selector);
					super.createControls();
				}
				
				public LittlePreview getPreview(LittleGridContext context) {
					GuiStackSelectorAll selector = (GuiStackSelectorAll) get("preview");
					ItemStack selected = selector.getSelected();
					
					if (!selected.isEmpty() && selected.getItem() instanceof ItemBlock) {
						LittleTile tile = new LittleTile(((ItemBlock) selected.getItem()).getBlock(), selected.getMetadata());
						tile.box = new LittleBox(0, 0, 0, 1, 1, 1);
						return tile.getPreviewTile();
					} else
						return ItemLittleGrabber.SimpleMode.getPreview(stack);
				}
				
				@Override
				public void saveConfiguration() {
					LittlePreview preview = getPreview(context);
					GuiColorPicker picker = (GuiColorPicker) get("picker");
					preview.setColor(ColorUtils.RGBAToInt(picker.color));
					if (stack.getTagCompound().hasKey("preview")) {
						LittlePreview oldPreview = LittleTileRegistry.loadPreview(stack.getTagCompound().getCompoundTag("preview"));
						if (oldPreview != null)
							preview.box = oldPreview.box;
					}
					setPreview(stack, preview);
					context.set(stack.getTagCompound());
				}
			};
		}
		
		@Override
		public SubContainerConfigure getContainer(EntityPlayer player, ItemStack stack) {
			return new SubContainerConfigure(player, stack);
		}
		
		@Override
		public LittlePreviews getPreviews(ItemStack stack) {
			return new LittlePreviews(LittleGridContext.get());
		}
		
		@Override
		public boolean onRightClick(World world, EntityPlayer player, ItemStack stack, RayTraceResult result) {
			if (PlacementHelper.canBlockBeUsed(world, result.getBlockPos()))
				new LittleActionReplace(world, result.getBlockPos(), player, getPreview(stack)).execute();
			return false;
		}
		
		@Override
		public LittlePreview getSeparateRenderingPreview(ItemStack stack) {
			return getPreview(stack);
		}
		
	}
}
