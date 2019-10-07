package com.creativemd.littletiles.common.tiles;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileTE extends LittleTileBlock {
	
	public LittleTileTE() {
		super();
	}
	
	public LittleTileTE(Block block, int meta, TileEntity tileEntity) {
		super(block, meta);
		this.tileEntity = tileEntity;
		setMeta(meta);
	}
	
	public boolean firstSended = false;
	
	private TileEntity tileEntity;
	
	private boolean isTileEntityLoaded = false;
	
	public TileEntity getTileEntity() {
		if (!isTileEntityLoaded && tileEntity != null) {
			tileEntity.setWorld(te.getWorld());
			setTEMetadata(getMeta());
			isTileEntityLoaded = true;
		}
		return tileEntity;
	}
	
	public void setTileEntity(TileEntity tileEntity) {
		this.tileEntity = tileEntity;
	}
	
	private static Field metadataField = ReflectionHelper.findField(TileEntity.class, new String[] { "blockMetadata", "field_145847_g" });
	
	protected void setTEMetadata(int meta) {
		try {
			metadataField.setInt(tileEntity, meta);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setMeta(int meta) {
		super.setMeta(meta);
		if (tileEntity != null)
			setTEMetadata(meta);
	}
	
	@Override
	public boolean supportsUpdatePacket() {
		return true;
	}
	
	@Override
	public NBTTagCompound getUpdateNBT() {
		tileEntity.setWorld(te.getWorld());
		return ReflectionHelper.getPrivateValue(SPacketUpdateTileEntity.class, getTileEntity().getUpdatePacket(), "nbt", "field_148860_e");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void receivePacket(NBTTagCompound nbt, NetworkManager net) {
		tileEntity.onDataPacket(net, new SPacketUpdateTileEntity(tileEntity.getPos(), getMeta(), nbt));
	}
	
	@Override
	public boolean canBeSplitted() {
		return false;
	}
	
	@Override
	public void loadTileExtra(NBTTagCompound nbt) {
		super.loadTileExtra(nbt);
		NBTTagCompound tileNBT = nbt.getCompoundTag("tileEntity");
		if (tileNBT != null) {
			tileEntity = TileEntity.create(te.getWorld(), tileNBT);
			setMeta(getMeta());
			tileEntity.setWorld(te.getWorld());
			tileEntity.setPos(new BlockPos(te.getPos()));
			isTileEntityLoaded = te.getWorld() != null;
			// if(tileEntity.isInvalid())
			// setInValid();
		}
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt) {
		super.saveTileExtra(nbt);
		if (tileEntity != null) {
			NBTTagCompound tileNBT = new NBTTagCompound();
			tileEntity.writeToNBT(tileNBT);
			nbt.setTag("tileEntity", tileNBT);
		}
	}
	
	@Override
	public boolean shouldTick() {
		return true;
	}
	
	@Override
	public void updateEntity() {
		if (tileEntity != null) {
			if (tileEntity.getWorld() == null) {
				tileEntity.setWorld(te.getWorld());
				isTileEntityLoaded = true;
			}
			if (tileEntity instanceof ITickable)
				((ITickable) tileEntity).update();
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderTick(double x, double y, double z, float partialTickTime) {
		if (tileEntity != null) {
			Minecraft mc = Minecraft.getMinecraft();
			if (te.getDistanceSq(mc.player.posX, mc.player.posY, mc.player.posZ) < getTileEntity().getMaxRenderDistanceSquared()) {
				RenderHelper.enableStandardItemLighting();
				
				int i = te.getWorld().getCombinedLight(te.getPos(), 0);
				int j = i % 65536;
				int k = i / 65536;
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				BlockPos blockpos = te.getPos();
				
				renderTileEntity(x, y, z, partialTickTime);
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void renderTileEntity(double x, double y, double z, float partialTickTime) {
		
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		if (tileEntity != null)
			return getTileEntity().getMaxRenderDistanceSquared();
		return super.getMaxRenderDistanceSquared();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (tileEntity != null)
			return getTileEntity().getRenderBoundingBox().offset(getContext().toVanillaGrid(box.minX), getContext().toVanillaGrid(box.minY), getContext().toVanillaGrid(box.minZ));
		return super.getRenderBoundingBox();
	}
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if (tile instanceof LittleTileTE) {
			LittleTileTE thisTile = (LittleTileTE) tile;
			thisTile.tileEntity = TileEntity.create(te.getWorld(), getTileEntity().writeToNBT(new NBTTagCompound()));
			thisTile.isTileEntityLoaded = false;
		}
	}
	
	@Override
	protected boolean canSawResize(EnumFacing direction, EntityPlayer player) {
		return false;
	}
	
	@Override
	public boolean canBeCombined(LittleTile tile) {
		if (super.canBeCombined(tile))
			return tile instanceof LittleTileTE && getTileEntity() == ((LittleTileTE) tile).getTileEntity();
		return false;
	}
	
	@Override
	public boolean canBeConvertedToVanilla() {
		return false;
	}
}
