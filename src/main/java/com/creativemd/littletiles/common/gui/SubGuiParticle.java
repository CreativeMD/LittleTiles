package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.utils.ColorUtils;
import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.gui.controls.gui.GuiColorPlate;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.littletiles.common.particles.LittleParticleSettingType;
import com.creativemd.littletiles.common.particles.LittleParticleType;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class SubGuiParticle extends SubGui {
	
	public TileEntityParticle particle;
	
	public SubGuiParticle(TileEntityParticle particle) {
		this.particle = particle;
	}

	@Override
	public void createControls() {
		ArrayList<String> types = new ArrayList<>();//new ArrayList<>(EnumParticleTypes.getParticleNames());
		//types.addAll(Arrays.asList(new String[]{"flame","splash","mobSpell","endRod","mobSpellAmbient","damageIndicator","crit","fireworksSpark","magicCrit","enchantmenttable","heart","snowballpoof","dragonbreath","smoke","largesmoke","instantSpell","spell","sweepAttack","dripWater","note","lava","largeexplode","hugeexplosion","bubble","townaura","slime","barrier","depthsuspend","dripLava","droplet","angryVillager","happyVillager","witchMagic","mobappearance","reddust"}));
		for (int i = 0; i < LittleParticleType.values().length; i++) {
			types.add(LittleParticleType.values()[i].name());
		}
		
		GuiComboBox comboBox = new GuiComboBox("list", 17, 0, 136, types);
		comboBox.select(particle.particle.name());
		controls.add(comboBox);
		controls.add(new GuiButton("<<", 0, 0) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int index = comboBox.index-1;
				if(index < 0)
					index = comboBox.lines.size()-1;
				comboBox.select(comboBox.lines.get(index));
			}
		});
		
		controls.add(new GuiButton(">>", 160, 0) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				int index = comboBox.index+1;
				if(index >= comboBox.lines.size())
					index = 0;
				comboBox.select(comboBox.lines.get(index));
			}
		});		
		
		controls.add(new GuiLabel("labelSpeed", "Speed (particles/tick:", 0, 82));
		controls.add(new GuiTextfield("speed", "" + particle.speed, 120, 80, 40, 12).setFloatOnly());
		controls.add(new GuiCheckBox("randomize", 0, 100, particle.randomize));
		
		
		controls.add(new GuiAnalogeSlider("age", 0, 120, 100, 10, particle.ageModifier, 0.1F, 10));
		
		controls.add(new GuiButton("Save", 145, 145) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("particle", ((GuiComboBox) get("list")).caption);
				LittleParticleType type = LittleParticleType.valueOf(((GuiComboBox) get("list")).caption);
				if(type != null)
				{
					switch (type.type) {
					case COLOR:
					case COLOR_RED_OFFSET:
						GuiColorPlate plate = (GuiColorPlate) get("color");
						Color color = plate.getColor();
						nbt.setFloat("par1", color.getRed()/255F);
						nbt.setFloat("par2", color.getGreen()/255F);
						nbt.setFloat("par3", color.getBlue()/255F);
						break;
					case FIRST_COLOR:
						nbt.setFloat("par1", ((GuiAnalogeSlider) get("color")).value);
						break;
					case MOTION:
					case MOTION_WITHOUT_Y:
						nbt.setFloat("par1", ((GuiAnalogeSlider) get("par1")).value);
						if(type.type == LittleParticleSettingType.MOTION_WITHOUT_Y)
							nbt.setFloat("par2", 0);
						else
							nbt.setFloat("par2", ((GuiAnalogeSlider) get("par2")).value);
						nbt.setFloat("par3", ((GuiAnalogeSlider) get("par3")).value);
						break;
					case MOTION_XY_OPTION:
						if(((GuiCheckBox) get("spread")).value)
						{
							nbt.setFloat("par1", 1);
							nbt.setFloat("par2", 1);
							nbt.setFloat("par3", 1);
						}
						break;
					case NONE:
						break;
					case SIZE:
						nbt.setFloat("par1", 2-((GuiAnalogeSlider) get("size")).value);
						break;
					default:
						break;
					
					
					}
				}
				
				
				nbt.setFloat("speed", Float.parseFloat(((GuiTextfield) get("speed")).text));
				
				nbt.setBoolean("randomize", ((GuiCheckBox) get("randomize")).value);
				
				nbt.setFloat("age", ((GuiAnalogeSlider) get("age")).value);
				
				sendPacketToServer(nbt);
			}
		});
		loadParticleSettings();
	}
	
	@CustomEventSubscribe
	public void onParticleChange(GuiControlChangedEvent event)
	{
		if(event.source.is("list"))
		{
			removeControls("<<", "list", ">>", "randomize", "age", "Save", "labelSpeed", "speed");
			loadParticleSettings();
		}else if(event.source.is("colorR", "colorG", "colorB")){
			((GuiColorPlate) get("color")).setColor(new Vec3i(((GuiAnalogeSlider) get("colorR")).value * 255, ((GuiAnalogeSlider) get("colorG")).value * 255, ((GuiAnalogeSlider) get("colorB")).value * 255)); 
		}
	}
	
	public void loadParticleSettings()
	{
		LittleParticleType type = LittleParticleType.valueOf(((GuiComboBox) get("list")).caption);
		if(type != null)
		{
			switch (type.type) {
				case COLOR:
				case COLOR_RED_OFFSET:
					controls.add(new GuiAnalogeSlider("colorR", 0, 25, 100, 10, particle.par1, 0, 1));
					controls.add(new GuiAnalogeSlider("colorG", 0, 42, 100, 10, particle.par2, 0, 1));
					controls.add(new GuiAnalogeSlider("colorB", 0, 59, 100, 10, particle.par3, 0, 1));
					controls.add(new GuiColorPlate("color", 120, 25, 20, 20, ColorUtils.IntToRGB(ColorUtils.VecToInt(new Vec3d(particle.par1, particle.par2, particle.par3)))));
					break;
				case FIRST_COLOR:
					controls.add(new GuiAnalogeSlider("color", 0, 25, 100, 10, particle.par1, 0, 1));
					break;
				case MOTION:
				case MOTION_WITHOUT_Y:
					controls.add(new GuiAnalogeSlider("par1", 0, 25, 100, 10, particle.par1, -1, 1));
					if(type.type != LittleParticleSettingType.MOTION_WITHOUT_Y)
						controls.add(new GuiAnalogeSlider("par2", 0, 42, 100, 10, particle.par2, -1, 1));
					controls.add(new GuiAnalogeSlider("par3", 0, 59, 100, 10, particle.par3, -1, 1));
					break;
				case MOTION_XY_OPTION:
					controls.add(new GuiCheckBox("spread", 0, 25, particle.par1 != 0 || particle.par3 != 0));
					break;
				case NONE:
					break;
				case SIZE:
					controls.add(new GuiAnalogeSlider("size", 0, 25, 100, 10, 2-particle.par1, 0, 2));
					break;
			}
			
		}
		refreshControls();
	}

}
