package com.creativemd.littletiles.common.particles;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

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
	reddust(LittleParticleSettingType.COLOR_RED_OFFSET);
	
	public final LittleParticleSettingType type;
	
	LittleParticleType(LittleParticleSettingType type)
	{
		this.type = type;
	}
	
}
