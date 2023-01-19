package team.creative.littletiles.common.gui.signal.dialog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.VAlign;
import team.creative.creativecore.common.gui.controls.collection.GuiComboBoxMapped;
import team.creative.creativecore.common.gui.controls.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.controls.simple.GuiButton;
import team.creative.creativecore.common.gui.controls.simple.GuiLabel;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.gui.sync.GuiSyncGlobalLayer;
import team.creative.creativecore.common.gui.sync.GuiSyncHolder;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.signal.GeneratePatternException;
import team.creative.littletiles.common.gui.signal.GuiSignalComponent;
import team.creative.littletiles.common.gui.signal.GuiSignalController;
import team.creative.littletiles.common.gui.signal.IConditionConfiguration;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalMode;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;

public class GuiDialogSignal extends GuiLayer {
    
    public static final GuiSyncGlobalLayer<GuiDialogSignal> SIGNAL_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_dialog", x -> new GuiDialogSignal());
    public static final GuiSyncGlobalLayer<GuiDialogSignalInput> INPUT_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_input_dialog", x -> new GuiDialogSignalInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalMode> MODE_DIALOG = GuiSyncHolder.GLOBAL.layer("signal_mode_dialog", x -> new GuiDialogSignalMode());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualInput> VIRTUAL_INPUT_DIALOG = GuiSyncHolder.GLOBAL
            .layer("signal_virtual_input_dialog", x -> new GuiDialogSignalVirtualInput());
    public static final GuiSyncGlobalLayer<GuiDialogSignalVirtualNumberInput> VIRTUAL_NUMBER_DIALOG = GuiSyncHolder.GLOBAL
            .layer("signal_virtual_number_dialog", x -> new GuiDialogSignalVirtualNumberInput());
    
    public List<GuiSignalComponent> inputs;
    protected IConditionConfiguration event;
    
    public GuiDialogSignal() {
        super("gui.dialog.signal", 320, 200);
        flow = GuiFlow.STACK_Y;
        registerEventChanged(this::changed);
    }
    
    public void init(List<GuiSignalComponent> inputs, IConditionConfiguration event) {
        this.inputs = inputs;
        this.event = event;
        super.init();
    }
    
    public void changed(GuiControlChangedEvent event) {
        if (event.control.is("controller")) {
            GuiLabel label = get("result");
            GuiLabel delay = get("delay");
            try {
                SignalInputCondition condition = ((GuiSignalController) event.control).generatePattern();
                label.setTitle(Component.translatable("gui.signal.configuration.result").append(" " + condition.toString()));
                DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
                df.setMaximumFractionDigits(5);
                delay.setTitle(Component.literal(df.format(condition.calculateDelay()) + " ticks"));
            } catch (GeneratePatternException e) {
                label.setTitle(Component.translatable("gui.signal.configuration.result").append(" ").append(Component.translatable(e.getMessage())));
                delay.setTitle(Component.literal(0 + " ticks"));
            }
        }
    }
    
    @Override
    public void create() {
        if (inputs == null)
            return;
        
        GuiLeftRightBox top = new GuiLeftRightBox();
        add(top.setVAlign(VAlign.CENTER));
        top.addLeft(new GuiLabel("result").setTranslate("gui.signal.configuration.result"));
        top.addRight(new GuiLabel("delay"));
        
        if (event.hasModeConfiguration())
            top.addRight(new GuiButton("mode", x -> MODE_DIALOG.open(getIntegratedParent(), new CompoundTag()).init(GuiDialogSignal.this, event)));
        
        GuiSignalController controller = new GuiSignalController("controller", event.getOutput(), inputs);
        add(controller.setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        List<GuiSignalComponent> allInputs = new ArrayList<>(inputs);
        allInputs.add(new GuiSignalComponent("[]", true, false, -1, 1, null, SignalMode.EQUAL));
        allInputs.add(new GuiSignalComponent("number", true, false, -1, 1, null, SignalMode.EQUAL));
        
        GuiComboBoxMapped<GuiSignalComponent> inputs = new GuiComboBoxMapped<GuiSignalComponent>("inputs", new TextMapBuilder<GuiSignalComponent>()
                .addComponent(allInputs, x -> Component.literal(x.info())));
        bottom.addLeft(inputs);
        bottom.addLeft(new GuiButton("add", x -> {
            GuiSignalComponent com = inputs.getSelected();
            if (com.name().equals("[]"))
                controller.addVirtualInput();
            else if (com.name().equals("number"))
                controller.addVirtualNumberInput();
            else
                controller.addInput(com);
        }).setTranslate("gui.plus"));
        
        TextMapBuilder<Consumer<GuiSignalController>> map = new TextMapBuilder<>();
        map.addComponent(x -> x.addOperator(SignalLogicOperator.AND), Component.literal(SignalLogicOperator.AND.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.OR), Component.literal(SignalLogicOperator.OR.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.XOR), Component.literal(SignalLogicOperator.XOR.display));
        map.addComponent(x -> x.addNotOperator(false), Component.literal("not"));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.BITWISE_AND), Component.literal(SignalLogicOperator.BITWISE_AND.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.BITWISE_OR), Component.literal(SignalLogicOperator.BITWISE_OR.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.BITWISE_XOR), Component.literal(SignalLogicOperator.BITWISE_XOR.display));
        map.addComponent(x -> x.addNotOperator(true), Component.literal("b-not"));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.ADD), Component.literal(SignalLogicOperator.ADD.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.SUB), Component.literal(SignalLogicOperator.SUB.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.MUL), Component.literal(SignalLogicOperator.MUL.display));
        map.addComponent(x -> x.addOperator(SignalLogicOperator.DIV), Component.literal(SignalLogicOperator.DIV.display));
        
        GuiComboBoxMapped<Consumer<GuiSignalController>> operators = new GuiComboBoxMapped<>("operators", map);
        bottom.addLeft(operators);
        bottom.addLeft(new GuiButton("add", x -> operators.getSelected().accept(controller)).setTranslate("gui.plus"));
        
        if (event.getCondition() != null)
            controller.setCondition(event.getCondition(), this);
        
        bottom.addRight(new GuiButton("save", x -> {
            try {
                event.setCondition(controller.generatePattern());
                event.update();
                GuiDialogSignal.this.closeThisLayer();
            } catch (GeneratePatternException e) {}
            
        }).setTranslate("gui.save"));
        
        changed(new GuiControlChangedEvent(controller));
        modeChanged();
    }
    
    public GuiSignalComponent getInput(SignalTarget target) throws ParseException {
        for (GuiSignalComponent component : inputs)
            if (component.name().equals(target.writeBase()))
                return component;
        throw new ParseException("input not found", 0);
    }
    
    public void modeChanged() {
        if (event.hasModeConfiguration()) {
            get("mode", GuiButton.class).setTranslate(event.getModeConfiguration().getMode().translateKey);
            reflow();
        }
    }
}
