package com.creativemd.littletiles.common.tileentity;

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityParticle extends TileEntityCreative implements ITickable {
	
	public String particle = "";
	public float par1 = 0;
	public float par2 = 0;
	public float par3 = 0;
	
	@SideOnly(Side.CLIENT)
	public EnumParticleTypes particleType;
	
	@Override
	public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        receiveUpdatePacket(compound);
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
		super.writeToNBT(compound);
		getDescriptionNBT(compound);
		return compound;
    }
	
	@Override
	public void getDescriptionNBT(NBTTagCompound nbt)
	{
		nbt.setString("particle", particle);
		nbt.setFloat("par1", par1);
		nbt.setFloat("par2", par2);
		nbt.setFloat("par3", par3);
	}
	
	@Override
	public void receiveUpdatePacket(NBTTagCompound nbt)
	{
		particle = nbt.getString("particle");
		par1 = nbt.getFloat("par1");
		par2 = nbt.getFloat("par2");
		par3 = nbt.getFloat("par3");
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			particleType = null;
	}

	@Override
	public void update() {
		if(worldObj.isRemote)
		{
			if(particleType == null)
			{
				particleType = EnumParticleTypes.getByName(particle);
				if(particleType == null)
					particleType = EnumParticleTypes.SMOKE_NORMAL;
			}
			
			worldObj.spawnParticle(particleType, getPos().getX()+0.5, getPos().getY()+1, getPos().getZ()+0.5, par1, par2, par3);
			
		}
	}
}
