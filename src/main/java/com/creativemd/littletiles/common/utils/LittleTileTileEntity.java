package com.creativemd.littletiles.common.utils;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleTileTileEntity extends LittleTileBlock{
	
	public LittleTileTileEntity()
	{
		super();
	}
	
	public LittleTileTileEntity(Block block, int meta, TileEntity tileEntity)
	{
		super(block, meta);
		this.tileEntity = tileEntity;
	}
	
	public boolean firstSended = false;
	
	public TileEntity tileEntity;
	
	/**All information the client needs*/
	@Override
	public void updatePacket(NBTTagCompound nbt)
	{
		super.updatePacket(nbt);
		if(!firstSended)
		{
			firstSended = true;
			NBTTagCompound nbtTag = new NBTTagCompound();
			tileEntity.writeToNBT(nbtTag);
			nbt.setTag("tileentity", nbtTag);
			nbt.setBoolean("isFirst", true);
		}else{
			SPacketUpdateTileEntity packet = tileEntity.getUpdatePacket();
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				packet.writePacketData(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
			int x = buffer.readInt();
	        int y = buffer.readShort();
	        int z = buffer.readInt();
	        int meta = buffer.readUnsignedByte();
	        NBTTagCompound newNBT = null;
	        try {
				newNBT = buffer.readNBTTagCompoundFromBuffer();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        if(newNBT != null)
	        	nbt.setTag("tileentity", newNBT);
		}
	}
	
	/**Should apply all information from sendToCLient**/
	@Override
	@SideOnly(Side.CLIENT)
	public void receivePacket(NBTTagCompound nbt, NetworkManager net)
	{
		super.receivePacket(nbt, net);
		if(nbt.getBoolean("isFirst"))
		{
			tileEntity = TileEntity.func_190200_a(te.getWorld(), nbt.getCompoundTag("tileentity"));
		}else{
			NBTTagCompound tileNBT = nbt.getCompoundTag("tileentity");
			if(tileEntity != null)
				tileEntity.onDataPacket(net, new SPacketUpdateTileEntity(tileEntity.getPos(), meta, tileNBT));
		}
	}
	
	
	@Override
	public void loadTileExtra(NBTTagCompound nbt)
	{
		super.loadTileExtra(nbt);
		NBTTagCompound tileNBT = nbt.getCompoundTag("tileEntity");
		if(tileNBT != null)
		{
			tileEntity = TileEntity.func_190200_a(te.getWorld(), tileNBT);
			//tileEntity.setWorldObj(te.getWorld());
			tileEntity.setPos(new BlockPos(te.getPos()));
			//if(tileEntity.isInvalid())
				//setInValid();
		}
	}
	
	@Override
	public void saveTileExtra(NBTTagCompound nbt)
	{
		super.saveTileExtra(nbt);
		if(tileEntity != null)
		{
			NBTTagCompound tileNBT = new NBTTagCompound();
			tileEntity.writeToNBT(tileNBT);
			nbt.setTag("tileEntity", tileNBT);
		}
	}
	
	@Override
	public void updateEntity()
	{
		if(tileEntity != null)
		{
			if(tileEntity.getWorld() == null)
				tileEntity.setWorldObj(te.getWorld());
			if(tileEntity instanceof ITickable)
				((ITickable) tileEntity).update();
		}
	}
	
	/*@Override
	@SideOnly(Side.CLIENT)
	public void renderTick(double x, double y, double z, float partialTickTime) {
		if(tileEntity != null)
		{
			Minecraft mc = Minecraft.getMinecraft();
			if (te.getDistanceSq(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ) < tileEntity.getMaxRenderDistanceSquared())
	        {
	            int i = te.getWorld().getLightBrightnessForSkyBlocks(te.getPos(), 0);
	            int j = i % 65536;
	            int k = i / 65536;
	            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
	            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	            double posX = (double)te.xCoord - TileEntityRendererDispatcher.staticPlayerX;
	            double posY = (double)te.yCoord - TileEntityRendererDispatcher.staticPlayerY;
	            double posZ = (double)te.zCoord - TileEntityRendererDispatcher.staticPlayerZ;
	            
	            posX += cornerVec.getPosX() - 0.5;
	            posY += cornerVec.getPosY() - 0.5;
	            posZ += cornerVec.getPosZ() - 0.5;
	            
	            tileEntity.blockMetadata = meta;
	            
	            TileEntityRendererDispatcher.instance.renderTileEntityAt(tileEntity, posX, posY, posZ, partialTickTime);
	        }
		}
	}*/
	
	@Override
	@SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared()
    {
		if(tileEntity != null)
			return tileEntity.getMaxRenderDistanceSquared();
		return super.getMaxRenderDistanceSquared();
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
		if(tileEntity != null)
			return tileEntity.getRenderBoundingBox().offset(cornerVec.getPosX(), cornerVec.getPosY(), cornerVec.getPosZ());
		return super.getRenderBoundingBox();
    }
	
	@Override
	public void copyExtra(LittleTile tile) {
		super.copyExtra(tile);
		if(tile instanceof LittleTileTileEntity)
		{
			LittleTileTileEntity thisTile = (LittleTileTileEntity) tile;
			thisTile.tileEntity = tileEntity;
		}
	}
	
	public boolean loadTileEntity()
	{
		if(tileEntity != null && tileEntity.getWorld() != null)
		{
			TileEntity Tempte = tileEntity.getWorld().getTileEntity(tileEntity.getPos());
			if(Tempte instanceof TileEntityLittleTiles)
			{
				te = (TileEntityLittleTiles) Tempte;
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected boolean canSawResize(EnumFacing direction, EntityPlayer player) {
		return false;
	}
	
	@Override
	public boolean canBeCombined(LittleTile tile) {
		if(super.canBeCombined(tile))
			return tile instanceof LittleTileTileEntity && tileEntity == ((LittleTileTileEntity) tile).tileEntity;
		return false;
	}
}
