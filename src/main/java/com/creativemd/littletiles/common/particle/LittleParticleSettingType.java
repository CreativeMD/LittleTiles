package com.creativemd.littletiles.common.particle;

import net.minecraft.util.math.Vec3d;

public enum LittleParticleSettingType {
	
	NONE {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(par1, par2, par3);
		}
	},
	MOTION {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(randomizeAnyDirection(par1), randomizeAnyDirection(par2), randomizeAnyDirection(par3));
		}
	},
	MOTION_WITHOUT_Y {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(randomizeAnyDirection(par1), 0, randomizeAnyDirection(par3));
		}
	},
	MOTION_XY_OPTION {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(par1, par2, par3);
		}
	},
	SIZE {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(par1 * Math.random(), par2, par3);
		}
	},
	COLOR {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(par1 * Math.random(), par2 * Math.random(), par3 * Math.random());
		}
	},
	COLOR_RED_OFFSET {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(par1 * Math.random(), par2 * Math.random(), par3 * Math.random());
		}
	},
	FIRST_COLOR {
		@Override
		public Vec3d randomize(float par1, float par2, float par3) {
			return new Vec3d(randomizeAnyDirection(par1), par2, par3);
		}
	};
	
	public abstract Vec3d randomize(float par1, float par2, float par3);
	
	public static double randomizeAnyDirection(double number) {
		return Math.random() * number * 2 - number;
	}
	
}