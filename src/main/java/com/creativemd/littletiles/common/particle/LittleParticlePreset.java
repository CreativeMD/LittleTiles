package com.creativemd.littletiles.common.particle;

import com.creativemd.creativecore.common.gui.GuiControl;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;

public enum LittleParticlePreset {
	
	smoke {
		@Override
		public void populate(ParticleSettings settings) {
			settings.gravity = 0;
			settings.color = ColorUtils.RGBAToInt(120, 120, 120, 255);
			settings.growrate = 0.001F;
			settings.texture = LittleParticleTexture.dust_fade_out;
			settings.size = 0.4F;
			settings.lifetime = 40;
		}
		
	};
	
	public final ParticleSettings settings;
	
	private LittleParticlePreset() {
		this.settings = new ParticleSettings();
		this.populate(settings);
	}
	
	public abstract void populate(ParticleSettings settings);
	
	public String translatedName() {
		return GuiControl.translateOrDefault("particle.preset." + name(), name());
	}
	
}
