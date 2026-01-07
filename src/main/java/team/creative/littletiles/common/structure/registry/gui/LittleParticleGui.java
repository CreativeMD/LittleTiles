package team.creative.littletiles.common.structure.registry.gui;

import team.creative.littletiles.common.gui.structure.GuiParticle.GuiParticleControl;
import team.creative.littletiles.common.gui.tool.blueprint.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructure;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpread;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadRandom;

public class LittleParticleGui extends LittleStructureGuiControl {
    
    public GuiParticleControl control;
    
    public LittleParticleGui(LittleStructureGui gui, GuiTreeItemStructure item) {
        super(gui, item);
    }
    
    @Override
    public void create(LittleStructure structure) {
        ParticleSpread spread;
        ParticleSettings settings;
        int count;
        int delay;
        boolean locked;
        if (structure instanceof LittleParticleEmitter p) {
            spread = p.spread;
            settings = p.settings;
            count = p.count;
            delay = p.delay;
            locked = p.locked;
        } else {
            spread = new ParticleSpreadRandom();
            settings = new ParticleSettings();
            count = 1;
            delay = 10;
            locked = false;
        }
        
        add(control = new GuiParticleControl(spread, settings, count, delay, locked));
    }
    
    @Override
    public LittleStructure save(LittleStructure structure) {
        LittleParticleEmitter particle = (LittleParticleEmitter) structure;
        
        if (item.group.getStructureTag() != null)
            structure.load(item.group.getStructureTag(), item.provider()); // Make sure other fields are like direction
            
        particle.count = control.count.getValue();
        particle.delay = control.delay.getValue();
        
        particle.spread = control.saveSpread();
        
        ParticleSettings newSettings = new ParticleSettings();
        newSettings.randomColor = control.randomColor.value;
        newSettings.collision = control.collision.value;
        newSettings.texture = control.textureBox.selected();
        newSettings.lifetime = control.age.getIntValue();
        newSettings.lifetimeDeviation = control.ageDiv.getValue();
        newSettings.color = control.color.color.toInt();
        newSettings.gravity = (float) control.gravity.getValue();
        newSettings.startSize = (float) control.sizeStart.getValue();
        newSettings.endSize = (float) control.sizeEnd.getValue();
        newSettings.sizeDeviation = (float) control.sizeDiv.getValue();
        particle.settings = newSettings;
        particle.locked = control.locked.value;
        
        return particle;
    }
    
}
