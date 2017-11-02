package com.creativemd.littletiles.common.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum LittleParticleType {
	
	flame(LittleParticleSettingType.MOTION),
	splash(LittleParticleSettingType.MOTION_WITHOUT_Y),
	mobSpell(LittleParticleSettingType.COLOR),
	endRod(LittleParticleSettingType.MOTION),
	mobSpellAmbient(LittleParticleSettingType.COLOR),
	damageIndicator(LittleParticleSettingType.MOTION),
	crit(LittleParticleSettingType.MOTION),
	fireworksSpark(LittleParticleSettingType.MOTION),
	magicCrit(LittleParticleSettingType.MOTION),
	enchantmenttable(LittleParticleSettingType.MOTION),
	heart(LittleParticleSettingType.NONE),
	snowballpoof(LittleParticleSettingType.NONE),
	dragonbreath(LittleParticleSettingType.MOTION),
	smoke(LittleParticleSettingType.MOTION),
	largesmoke(LittleParticleSettingType.MOTION),
	instantSpell(LittleParticleSettingType.MOTION_XY_OPTION),
	spell(LittleParticleSettingType.MOTION_XY_OPTION),
	sweepAttack(LittleParticleSettingType.SIZE),
	note(LittleParticleSettingType.FIRST_COLOR),
	lava(LittleParticleSettingType.NONE),
	largeexplode(LittleParticleSettingType.SIZE),
	hugeexplosion(LittleParticleSettingType.NONE),
	slime(LittleParticleSettingType.NONE),
	barrier(LittleParticleSettingType.NONE),
	depthsuspend(LittleParticleSettingType.NONE),
	angryVillager(LittleParticleSettingType.NONE),
	happyVillager(LittleParticleSettingType.NONE),
	witchMagic(LittleParticleSettingType.MOTION_XY_OPTION),
	mobappearance(LittleParticleSettingType.NONE),
	reddust(LittleParticleSettingType.COLOR_RED_OFFSET),
	dripWater(LittleParticleSettingType.MOTION, 0, true),
	dripLava(LittleParticleSettingType.MOTION, 0, true);
	
	public static LittleParticleType byName(String name)
	{
		for (LittleParticleType type : LittleParticleType.values()) {
			if(type.name().equalsIgnoreCase(name))
				return type;
		}
		return LittleParticleType.smoke;
	}
	
	@SideOnly(Side.CLIENT)
	public static void initClient()
	{
		for (LittleParticleType type : LittleParticleType.values()) {
			if(!type.isModded)
				type.particleType = EnumParticleTypes.getByName(type.name());
		}
		
		dripWater.factory = new ParticleLittleDrip.WaterFactory();
		dripLava.factory = new ParticleLittleDrip.LavaFactory();
	}
	
	public final LittleParticleSettingType type;
	
	public final boolean isModded;
	public final int subID;
	public final boolean spawnBelow;
	
	@SideOnly(Side.CLIENT)
	public IParticleFactory factory;
	
	@SideOnly(Side.CLIENT)
	public EnumParticleTypes particleType;
	
	LittleParticleType(LittleParticleSettingType type)
	{
		this.type = type;
		this.isModded = false;
		this.subID = -1;
		this.spawnBelow = false;
	}
	
	LittleParticleType(LittleParticleSettingType type, int id, boolean spawnBelow)
	{
		this.type = type;
		this.isModded = true;
		this.subID = id;
		this.spawnBelow = spawnBelow;
	}
	
}
