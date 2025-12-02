package team.creative.littletiles.common.gui.structure;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.Align;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.control.collection.GuiComboBox;
import team.creative.creativecore.common.gui.control.parent.GuiColumn;
import team.creative.creativecore.common.gui.control.parent.GuiLabeledControl;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiPanel;
import team.creative.creativecore.common.gui.control.parent.GuiRow;
import team.creative.creativecore.common.gui.control.parent.GuiTable;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiCheckBox;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiCounter;
import team.creative.creativecore.common.gui.control.simple.GuiCounterDecimal;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiSlider;
import team.creative.creativecore.common.gui.control.simple.GuiSteppedSlider;
import team.creative.creativecore.common.gui.control.simple.GuiTabButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncLocal;
import team.creative.creativecore.common.util.registry.NamedClassBoundHandlerRegistry;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.common.entity.particle.LittleParticlePresets;
import team.creative.littletiles.common.entity.particle.LittleParticleTexture;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSettings;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpread;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadCircular;
import team.creative.littletiles.common.structure.type.premade.LittleParticleEmitter.ParticleSpreadRandom;

public class GuiParticle extends GuiLayer {
    
    public static final NamedClassBoundHandlerRegistry<ParticleSpread, ParticleSpreadGuiHandler> REGISTRY = new NamedClassBoundHandlerRegistry<>();
    
    static {
        REGISTRY.registerDefault("random", ParticleSpreadRandom.class, new ParticleSpreadGuiHandler() {
            
            @Override
            public ParticleSpread save(GuiParent parent) {
                ParticleSpreadRandom spread = new ParticleSpreadRandom();
                spread.speedX = (float) parent.get("speedx", GuiCounterDecimal.class).getValue();
                spread.speedY = (float) parent.get("speedy", GuiCounterDecimal.class).getValue();
                spread.speedZ = (float) parent.get("speedz", GuiCounterDecimal.class).getValue();
                spread.spread = (float) parent.get("deviation", GuiCounterDecimal.class).getValue();
                return spread;
            }
            
            @Override
            public void load(GuiParent parent, ParticleSpread spread) {
                GuiTable table = new GuiTable();
                parent.add(table);
                GuiRow row = new GuiRow();
                table.addRow(row);
                GuiColumn col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.speedy", new GuiCounterDecimal("speedy", spread.speedY).setStep(0.05)));
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.spread", new GuiCounterDecimal("deviation", spread.spread).setStep(0.05)));
                
                row = new GuiRow();
                table.addRow(row);
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.speedx", new GuiCounterDecimal("speedx", spread instanceof ParticleSpreadRandom r ? r.speedX : 0.05).setStep(
                    0.1)));
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.speedz", new GuiCounterDecimal("speedz", spread instanceof ParticleSpreadRandom r ? r.speedZ : 0.05).setStep(
                    0.1)));
            }
        });
        REGISTRY.register("circular", ParticleSpreadCircular.class, new ParticleSpreadGuiHandler() {
            
            @Override
            public ParticleSpread save(GuiParent parent) {
                ParticleSpreadCircular spread = new ParticleSpreadCircular();
                spread.speedY = (float) parent.get("speedy", GuiCounterDecimal.class).getValue();
                spread.spread = (float) parent.get("deviation", GuiCounterDecimal.class).getValue();
                spread.radius = (float) parent.get("radius", GuiCounterDecimal.class).getValue();
                spread.steps = parent.get("steps", GuiCounter.class).getValue();
                return spread;
            }
            
            @Override
            public void load(GuiParent parent, ParticleSpread spread) {
                GuiTable table = new GuiTable();
                parent.add(table);
                GuiRow row = new GuiRow();
                table.addRow(row);
                GuiColumn col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.speedy", new GuiCounterDecimal("speedy", spread.speedY).setStep(0.05)));
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.spread", new GuiCounterDecimal("deviation", spread.spread).setStep(0.05)));
                
                row = new GuiRow();
                table.addRow(row);
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.radius", new GuiCounterDecimal("radius", spread instanceof ParticleSpreadCircular c ? c.radius : 0.05).setStep(
                    0.1)));
                
                col = new GuiColumn();
                row.addColumn(col);
                col.add(new GuiLabeledControl("gui.particle.spread.steps", new GuiCounter("steps", spread instanceof ParticleSpreadCircular c ? c.steps : 30)));
            }
        });
    }
    
    public static abstract class ParticleSpreadGuiHandler {
        
        public abstract void load(GuiParent parent, ParticleSpread spread);
        
        public abstract ParticleSpread save(GuiParent parent);
        
    }
    
    public LittleParticleEmitter particle;
    public GuiComboBox<LittleParticlePresets> presetBox;
    public GuiComboBox<LittleParticleTexture> textureBox;
    public GuiCounterDecimal sizeStart;
    public GuiCounterDecimal sizeEnd;
    public GuiCounterDecimal sizeDiv;
    public GuiColorPicker color;
    public GuiCheckBox randomColor;
    public GuiSlider gravity;
    public GuiCheckBox collision;
    public GuiSteppedSlider age;
    public GuiCounter ageDiv;
    public GuiCounter count;
    public GuiCounter delay;
    public GuiTabButton<ParticleSpreadGuiHandler> spread;
    public GuiPanel spreadPanel;
    public GuiCheckBox locked;
    
    public GuiSyncLocal<CompoundTag> SAVE = getSyncHolder().register("save", x -> {
        particle.loadSettings(x);
        particle.updateStructure();
        closeThisLayer();
    });
    
    public GuiParticle(LittleParticleEmitter particle) {
        super("particle", 200, 230);
        this.particle = particle;
        this.flow = GuiFlow.STACK_Y;
        this.setAlign(Align.STRETCH);
        registerEventChanged(x -> {
            if (x.control.is("spread"))
                loadSpread(particle.spread);
        });
    }
    
    @Override
    public void create() {
        GuiParent presets = new GuiParent();
        add(presets);
        presets.add(presetBox = new GuiComboBox<>("presets", new TextMapBuilder<LittleParticlePresets>().addComponent(null, Component.literal("")).addComponent(
            LittleParticlePresets.values(), x -> Component.translatable("gui.particle.preset." + x.name().toLowerCase()))));
        presetBox.setExpandableX();
        presets.add(new GuiButton("save_preset", x -> {
            if (presetBox.selected() != null)
                loadPreset(presetBox.selected());
        }).setTranslate("gui.particle.load"));
        
        add(new GuiLabeledControl("gui.particle.texture", textureBox = new GuiComboBox<LittleParticleTexture>("texture", new TextMapBuilder<LittleParticleTexture>().addComponent(
            LittleParticleTexture.values(), x -> x.title()))));
        
        GuiParent size = new GuiParent();
        add(size.setVAlign(VAlign.CENTER));
        
        size.add(new GuiLabel("sizeLabel").setTranslate("gui.particle.size"));
        size.add(sizeStart = new GuiCounterDecimal("size", 0.4).setStep(0.05));
        size.add(new GuiLabel("sizeLabel").setTitle(Component.literal("->")));
        size.add(sizeEnd = new GuiCounterDecimal("sizeend", 0.6).setStep(0.05));
        size.add(new GuiLabel("sizeDeviationLabel").setTranslate("gui.particle.sizedeviation"));
        size.add(sizeDiv = new GuiCounterDecimal("sizeDiv", 0.02).setStep(0.02));
        
        add(color = new GuiColorPicker("color", new Color(255, 255, 255), true, 1));
        add(randomColor = new GuiCheckBox("randomColor", particle.settings.randomColor).setTranslate("gui.particle.randomcolor"));
        
        GuiParent physic = new GuiParent();
        add(physic.setVAlign(VAlign.CENTER));
        physic.add(new GuiLabeledControl("gui.particle.gravity", gravity = new GuiSlider("gravity", 0, -1, 1)));
        physic.add(collision = new GuiCheckBox("collision", particle.settings.collision).setTranslate("gui.particle.collision"));
        
        GuiParent ageParent = new GuiParent();
        add(ageParent.setVAlign(VAlign.CENTER));
        ageParent.add(new GuiLabeledControl("gui.particle.age", age = new GuiSteppedSlider("age", 20, 1, 100)));
        ageParent.add(new GuiLabeledControl("gui.particle.agedeviation", ageDiv = new GuiCounter("ageDiv", 20, 1, 100)));
        
        GuiParent amount = new GuiParent();
        add(amount.setVAlign(VAlign.CENTER));
        amount.add(new GuiLabeledControl("gui.particle.count", count = new GuiCounter("counter", particle.count)));
        amount.add(new GuiLabeledControl("gui.particle.per", delay = new GuiCounter("delay", particle.delay)));
        amount.add(new GuiLabel("tickLabel").setTranslate("gui.particle.tick"));
        
        add(spread = new GuiTabButton<>("spread", new TextMapBuilder<ParticleSpreadGuiHandler>().addEntrySet(REGISTRY.entrySet(), (x) -> Component.translatable(
            "gui.particle.spread." + x.getKey()))));
        
        add(spreadPanel = new GuiPanel());
        spread.select(REGISTRY.get(particle.spread.getClass()));
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        bottom.addLeft(locked = new GuiCheckBox("locked", particle.locked).setTranslate("gui.structure.locked"));
        bottom.addRight(new GuiButton("save", x -> {
            CompoundTag nbt = new CompoundTag();
            
            nbt.putInt("tickCount", count.getValue());
            nbt.putInt("tickDelay", delay.getValue());
            
            ParticleSpread spread = saveSpread();
            spread.write(nbt);
            
            ParticleSettings newSettings = new ParticleSettings();
            newSettings.randomColor = randomColor.value;
            newSettings.collision = collision.value;
            newSettings.texture = textureBox.selected();
            newSettings.lifetime = age.getIntValue();
            newSettings.lifetimeDeviation = ageDiv.getValue();
            newSettings.color = color.color.toInt();
            newSettings.gravity = (float) gravity.getValue();
            newSettings.startSize = (float) sizeStart.getValue();
            newSettings.endSize = (float) sizeEnd.getValue();
            newSettings.sizeDeviation = (float) sizeDiv.getValue();
            CompoundTag data = new CompoundTag();
            newSettings.write(data);
            nbt.put("settings", data);
            
            nbt.putBoolean("locked", locked.value);
            
            SAVE.send(nbt);
            closeThisLayer();
        }).setTranslate("gui.save"));
        loadParticleSettings(particle.settings);
    }
    
    public void loadPreset(LittleParticlePresets preset) {
        loadParticleSettings(preset.settings);
        count.setValue(preset.count);
        delay.setValue(preset.delay);
        spread.select(REGISTRY.get(particle.spread.getClass()));
        loadSpread(preset.spread);
    }
    
    public void loadSpread(ParticleSpread particleSpread) {
        ParticleSpreadGuiHandler handler = spread.selected();
        spreadPanel.clear();
        handler.load(spreadPanel, particleSpread);
        reflow();
    }
    
    public ParticleSpread saveSpread() {
        ParticleSpreadGuiHandler handler = spread.selected();
        if (handler != null)
            return handler.save(spreadPanel);
        return new ParticleSpreadRandom();
    }
    
    public void loadParticleSettings(ParticleSettings settings) {
        textureBox.select(settings.texture);
        age.setValue(settings.lifetime);
        ageDiv.setValue(settings.lifetimeDeviation);
        color.setColor(new Color(settings.color));
        randomColor.set(settings.randomColor);
        gravity.setValue(settings.gravity);
        sizeStart.setValue(settings.startSize);
        sizeEnd.setValue(settings.endSize);
        sizeDiv.setValue(settings.sizeDeviation);
        collision.set(settings.collision);
    }
    
}
