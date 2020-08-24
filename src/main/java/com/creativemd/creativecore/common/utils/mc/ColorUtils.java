package com.creativemd.creativecore.common.utils.mc;

import org.lwjgl.util.Color;

import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class ColorUtils {
	
	public static enum ColorPart {
		RED {
			@Override
			public int getColor(Color color) {
				return color.getRed();
			}
			
			@Override
			public void setColor(Color color, int intenstiy) {
				color.setRed(intenstiy);
			}
			
			@Override
			public int getBrightest() {
				return 0xFF0000;
			}
		},
		GREEN {
			@Override
			public int getColor(Color color) {
				return color.getGreen();
			}
			
			@Override
			public void setColor(Color color, int intenstiy) {
				color.setGreen(intenstiy);
			}
			
			@Override
			public int getBrightest() {
				return 0x00FF00;
			}
		},
		BLUE {
			@Override
			public int getColor(Color color) {
				return color.getBlue();
			}
			
			@Override
			public void setColor(Color color, int intenstiy) {
				color.setBlue(intenstiy);
			}
			
			@Override
			public int getBrightest() {
				return 0x0000FF;
			}
		},
		ALPHA {
			@Override
			public int getColor(Color color) {
				return color.getAlpha();
			}
			
			@Override
			public void setColor(Color color, int intenstiy) {
				color.setAlpha(intenstiy);
			}
			
			@Override
			public int getBrightest() {
				return 0x000000FF;
			}
		},
		SHADE {
			@Override
			public int getColor(Color color) {
				return color.getBlue();
			}
			
			@Override
			public void setColor(Color color, int intenstiy) {
			}
			
			@Override
			public int getBrightest() {
				return 0xFFFFFFFF;
			}
		};
		
		public abstract int getColor(Color color);
		
		public abstract void setColor(Color color, int intenstiy);
		
		public abstract int getBrightest();
	}
	
	public static final int WHITE = -1;
	public static final int RED = -65536;
	public static final int GREEN = -16711936;
	public static final int BLUE = -16776961;
	public static final int LIGHT_BLUE = -16740609;
	public static final int ORANGE = -23296;
	public static final int YELLOW = -256;
	public static final int CYAN = 16711681;
	public static final int MAGENTA = -65281;
	public static final int BLACK = -16777216;
	
	public static float getAlphaDecimal(int color) {
		return (color >> 24 & 255) / 255F;
	}
	
	public static float getRedDecimal(int color) {
		return (color >> 16 & 255) / 255F;
	}
	
	public static float getGreenDecimal(int color) {
		return (color >> 8 & 255) / 255F;
	}
	
	public static float getBlueDecimal(int color) {
		return (color & 255) / 255F;
	}
	
	public static int getAlpha(int color) {
		return color >> 24 & 255;
	}
	
	public static int getRed(int color) {
		return color >> 16 & 255;
	}
	
	public static int getGreen(int color) {
		return color >> 8 & 255;
	}
	
	public static int getBlue(int color) {
		return color & 255;
	}
	
	public static int RGBAToInt(int red, int green, int blue, int alpha) {
		return (alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8 | blue & 255;
	}
	
	public static int RGBAToInt(Color color) {
		return (color.getAlpha() & 255) << 24 | (color.getRed() & 255) << 16 | (color.getGreen() & 255) << 8 | color.getBlue() & 255;
	}
	
	public static Color IntToRGBA(int color) {
		int a = color >> 24 & 255;
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		return new Color(r, g, b, a);
	}
	
	public static int RGBToInt(Vec3i color) {
		return (255 & 255) << 24 | (color.getX() & 255) << 16 | (color.getY() & 255) << 8 | color.getZ() & 255;
	}
	
	public static Vec3i IntToRGB(int color) {
		float r = color >> 16 & 255;
		float g = color >> 8 & 255;
		float b = color & 255;
		return new Vec3i(r, g, b);
	}
	
	public static Vec3d IntToVec(int color) {
		float r = color >> 16 & 255;
		float g = color >> 8 & 255;
		float b = color & 255;
		return new Vec3d(r / 255F, g / 255F, b / 255F);
	}
	
	public static int VecToInt(Vec3d color) {
		return RGBToInt(new Vec3i(color.x * 255, color.y * 255, color.z * 255));
	}
	
	public static Vec3i colorToVec(Color color) {
		return new Vec3i(color.getRed(), color.getGreen(), color.getBlue());
	}
	
	public static boolean isWhite(int color) {
		int r = color >> 16 & 255;
		int g = color >> 8 & 255;
		int b = color & 255;
		return r == 255 && g == 255 && b == 255;
	}
	
	public static boolean isTransparent(int color) {
		int a = color >> 24 & 255;
		return a < 255;
	}
	
	public static boolean isInvisible(int color) {
		int a = color >> 24 & 255;
		return a == 0;
	}
	
	public static int blend(int i1, int i2) {
		return blend(i1, i2, 0.5F);
	}
	
	public static int blend(int i1, int i2, float ratio) {
		if (ratio > 1f)
			ratio = 1f;
		else if (ratio < 0f)
			ratio = 0f;
		float iRatio = 1.0f - ratio;
		
		int a1 = (i1 >> 24 & 0xff);
		int r1 = ((i1 & 0xff0000) >> 16);
		int g1 = ((i1 & 0xff00) >> 8);
		int b1 = (i1 & 0xff);
		
		int a2 = (i2 >> 24 & 0xff);
		int r2 = ((i2 & 0xff0000) >> 16);
		int g2 = ((i2 & 0xff00) >> 8);
		int b2 = (i2 & 0xff);
		
		int a = (int) ((a1 * iRatio) + (a2 * ratio));
		int r = (int) ((r1 * iRatio) + (r2 * ratio));
		int g = (int) ((g1 * iRatio) + (g2 * ratio));
		int b = (int) ((b1 * iRatio) + (b2 * ratio));
		
		return a << 24 | r << 16 | g << 8 | b;
	}
}
