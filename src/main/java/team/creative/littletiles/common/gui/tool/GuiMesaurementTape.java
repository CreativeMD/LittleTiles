package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.PatchedDataComponentMap;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiListBoxBase;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.math.measure.LittleMeasurement;

public class GuiMesaurementTape extends GuiConfigure {
    
    public GuiMesaurementTape(ContainerSlotView tool) {
        super("measurement_tape", 200, 200, tool);
    }
    
    @Override
    public void create() {
        var measurements = ((ILittleMeasure) tool.get().getItem()).getMeasurements(tool.get());
        List<GuiMeasurement> controls = new ArrayList<>();
        for (LittleMeasurement m : measurements)
            controls.add(new GuiMeasurement(m));
        GuiListBoxBase<GuiMeasurement> list = new GuiListBoxBase<>("measures", true, controls);
        add(list.setExpandable());
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        List<LittleMeasurement> measurements = new ArrayList<>();
        GuiListBoxBase<GuiMeasurement> list = get("measures");
        for (GuiMeasurement m : list.items())
            measurements.add(m.save());
        data.set(LittleTilesRegistry.MEASUREMENTS.value(), MeasurementsComponent.of(measurements));
        return true;
    }
    
    public class GuiMeasurement extends GuiParent {
        
        public final LittleMeasurement measurement;
        public final GuiColorPicker picker;
        
        public GuiMeasurement(LittleMeasurement measurement) {
            this.measurement = measurement;
            this.picker = new GuiColorPicker("color", new Color(measurement.color), true, 0);
            add(picker);
        }
        
        public LittleMeasurement save() {
            measurement.color = this.picker.color.toInt();
            return measurement;
        }
    }
    
}
