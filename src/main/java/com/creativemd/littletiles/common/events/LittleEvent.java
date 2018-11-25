package com.creativemd.littletiles.common.events;

import java.lang.reflect.Field;
import java.util.Iterator;

import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.opener.GuiHandler;
import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.client.LittleTilesClient;
import com.creativemd.littletiles.client.render.ItemModelCache;
import com.creativemd.littletiles.client.render.PreviewRenderer;
import com.creativemd.littletiles.common.action.block.LittleActionPlaceStack;
import com.creativemd.littletiles.common.action.tool.LittleActionGlowstone;
import com.creativemd.littletiles.common.api.ILittleTile;
import com.creativemd.littletiles.common.api.ISpecialBlockSelector;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.entity.EntityAnimation;
import com.creativemd.littletiles.common.entity.EntityDoorAnimation;
import com.creativemd.littletiles.common.packet.LittleEntityRequestPacket;
import com.creativemd.littletiles.common.packet.LittleFlipPacket;
import com.creativemd.littletiles.common.packet.LittleRotatePacket;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.type.LittleBed;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.tiles.LittleTile;
import com.creativemd.littletiles.common.tiles.LittleTileBlockColored;
import com.creativemd.littletiles.common.tiles.vec.LittleBoxes;
import com.creativemd.littletiles.common.tiles.vec.LittleTilePos;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper;
import com.creativemd.littletiles.common.utils.placing.PlacementHelper.PositionResult;
import com.creativemd.littletiles.common.utils.placing.PlacementMode;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer.SleepResult;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.SleepingLocationCheckEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleEvent {
	
	@SubscribeEvent
	public void trackEntity(StartTracking event) {
		if (event.getTarget() instanceof EntityDoorAnimation && ((EntityDoorAnimation) event.getTarget()).activator != event.getEntityPlayer()) {
			EntityDoorAnimation animation = (EntityDoorAnimation) event.getTarget();
			PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation.writeToNBT(new NBTTagCompound()), true), (EntityPlayerMP) event.getEntityPlayer());
		}
	}
	
	public static boolean cancelNext = false;
	
	public static ItemStack lastSelectedItem = null;
	public static ISpecialBlockSelector blockSelector = null;
	public static ILittleTile iLittleTile = null;
	
	@SideOnly(Side.CLIENT)
	private boolean leftClicked;
	
	@SideOnly(Side.CLIENT)
	public static boolean onMouseWheelClick(RayTraceResult result, EntityPlayer player, World world) {
		if (result.typeOfHit == Type.BLOCK) {
			ItemStack stack = player.getHeldItemMainhand();
			ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
			if (iTile != null)
				return iTile.onMouseWheelClickBlock(player, stack, result);
		}
		return false;
	}
	
	@SubscribeEvent
	public void onLeftClickAir(LeftClickEmpty event) {
		if (event.getWorld().isRemote) {
			ItemStack stack = event.getItemStack();
			ILittleTile iLittleTile = PlacementHelper.getLittleInterface(stack);
			
			if (iLittleTile != null)
				iLittleTile.onClickAir(event.getEntityPlayer(), stack);
		}
	}
	
	@SubscribeEvent
	public void onLeftClick(LeftClickBlock event) {
		if (event.getWorld().isRemote) {
			if (!leftClicked) {
				ItemStack stack = event.getItemStack();
				RayTraceResult ray = new RayTraceResult(event.getHitVec(), event.getFace(), event.getPos());
				if (lastSelectedItem != null && lastSelectedItem.getItem() != stack.getItem()) {
					if (blockSelector != null) {
						blockSelector.onDeselect(event.getWorld(), lastSelectedItem, event.getEntityPlayer());
						blockSelector = null;
					}
					
					if (iLittleTile != null) {
						iLittleTile.onDeselect(event.getEntityPlayer(), lastSelectedItem);
						iLittleTile = null;
					}
					
					lastSelectedItem = null;
				}
				
				if (stack.getItem() instanceof ISpecialBlockSelector) {
					if (((ISpecialBlockSelector) stack.getItem()).onClickBlock(event.getWorld(), stack, event.getEntityPlayer(), ray, new LittleTilePos(ray, ((ISpecialBlockSelector) stack.getItem()).getContext(stack))))
						;
					event.setCanceled(true);
					blockSelector = (ISpecialBlockSelector) stack.getItem();
					lastSelectedItem = stack;
				}
				
				iLittleTile = PlacementHelper.getLittleInterface(stack);
				
				if (iLittleTile != null) {
					if (iLittleTile.onClickBlock(event.getEntityPlayer(), stack, getPosition(event.getWorld(), iLittleTile, stack, ray), ray))
						event.setCanceled(true);
					lastSelectedItem = stack;
				}
				
				leftClicked = true;
			}
		} else if (event.getItemStack().getItem() instanceof ISpecialBlockSelector)
			event.setCanceled(true);
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderOverlay(RenderBlockOverlayEvent event) {
		if (event.getOverlayType() == OverlayType.WATER) {
			EntityPlayer player = event.getPlayer();
			double d0 = player.posY + (double) player.getEyeHeight();
			BlockPos blockpos = new BlockPos(player.posX, d0, player.posZ);
			TileEntity te = player.world.getTileEntity(blockpos);
			if (te instanceof TileEntityLittleTiles) {
				AxisAlignedBB bb = player.getEntityBoundingBox();
				for (LittleTile tile : ((TileEntityLittleTiles) te).getTiles()) {
					if (tile instanceof LittleTileBlockColored && tile.isMaterial(Material.WATER) && tile.box.getBox(tile.getContext(), blockpos).intersects(bb)) {
						Vec3d color = ColorUtils.IntToVec(((LittleTileBlockColored) tile).color);
						// GlStateManager.color((float) color.x, (float) color.y, (float) color.z);
						Minecraft mc = Minecraft.getMinecraft();
						mc.getTextureManager().bindTexture(new ResourceLocation("textures/misc/underwater.png"));
						Tessellator tessellator = Tessellator.getInstance();
						BufferBuilder bufferbuilder = tessellator.getBuffer();
						float f = mc.player.getBrightness();
						GlStateManager.color(f * (float) color.x, f * (float) color.y, f * (float) color.z, 0.5F);
						GlStateManager.enableBlend();
						// GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
						// GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
						// GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
						GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
						GlStateManager.pushMatrix();
						float f1 = 4.0F;
						float f2 = -1.0F;
						float f3 = 1.0F;
						float f4 = -1.0F;
						float f5 = 1.0F;
						float f6 = -0.5F;
						float f7 = -mc.player.rotationYaw / 64.0F;
						float f8 = mc.player.rotationPitch / 64.0F;
						bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
						bufferbuilder.pos(-1.0D, -1.0D, -0.5D).tex((double) (4.0F + f7), (double) (4.0F + f8)).endVertex();
						bufferbuilder.pos(1.0D, -1.0D, -0.5D).tex((double) (0.0F + f7), (double) (4.0F + f8)).endVertex();
						bufferbuilder.pos(1.0D, 1.0D, -0.5D).tex((double) (0.0F + f7), (double) (0.0F + f8)).endVertex();
						bufferbuilder.pos(-1.0D, 1.0D, -0.5D).tex((double) (4.0F + f7), (double) (0.0F + f8)).endVertex();
						tessellator.draw();
						
						GlStateManager.popMatrix();
						GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
						GlStateManager.disableBlend();
						event.setCanceled(true);
						return;
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void breakSpeed(BreakSpeed event) {
		ItemStack stack = event.getEntityPlayer().getHeldItemMainhand();
		if (stack.getItem() instanceof ISpecialBlockSelector)
			event.setNewSpeed(0);
	}
	
	@SubscribeEvent
	public void onInteract(RightClickBlock event) {
		if (cancelNext) {
			cancelNext = false;
			event.setCanceled(true);
			return;
		}
		
		ItemStack stack = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
		if (event.getWorld().isRemote && event.getHand() == EnumHand.MAIN_HAND && stack.getItem() == Items.GLOWSTONE_DUST && event.getEntityPlayer().isSneaking()) {
			BlockTile.TEResult te = BlockTile.loadTeAndTile(event.getEntityPlayer().world, event.getPos(), event.getEntityPlayer());
			if (te.isComplete()) {
				new LittleActionGlowstone(event.getPos(), event.getEntityPlayer()).execute();
				event.setCanceled(true);
			}
		}
		
		ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
		
		if (iTile != null) {
			if (event.getHand() == EnumHand.MAIN_HAND && event.getWorld().isRemote)
				onRightInteractClient(iTile, event.getEntityPlayer(), event.getHand(), event.getWorld(), stack, event.getPos(), event.getFace());
			event.setCanceled(true);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static PositionResult getPosition(World world, ILittleTile iTile, ItemStack stack, RayTraceResult result) {
		return PreviewRenderer.marked != null ? PreviewRenderer.marked.position.copy() : PlacementHelper.getPosition(world, result, iTile.getPositionContext(stack));
	}
	
	@SideOnly(Side.CLIENT)
	public void onRightInteractClient(ILittleTile iTile, EntityPlayer player, EnumHand hand, World world, ItemStack stack, BlockPos pos, EnumFacing facing) {
		PositionResult position = getPosition(world, iTile, stack, Minecraft.getMinecraft().objectMouseOver);
		if (iTile.onRightClick(player, stack, position.copy(), Minecraft.getMinecraft().objectMouseOver)) {
			if (!stack.isEmpty() && player.canPlayerEdit(pos, facing, stack)) {
				PlacementMode mode = iTile.getPlacementMode(stack).place();
				new LittleActionPlaceStack(stack, iTile.getLittlePreview(stack, false, PreviewRenderer.marked != null), position, PreviewRenderer.isCentered(player, iTile), PreviewRenderer.isFixed(player, iTile), mode).execute();
				
				PreviewRenderer.marked = null;
			}
			iTile.onDeselect(player, stack);
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void drawHighlight(DrawBlockHighlightEvent event) {
		if (event.getTarget().typeOfHit == Type.BLOCK) {
			EntityPlayer player = event.getPlayer();
			World world = event.getPlayer().getEntityWorld();
			BlockPos pos = event.getTarget().getBlockPos();
			IBlockState state = world.getBlockState(pos);
			ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
			if (stack.getItem() instanceof ISpecialBlockSelector) {
				ISpecialBlockSelector selector = (ISpecialBlockSelector) stack.getItem();
				LittleTilePos result = new LittleTilePos(event.getTarget(), selector.getContext(stack));
				if (selector.hasCustomBox(world, stack, player, state, event.getTarget(), result)) {
					while (LittleTilesClient.flip.isPressed()) {
						int i4 = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
						EnumFacing direction = null;
						switch (i4) {
						case 0:
							direction = EnumFacing.SOUTH;
							break;
						case 1:
							direction = EnumFacing.WEST;
							break;
						case 2:
							direction = EnumFacing.NORTH;
							break;
						case 3:
							direction = EnumFacing.EAST;
							break;
						}
						if (player.rotationPitch > 45)
							direction = EnumFacing.DOWN;
						if (player.rotationPitch < -45)
							direction = EnumFacing.UP;
						
						LittleFlipPacket packet = new LittleFlipPacket(direction.getAxis());
						packet.executeClient(player);
						PacketHandler.sendPacketToServer(packet);
					}
					
					// Rotate Block
					while (LittleTilesClient.up.isPressed())
						processRotateKey(player, Rotation.Z_CLOCKWISE);
					
					while (LittleTilesClient.down.isPressed())
						processRotateKey(player, Rotation.Z_COUNTER_CLOCKWISE);
					
					while (LittleTilesClient.right.isPressed())
						processRotateKey(player, Rotation.Y_COUNTER_CLOCKWISE);
					
					while (LittleTilesClient.left.isPressed())
						processRotateKey(player, Rotation.Y_CLOCKWISE);
					
					double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
					double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
					double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
					LittleBoxes boxes = ((ISpecialBlockSelector) stack.getItem()).getBox(world, stack, player, event.getTarget(), result);
					// box.addOffset(new LittleTileVec(pos));
					
					GlStateManager.enableBlend();
					GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
					GlStateManager.glLineWidth(4.0F);
					GlStateManager.disableTexture2D();
					GlStateManager.depthMask(false);
					for (int i = 0; i < boxes.size(); i++) {
						RenderGlobal.drawSelectionBoundingBox(boxes.get(i).getBox(boxes.context).offset(boxes.pos).grow(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
					}
					
					if (state.getMaterial() != Material.AIR && world.getWorldBorder().contains(pos)) {
						GlStateManager.glLineWidth(1.0F);
						RenderGlobal.drawSelectionBoundingBox(state.getSelectedBoundingBox(world, pos).grow(0.0020000000949949026D).offset(-d0, -d1, -d2), 0.0F, 0.0F, 0.0F, 0.4F);
					}
					
					GlStateManager.depthMask(true);
					GlStateManager.enableTexture2D();
					GlStateManager.disableBlend();
					
					event.setCanceled(true);
				}
			}
		}
	}
	
	public void processRotateKey(EntityPlayer player, Rotation rotation) {
		LittleRotatePacket packet = new LittleRotatePacket(rotation);
		packet.executeClient(player);
		PacketHandler.sendPacketToServer(packet);
	}
	
	@SubscribeEvent
	public void isSleepingLocationAllowed(SleepingLocationCheckEvent event) {
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if (bed instanceof LittleBed && ((LittleBed) bed).sleepingPlayer == event.getEntityPlayer())
				event.setResult(Result.ALLOW);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onPlayerLogout(PlayerLoggedOutEvent event) {
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.player);
			if (bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.player, null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SubscribeEvent
	public void onWakeUp(PlayerWakeUpEvent event) {
		try {
			LittleStructure bed = (LittleStructure) LittleBed.littleBed.get(event.getEntityPlayer());
			if (bed instanceof LittleBed)
				((LittleBed) bed).sleepingPlayer = null;
			LittleBed.littleBed.set(event.getEntityPlayer(), null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPlayerSleep(PlayerSleepInBedEvent event) {
		if (event.getEntityPlayer().world.getBlockState(event.getPos()).getBlock() instanceof BlockTile) {
			TileEntityLittleTiles te = BlockTile.loadTe(event.getEntityPlayer().world, event.getPos());
			if (te != null) {
				for (Iterator iterator = te.getTiles().iterator(); iterator.hasNext();) {
					LittleTile tile = (LittleTile) iterator.next();
					if (!tile.isConnectedToStructure())
						continue;
					
					LittleStructure structure = tile.connection.getStructure(tile.te.getWorld());
					
					if (structure instanceof LittleBed && ((LittleBed) structure).hasBeenActivated) {
						((LittleBed) structure).trySleep(event.getEntityPlayer(), structure.getHighestCenterVec());
						event.setResult(SleepResult.OK);
						((LittleBed) structure).hasBeenActivated = false;
						return;
					}
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static int transparencySortingIndex;
	
	private static Field prevRenderSortX;
	private static Field prevRenderSortY;
	private static Field prevRenderSortZ;
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onRenderTick(RenderTickEvent event) {
		if (event.phase == Phase.START) {
			Minecraft mc = Minecraft.getMinecraft();
			
			if (mc.player != null && mc.renderGlobal != null) {
				if (prevRenderSortX == null) {
					prevRenderSortX = ReflectionHelper.findField(RenderGlobal.class, "prevRenderSortX", "field_147596_f");
					prevRenderSortY = ReflectionHelper.findField(RenderGlobal.class, "prevRenderSortY", "field_147597_g");
					prevRenderSortZ = ReflectionHelper.findField(RenderGlobal.class, "prevRenderSortZ", "field_147602_h");
				}
				
				Entity entityIn = mc.getRenderViewEntity();
				if (entityIn == null)
					return;
				try {
					double d0 = entityIn.posX - prevRenderSortX.getDouble(mc.renderGlobal);
					double d1 = entityIn.posY - prevRenderSortY.getDouble(mc.renderGlobal);
					double d2 = entityIn.posZ - prevRenderSortZ.getDouble(mc.renderGlobal);
					if (d0 * d0 + d1 * d1 + d2 * d2 > 1.0D)
						transparencySortingIndex++;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onClientTick(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			Minecraft mc = Minecraft.getMinecraft();
			
			ItemModelCache.tick();
			
			if (leftClicked && !mc.gameSettings.keyBindAttack.isKeyDown()) {
				leftClicked = false;
			}
			
			if (mc.player != null) {
				ItemStack stack = mc.player.getHeldItemMainhand();
				
				if (lastSelectedItem != null && lastSelectedItem.getItem() != stack.getItem()) {
					if (blockSelector != null) {
						blockSelector.onDeselect(mc.world, lastSelectedItem, mc.player);
						blockSelector = null;
					}
					
					if (iLittleTile != null) {
						iLittleTile.onDeselect(mc.player, lastSelectedItem);
						iLittleTile = null;
					}
					
					lastSelectedItem = null;
				}
				
				while (LittleTilesClient.configure.isPressed()) {
					ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
					if (iTile != null) {
						SubGui gui = iTile.getConfigureGUI(mc.player, stack);
						if (gui != null)
							GuiHandler.openGui("configure", new NBTTagCompound());
					} else if (stack.getItem() instanceof ISpecialBlockSelector) {
						SubGui gui = ((ISpecialBlockSelector) stack.getItem()).getConfigureGUI(mc.player, stack);
						if (gui != null)
							GuiHandler.openGui("configure", new NBTTagCompound());
					}
				}
				
				while (LittleTilesClient.configureAdvanced.isPressed()) {
					ILittleTile iTile = PlacementHelper.getLittleInterface(stack);
					if (iTile != null) {
						SubGui gui = iTile.getConfigureGUIAdvanced(mc.player, stack);
						if (gui != null)
							GuiHandler.openGui("configureadvanced", new NBTTagCompound());
					} else if (stack.getItem() instanceof ISpecialBlockSelector) {
						SubGui gui = ((ISpecialBlockSelector) stack.getItem()).getConfigureGUIAdvanced(mc.player, stack);
						if (gui != null)
							GuiHandler.openGui("configureadvanced", new NBTTagCompound());
					}
				}
			}
		}
	}
	
	private static Field entitiesById = ReflectionHelper.findField(World.class, "entitiesById", "field_175729_l");
	
	@SideOnly(Side.CLIENT)
	public static boolean cancelEntitySpawn(WorldClient world, int entityID, Entity entity) {
		if (entity instanceof EntityAnimation) {
			((EntityAnimation) entity).addDoor();
			
			if (((EntityAnimation) entity).spawnedInWorld) {
				entity.setEntityId(entityID);
				try {
					((IntHashMap<Entity>) entitiesById.get(world)).addKey(entityID, entity);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
				return true;
			}
			((EntityAnimation) entity).spawnedInWorld = true;
			return false;
		}
		return false;
	}
}
