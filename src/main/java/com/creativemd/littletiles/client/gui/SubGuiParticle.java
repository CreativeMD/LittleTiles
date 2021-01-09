package com.creativemd.littletiles.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.util.Color;

import com.creativemd.creativecore.common.gui.container.GuiParent;
import com.creativemd.creativecore.common.gui.container.SubGui;
import com.creativemd.creativecore.common.gui.controls.gui.GuiAnalogeSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiCheckBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiColorPicker;
import com.creativemd.creativecore.common.gui.controls.gui.GuiComboBox;
import com.creativemd.creativecore.common.gui.controls.gui.GuiLabel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiPanel;
import com.creativemd.creativecore.common.gui.controls.gui.GuiSteppedSlider;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButton;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTabStateButtonTranslated;
import com.creativemd.creativecore.common.gui.controls.gui.GuiTextfield;
import com.creativemd.creativecore.common.gui.event.gui.GuiControlChangedEvent;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.littletiles.common.particle.LittleParticleTexture;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpread;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadCircular;
import com.creativemd.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadRandom;
import com.n247s.api.eventapi.eventsystem.CustomEventSubscribe;

import net.minecraft.nbt.NBTTagCompound;

public class SubGuiParticle extends SubGui {
    
    public LittleParticleEmitter particle;
    
    public SubGuiParticle(LittleParticleEmitter particle) {
        super(200, 220);
        this.particle = particle;
    }
    
    @Override
    public void createControls() {
        ArrayList<String> textures = new ArrayList<>();
        for (int i = 0; i < LittleParticleTexture.values().length; i++)
            textures.add(LittleParticleTexture.values()[i].translatedName());
        
        controls.add(new GuiLabel("delay", translate("gui.particle.delay"), 0, 2));
        controls.add(new GuiTextfield("speed", "" + particle.tickDelay, 154, 0, 40, 12).setNumbersOnly());
        controls.add(new GuiCheckBox("randomColor", translate("gui.particle.randomcolor"), 100, 22, particle.randomColor));
        
        GuiTabStateButtonTranslated state = new GuiTabStateButtonTranslated("spread", 0, "gui.particle.spread", 0, 20, 12, spreadHandlerNames());
        state.selected = ArrayUtils.indexOf(state.states, getSpreadId(particle.spread.getClass()));
        if (state.selected == -1)
            state.selected = 0;
        controls.add(state);
        controls.add(new GuiPanel("spreadpanel", 0, 40, 194, 40));
        loadSpread();
        
        controls.add(new GuiLabel("textureLabel", translate("gui.particle.texture"), 0, 112));
        controls.add(new GuiComboBox("textures", 78, 110, 116, textures));
        controls.add(new GuiLabel("sizeLabel", translate("gui.particle.size"), 0, 134));
        controls.add(new GuiTextfield("size", "0.4", 30, 132, 40, 12).setFloatOnly());
        controls.add(new GuiColorPicker("color", 0, 152, new Color(255, 255, 255), true, 1));
        controls.add(new GuiLabel("growrateLabel", translate("gui.particle.growrate"), 75, 134));
        controls.add(new GuiTextfield("growrate", "0.001", 154, 134, 40, 12).setFloatOnly());
        controls.add(new GuiLabel("gravityLabel", translate("gui.particle.gravity"), 140, 155));
        controls.add(new GuiAnalogeSlider("gravity", 140, 173, 50, 12, 0, 0, 1));
        controls.add(new GuiLabel("ageLabel", translate("gui.particle.age"), 0, 202));
        controls.add(new GuiSteppedSlider("age", 60, 200, 70, 10, 20, 1, 100));
        controls.add(new GuiButton("save", translate("gui.save"), 170, 200) {
            
            @Override
            public void onClicked(int x, int y, int button) {
                NBTTagCompound nbt = new NBTTagCompound();
                
                GuiTextfield speed = (GuiTextfield) get("speed");
                nbt.setInteger("tickDelay", speed.parseInteger());
                GuiCheckBox randomColor = (GuiCheckBox) get("randomColor");
                nbt.setBoolean("randomColor", randomColor.value);
                
                ParticleSpread spread = parseSpread();
                spread.write(nbt);
                
                ParticleSettings newSettings = new ParticleSettings();
                GuiComboBox textures = (GuiComboBox) get("textures");
                newSettings.texture = LittleParticleTexture.get(textures.getCaption());
                GuiSteppedSlider age = (GuiSteppedSlider) get("age");
                newSettings.lifetime = (int) age.value;
                GuiColorPicker color = (GuiColorPicker) get("color");
                newSettings.color = ColorUtils.RGBAToInt(color.color);
                GuiAnalogeSlider gravity = (GuiAnalogeSlider) get("gravity");
                newSettings.gravity = (float) gravity.value;
                GuiTextfield size = (GuiTextfield) get("size");
                newSettings.size = size.parseFloat();
                GuiTextfield growrate = (GuiTextfield) get("growrate");
                newSettings.growrate = growrate.parseFloat();
                
                newSettings.write(nbt);
                sendPacketToServer(nbt);
                closeGui();
            }
        });
        loadParticleSettings(particle.settings);
    }
    
    public void loadSpread() {
        GuiTabStateButton spread = (GuiTabStateButton) get("spread");
        ParticleSpreadGuiHandler handler = getSpreadHanlder(spread.getCaption());
        
        if (handler != null) {
            GuiPanel panel = (GuiPanel) get("spreadpanel");
            panel.controls.clear();
            handler.createControls(panel, particle.spread);
        }
    }
    
    public ParticleSpread parseSpread() {
        GuiTabStateButton tab = (GuiTabStateButton) get("spread");
        ParticleSpreadGuiHandler handler = getSpreadHanlder(tab.getCaption());
        GuiPanel panel = (GuiPanel) get("spreadpanel");
        
        if (handler != null)
            return handler.parse(panel);
        return new ParticleSpreadRandom();
    }
    
    @CustomEventSubscribe
    public void onParticleChange(GuiControlChangedEvent event) {
        if (event.source.is("spread"))
            loadSpread();
    }
    
    public void loadParticleSettings(ParticleSettings settings) {
        GuiComboBox textures = (GuiComboBox) get("textures");
        textures.select(settings.texture.translatedName());
        GuiSteppedSlider age = (GuiSteppedSlider) get("age");
        age.setValue(settings.lifetime);
        GuiColorPicker color = (GuiColorPicker) get("color");
        color.setColor(ColorUtils.IntToRGBA(settings.color));
        GuiAnalogeSlider gravity = (GuiAnalogeSlider) get("gravity");
        gravity.setValue(settings.gravity);
        GuiTextfield size = (GuiTextfield) get("size");
        size.text = "" + settings.size;
        GuiTextfield growrate = (GuiTextfield) get("growrate");
        growrate.text = "" + settings.growrate;
    }
    
    private static LinkedHashMap<String, ParticleSpreadGuiHandler> spreadHandlers = new LinkedHashMap<>();
    private static HashMap<Class, String> spreadClass = new HashMap<>();
    
    public static String[] spreadHandlerNames() {
        return spreadHandlers.keySet().toArray(new String[0]);
    }
    
    public static ParticleSpreadGuiHandler getSpreadHanlder(String key) {
        return spreadHandlers.get(key);
    }
    
    public static String getSpreadId(Class<? extends ParticleSpread> clazz) {
        return spreadClass.getOrDefault(clazz, "random");
    }
    
    public static void registerParticleSpreadGuiHandler(String key, Class<? extends ParticleSpread> clazz, ParticleSpreadGuiHandler handler) {
        spreadClass.put(clazz, key);
        spreadHandlers.put(key, handler);
    }
    
    static {
        registerParticleSpreadGuiHandler("random", ParticleSpreadRandom.class, new ParticleSpreadGuiHandler() {
            
            @Override
            public ParticleSpread parse(GuiParent parent) {
                ParticleSpreadRandom spread = new ParticleSpreadRandom();
                GuiTextfield speedx = (GuiTextfield) parent.get("speedx");
                spread.speedX = speedx.parseFloat();
                GuiTextfield speedy = (GuiTextfield) parent.get("speedy");
                spread.speedY = speedy.parseFloat();
                GuiTextfield speedz = (GuiTextfield) parent.get("speedz");
                spread.speedZ = speedz.parseFloat();
                GuiTextfield spreadT = (GuiTextfield) parent.get("deviation");
                spread.spread = spreadT.parseFloat();
                return spread;
            }
            
            @Override
            public void createControls(GuiParent parent, ParticleSpread spread) {
                parent.addControl(new GuiLabel("speedyLabel", translate("gui.particle.spread.speedy"), 0, 2));
                parent.addControl(new GuiTextfield("speedy", "" + spread.speedY, 40, 0, 40, 12).setFloatOnly());
                
                parent.addControl(new GuiLabel("spreadLabel", translate("gui.particle.spread.spread"), 90, 2));
                parent.addControl(new GuiTextfield("deviation", "" + spread.spread, 145, 0, 40, 12).setFloatOnly());
                
                parent.addControl(new GuiLabel("speedxLabel", translate("gui.particle.spread.speedx"), 0, 22));
                parent.addControl(new GuiTextfield("speedx", "" + (spread instanceof ParticleSpreadRandom ? ((ParticleSpreadRandom) spread).speedX : 0.1F), 40, 20, 40, 12)
                    .setFloatOnly());
                
                parent.addControl(new GuiLabel("speedzLabel", translate("gui.particle.spread.speedz"), 90, 22));
                parent.addControl(new GuiTextfield("speedz", "" + (spread instanceof ParticleSpreadRandom ? ((ParticleSpreadRandom) spread).speedZ : 0.1F), 145, 20, 40, 12)
                    .setFloatOnly());
            }
        });
        registerParticleSpreadGuiHandler("circular", ParticleSpreadCircular.class, new ParticleSpreadGuiHandler() {
            
            @Override
            public ParticleSpread parse(GuiParent parent) {
                ParticleSpreadCircular spread = new ParticleSpreadCircular();
                GuiTextfield radius = (GuiTextfield) parent.get("radius");
                spread.radius = radius.parseFloat();
                GuiTextfield spreadT = (GuiTextfield) parent.get("deviation");
                spread.spread = spreadT.parseFloat();
                GuiTextfield speedy = (GuiTextfield) parent.get("speedy");
                spread.speedY = speedy.parseFloat();
                GuiTextfield steps = (GuiTextfield) parent.get("steps");
                spread.steps = steps.parseInteger();
                return spread;
            }
            
            @Override
            public void createControls(GuiParent parent, ParticleSpread spread) {
                parent.addControl(new GuiLabel("speedyLabel", translate("gui.particle.spread.speedy"), 0, 2));
                parent.addControl(new GuiTextfield("speedy", "" + spread.speedY, 40, 0, 40, 12).setFloatOnly());
                
                parent.addControl(new GuiLabel("spreadLabel", translate("gui.particle.spread.spread"), 90, 2));
                parent.addControl(new GuiTextfield("deviation", "" + spread.spread, 145, 0, 40, 12).setFloatOnly());
                
                parent.addControl(new GuiLabel("radiusLabel", translate("gui.particle.spread.radius"), 0, 22));
                parent.addControl(new GuiTextfield("radius", "" + (spread instanceof ParticleSpreadCircular ? ((ParticleSpreadCircular) spread).radius : 0.1F), 40, 20, 40, 12)
                    .setFloatOnly());
                
                parent.addControl(new GuiLabel("stepsLabel", translate("gui.particle.spread.steps"), 90, 22));
                parent.addControl(new GuiTextfield("steps", "" + (spread instanceof ParticleSpreadCircular ? ((ParticleSpreadCircular) spread).steps : 30), 145, 20, 40, 12)
                    .setNumbersOnly());
            }
        });
    }
    
    public static abstract class ParticleSpreadGuiHandler {
        
        public abstract void createControls(GuiParent parent, ParticleSpread spread);
        
        public abstract ParticleSpread parse(GuiParent parent);
        
    }
    
}
