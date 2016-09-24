package com.creativemd.littletiles.common.gui;

import java.util.ArrayList;
import java.util.Arrays;

import com.creativemd.creativecore.gui.container.SubGui;
import com.creativemd.creativecore.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.gui.controls.gui.GuiListBox;
import com.creativemd.creativecore.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.gui.controls.gui.GuiTextfield;
import com.creativemd.littletiles.common.tileentity.TileEntityParticle;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;

public class SubGuiParticle extends SubGui {
	
	public TileEntityParticle particle;
	
	public SubGuiParticle(TileEntityParticle particle) {
		this.particle = particle;
	}

	@Override
	public void createControls() {
		ArrayList<String> types = new ArrayList<>();//new ArrayList<>(EnumParticleTypes.getParticleNames());
		types.addAll(Arrays.asList(new String[]{"flame","splash","mobSpell","endRod","mobSpellAmbient","damageIndicator","crit","fireworksSpark","magicCrit","enchantmenttable","heart","snowballpoof","dragonbreath","smoke","largesmoke","instantSpell","spell","sweepAttack","dripWater","note","lava","largeexplode","hugeexplosion","bubble","townaura","slime","barrier","depthsuspend","dripLava","droplet","angryVillager","happyVillager","witchMagic","mobappearance","reddust"}));
		GuiComboBox comboBox = new GuiComboBox("list", 0, 0, 140, types);
		comboBox.select(particle.particle);
		controls.add(comboBox);
		
		controls.add(new GuiAnalogeSlider("par1", 0, 20, 100, 16, particle.par1, 0, 1));
		controls.add(new GuiAnalogeSlider("par2", 0, 40, 100, 16, particle.par2, 0, 1));
		controls.add(new GuiAnalogeSlider("par3", 0, 60, 100, 16, particle.par3, 0, 1));
		/*controls.add(new GuiTextfield("par1", ""+particle.par1, 0, 20, 40, 16).setFloatOnly());
		controls.add(new GuiTextfield("par2", ""+particle.par2, 0, 40, 40, 16).setFloatOnly());
		controls.add(new GuiTextfield("par3", ""+particle.par3, 0, 60, 40, 16).setFloatOnly());*/
		
		controls.add(new GuiButton("Save", 100, 100) {
			
			@Override
			public void onClicked(int x, int y, int button) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("particle", ((GuiComboBox) get("list")).caption);
				nbt.setFloat("par1", ((GuiAnalogeSlider) get("par1")).value);
				nbt.setFloat("par2", ((GuiAnalogeSlider) get("par2")).value);
				nbt.setFloat("par3", ((GuiAnalogeSlider) get("par3")).value);
				sendPacketToServer(nbt);
			}
		});
	}

}
