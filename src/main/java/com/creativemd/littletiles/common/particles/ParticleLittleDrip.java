package com.creativemd.littletiles.common.particles;

import com.creativemd.littletiles.common.blocks.BlockTile;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleLittleDrip extends Particle {
	/** the material type for dropped items/blocks */
	private final Material materialType;
	/** The height of the current bob */
	private int bobTimer;
	
	protected ParticleLittleDrip(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double speedX, double speedY, double speedZ, Material p_i1203_8_) {
		super(worldIn, xCoordIn, yCoordIn, zCoordIn, 0.0D, 0.0D, 0.0D);
		if (p_i1203_8_ == Material.WATER) {
			this.particleRed = 0.0F;
			this.particleGreen = 0.0F;
			this.particleBlue = 1.0F;
		} else {
			this.particleRed = 1.0F;
			this.particleGreen = 0.0F;
			this.particleBlue = 0.0F;
		}
		
		this.setSize(0.001F, 0.001F);
		setPosition(xCoordIn, yCoordIn, zCoordIn);
		this.particleGravity = 0.02F;
		this.materialType = p_i1203_8_;
		this.bobTimer = 0;
		this.motionX = speedX;
		this.motionY = speedY;
		this.motionZ = speedZ;
		this.particleMaxAge = (int) (64.0D / (Math.random() * 0.8D + 0.2D));
		this.setParticleTextureIndex(112);
		// this.onUpdate();
	}
	
	public int getBrightnessForRender(float p_189214_1_) {
		return this.materialType == Material.WATER ? super.getBrightnessForRender(p_189214_1_) : 257;
	}
	
	public void onUpdate() {
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		
		if (this.materialType == Material.WATER) {
			this.particleRed = 0.2F;
			this.particleGreen = 0.3F;
			this.particleBlue = 1.0F;
		} else {
			this.particleRed = 1.0F;
			this.particleGreen = 16.0F / (float) (40 - this.bobTimer + 16);
			this.particleBlue = 4.0F / (float) (40 - this.bobTimer + 8);
		}
		
		this.motionY -= (double) this.particleGravity;
		
		if (this.bobTimer-- > 0) {
			this.motionX *= 0.02D;
			this.motionY *= 0.02D;
			this.motionZ *= 0.02D;
			this.setParticleTextureIndex(113);
		} else {
			this.setParticleTextureIndex(112);
		}
		
		this.move(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;
		
		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		}
		
		if (this.onGround) {
			if (this.materialType == Material.WATER) {
				this.setExpired();
				this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
			} else {
				this.setParticleTextureIndex(114);
			}
			
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
		}
		
		BlockPos blockpos = new BlockPos(this.posX, this.posY, this.posZ);
		IBlockState iblockstate = this.world.getBlockState(blockpos);
		Material material = iblockstate.getMaterial();
		
		if ((material.isLiquid() || material.isSolid()) && !(iblockstate.getBlock() instanceof BlockTile)) {
			double d0 = 0.0D;
			
			if (iblockstate.getBlock() instanceof BlockLiquid) {
				d0 = (double) BlockLiquid.getLiquidHeightPercent(((Integer) iblockstate.getValue(BlockLiquid.LEVEL)).intValue());
			}
			
			double d1 = (double) (MathHelper.floor(this.posY) + 1) - d0;
			
			if (this.posY < d1) {
				this.setExpired();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class LavaFactory implements IParticleFactory {
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleLittleDrip(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Material.LAVA);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public static class WaterFactory implements IParticleFactory {
		public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleLittleDrip(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, Material.WATER);
		}
	}
}