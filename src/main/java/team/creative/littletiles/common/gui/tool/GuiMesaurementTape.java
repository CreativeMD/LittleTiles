package team.creative.littletiles.common.gui.tool;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.component.PatchedDataComponentMap;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiListBoxBase;
import team.creative.creativecore.common.gui.control.simple.GuiColorPicker;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.creativecore.common.util.type.Color;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleMeasure;
import team.creative.littletiles.common.item.component.MeasurementTypeComponent;
import team.creative.littletiles.common.item.component.MeasurementsComponent;
import team.creative.littletiles.common.math.measure.LittleMeasurement;
import team.creative.littletiles.common.math.measure.LittleMeasurementType;

public class GuiMesaurementTape extends GuiConfigure {
    
    public GuiMesaurementTape(ContainerSlotView tool) {
        super("measurement_tape", 200, 200, tool);
    }
    
    @Override
    public void create() {
        flow = GuiFlow.STACK_Y;
        var measurements = ((ILittleMeasure) tool.get().getItem()).getMeasurements(tool.get());
        GuiStateButton<LittleMeasurementType> types = new GuiStateButton<>("type", new TextMapBuilder<LittleMeasurementType>().addComponent(LittleMeasurementType.REGISTRY.values(),
            LittleMeasurementType::translatable));
        types.select(tool.get().has(LittleTilesRegistry.MEASUREMENT_TYPE) ? tool.get().get(LittleTilesRegistry.MEASUREMENT_TYPE).type : LittleMeasurementType.REGISTRY
                .getDefault());
        add(types.setExpandableX());
        List<GuiMeasurement> controls = new ArrayList<>();
        for (LittleMeasurement m : measurements)
            controls.add(new GuiMeasurement(m));
        GuiListBoxBase<GuiMeasurement> list = new GuiListBoxBase<>("measures", true, controls);
        add(list.setExpandable());
    }
    
    @Override
    public boolean saveConfiguration(PatchedDataComponentMap data) {
        GuiStateButton<LittleMeasurementType> types = get("type");
        data.set(LittleTilesRegistry.MEASUREMENT_TYPE.value(), new MeasurementTypeComponent(types.selected()));
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
