package com.creativemd.littletiles.common.tileentity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.common.tileentity.TileEntityCreative;
import com.creativemd.creativecore.common.world.WorldFake;
import com.creativemd.littletiles.common.items.ItemLittleWrench;
import com.creativemd.littletiles.common.particles.LittleParticleType;
import com.creativemd.littletiles.common.tiles.LittleTile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityParticle extends TileEntityCreative implements ITickable {
	
	public LittleTile tile;
	
	public LittleParticleType particle = LittleParticleType.smoke;
	public float par1 = 0;
	public float par2 = 0;
	public float par3 = 0;
	
	public float ageModifier = 1;
	
	/**Amount of particles per tick**/
	public float speed = 1;
	
	public int ticksToWait = 0;
	
	public boolean randomize = false;
	
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
		nbt.setString("particle", particle.name());
		nbt.setFloat("par1", par1);
		nbt.setFloat("par2", par2);
		nbt.setFloat("par3", par3);
		
		nbt.setFloat("speed", speed);
		nbt.setBoolean("randomize", randomize);
		
		nbt.setFloat("age", ageModifier);
	}
	
	@Override
	public void receiveUpdatePacket(NBTTagCompound nbt)
	{
		particle = LittleParticleType.byName(nbt.getString("particle"));
		par1 = nbt.getFloat("par1");
		par2 = nbt.getFloat("par2");
		par3 = nbt.getFloat("par3");
		
		speed = nbt.getFloat("speed");
		ticksToWait = 0;
		randomize = nbt.getBoolean("randomize");
		
		ageModifier = Math.max(0.1F, nbt.getFloat("age"));
	}
	
	@SideOnly(Side.CLIENT)
	public boolean shouldSpawnParticles()
	{
		return !(Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench);
	}

	@Override
	public void update() {
		if(isClientSide())
		{
			Vec3d offset = new Vec3d(0.5, 1, 0.5);
			if(tile != null)
				offset = tile.box.getMinVec().getVec().addVector(LittleTile.gridMCLength/2, particle.spawnBelow ? -LittleTile.gridMCLength*2 : LittleTile.gridMCLength, LittleTile.gridMCLength/2);
				
			if(speed >= 1)
			{
				if(shouldSpawnParticles())
				{
					for (int i = 0; i < speed; i++) {
						spawnParticle(offset);
					}
				}
			}else if(ticksToWait == 0){
				if(shouldSpawnParticles())
					spawnParticle(offset);
				ticksToWait = (int) Math.ceil(1/speed);
			}else
				ticksToWait--;
			
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void spawnParticle(Vec3d offset)
	{
		Vec3d additional = new Vec3d(par1, par2, par3);
		if(randomize)
			additional = particle.type.randomize(par1, par2, par3);
		
		Minecraft mc = Minecraft.getMinecraft();
		
		if(spawnParticle0 == null)
			spawnParticle0 = ReflectionHelper.findMethod(RenderGlobal.class, "spawnParticle0", "func_190571_b", int.class, boolean.class, boolean.class, double.class, double.class, double.class, double.class, double.class, double.class, int[].class);
		
		if(particleMaxAge == null)
			particleMaxAge = ReflectionHelper.findField(Particle.class, "particleMaxAge", "field_70547_e");
		
		try {
			Vector3d pos = new Vector3d(getPos().getX()+offset.xCoord, getPos().getY()+offset.yCoord, getPos().getZ()+offset.zCoord);
			if(world instanceof WorldFake)
			{
				pos = ((WorldFake) world).getRotatedVector(pos);
			}
			
			Particle particleEntity;
			
			if(particle.isModded)
			{
				particleEntity = particle.factory.createParticle(0, mc.world, pos.x, pos.y, pos.z, additional.xCoord, additional.yCoord, additional.zCoord, new int[]{});
				mc.effectRenderer.addEffect(particleEntity);
			}else
				particleEntity = (Particle) spawnParticle0.invoke(mc.renderGlobal, particle.particleType.getParticleID(), true, false, pos.x, pos.y, pos.z, additional.xCoord, additional.yCoord, additional.zCoord, new int[]{});
			
			if(particleEntity != null)
				particleEntity.setMaxAge(Math.max(1, (int) (particleMaxAge.getInt(particleEntity)*ageModifier)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SideOnly(Side.CLIENT)
	private Method spawnParticle0;
	
	@SideOnly(Side.CLIENT)
	private Field particleMaxAge;
}
