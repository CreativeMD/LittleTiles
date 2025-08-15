package team.creative.littletiles.common.gui.signal.dialog;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.network.chat.Component;
import team.creative.creativecore.common.gui.GuiLayer;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.parent.GuiLeftRightBox;
import team.creative.creativecore.common.gui.control.parent.GuiScrollY;
import team.creative.creativecore.common.gui.control.simple.GuiButton;
import team.creative.creativecore.common.gui.control.simple.GuiLabel;
import team.creative.creativecore.common.gui.control.simple.GuiStateButton;
import team.creative.creativecore.common.gui.control.simple.GuiTabButton;
import team.creative.creativecore.common.gui.control.simple.GuiTextfield;
import team.creative.creativecore.common.gui.event.GuiControlChangedEvent;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.text.TextMapBuilder;
import team.creative.littletiles.common.gui.signal.node.GuiSignalNodeInput;
import team.creative.littletiles.common.structure.signal.input.SignalInputCondition;
import team.creative.littletiles.common.structure.signal.logic.SignalLogicOperator;
import team.creative.littletiles.common.structure.signal.logic.SignalPatternParser;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget;
import team.creative.littletiles.common.structure.signal.logic.SignalTarget.SignalCustomIndex;

public class GuiDialogSignalInput extends GuiLayer {
    
    public GuiSignalNodeInput input;
    
    public int bandwidth;
    
    public GuiDialogSignalInput() {
        super("gui.dialog.signal.input", 200, 100);
        flow = GuiFlow.STACK_Y;
        registerEventChanged(this::changed);
    }
    
    public void init(GuiSignalNodeInput input) {
        this.input = input;
        super.init();
    }
    
    @Override
    public void create() {
        if (input == null)
            return;
        GuiParent upper = new GuiParent(GuiFlow.STACK_X);
        add(upper);
        upper.add(new GuiLabel("start").setTitle(Component.literal(input.component.name() + "[")));
        upper.add(new GuiTextfield("range", input.getRange()).setDim(100));
        upper.add(new GuiLabel("result").setTitle(Component.literal("] " + input.component.bandwidth() + " ->")));
        List<GuiSignalInputOperator> modes = new ArrayList<>();
        modes.add(new GuiSignalInputOperator("none") {
            
            @Override
            public void load(GuiSignalNodeInput input, GuiParent panel) {}
            
            @Override
            public void save(GuiSignalNodeInput input, GuiParent panel) {}
            
        });
        modes.add(new GuiSignalInputOperator("operation") {
            
            @Override
            public void load(GuiSignalNodeInput input, GuiParent panel) {
                panel.add(
                    new GuiStateButton<SignalLogicOperator>("operation", input.logic == null ? SignalLogicOperator.AND : input.logic, new TextMapBuilder<SignalLogicOperator>()
                            .addComponent(Arrays.asList(SignalLogicOperator.AND, SignalLogicOperator.OR, SignalLogicOperator.XOR), x -> Component.literal(x.display))));
            }
            
            @Override
            public void save(GuiSignalNodeInput input, GuiParent panel) {
                input.logic = ((GuiStateButton<SignalLogicOperator>) panel.get("operation")).selected();
            }
            
        });
        modes.add(new GuiSignalInputOperator("pattern") {
            
            @Override
            public void load(GuiSignalNodeInput input, GuiParent panel) {
                for (int i = 0; i < bandwidth; i++) {
                    GuiParent line = new GuiParent(GuiFlow.STACK_X);
                    panel.add(line.setExpandableX());
                    int state = 2;
                    if (input.pattern != null && input.pattern.length > i)
                        state = input.pattern[i];
                    
                    line.add(new GuiLabel("label" + i).setTitle(Component.literal(i + ":")));
                    line.add(new GuiStateButton<Integer>(i + "", state, new TextMapBuilder<Integer>().addComponent(0, Component.translatable("gui.false")).addComponent(1, Component
                            .translatable("gui.true")).addComponent(2, Component.translatable("gui.ignore"))));
                }
            }
            
            @Override
            public void save(GuiSignalNodeInput input, GuiParent panel) {
                int[] indexes = new int[bandwidth];
                for (int i = 0; i < bandwidth; i++) {
                    GuiStateButton<Integer> stateButton = panel.get(i + "");
                    indexes[i] = stateButton.selected();
                }
                input.pattern = indexes;
            }
            
        });
        modes.add(new GuiSignalInputOperator("equation") {
            
            @Override
            public void load(GuiSignalNodeInput input, GuiParent panel) {
                GuiParent line = new GuiParent(GuiFlow.STACK_X);
                panel.add(line.setExpandableX());
                line.add(new GuiLabel("label").setTitle(Component.literal("d<index>")));
                line.add(new GuiTextfield("equation", input.equation != null ? input.equation.write() : "").setDim(100));
                
            }
            
            @Override
            public void save(GuiSignalNodeInput input, GuiParent panel) {
                GuiTextfield textfield = (GuiTextfield) get("equation");
                try {
                    input.equation = SignalInputCondition.parseExpression(new SignalPatternParser(textfield.getText()), new char[0], false, true);
                } catch (ParseException e) {
                    input.equation = null;
                }
                
            }
            
        });
        add(new GuiTabButton<GuiSignalInputOperator>("type", input.operator, new TextMapBuilder<GuiSignalInputOperator>().addComponent(modes, x -> x.translatable())));
        add(new GuiScrollY("config").setExpandable());
        
        GuiLeftRightBox bottom = new GuiLeftRightBox();
        add(bottom);
        
        bottom.addLeft(new GuiButton("cancel", x -> GuiDialogSignalInput.this.closeThisLayer()).setTranslate("gui.cancel"));
        bottom.addRight(new GuiButton("save", x -> {
            GuiTextfield range = (GuiTextfield) get("range");
            try {
                input.indexes = parseRange(range.getText());
            } catch (ParseException e) {}
            
            GuiTabButton<GuiSignalInputOperator> tab = get("type");
            GuiScrollY panel = get("config");
            input.operator = tab.index();
            tab.selected().save(input, panel);
            input.updateLabel();
            closeTopLayer();
        }).setTranslate("gui.save"));
        
        updateBandwidth();
        
    }
    
    public void changed(GuiControlChangedEvent event) {
        if (event.control.is("type")) {
            GuiTabButton<GuiSignalInputOperator> tab = get("type");
            GuiScrollY panel = get("config");
            panel.clear();
            tab.selected().load(input, panel);
            tab.reflow();
        } else if (event.control.is("range"))
            updateBandwidth();
    }
    
    public void updateBandwidth() {
        GuiTextfield range = (GuiTextfield) get("range");
        try {
            SignalCustomIndex[] indexes = parseRange(range.getText());
            if (indexes == null)
                bandwidth = input.component.bandwidth();
            else {
                bandwidth = 0;
                for (int i = 0; i < indexes.length; i++)
                    bandwidth += indexes[i].length();
            }
        } catch (ParseException e) {
            bandwidth = input.component.bandwidth();
        }
        GuiLabel result = (GuiLabel) get("result");
        result.setTitle(Component.literal("] " + input.component.bandwidth() + " -> " + bandwidth));
        raiseEvent(new GuiControlChangedEvent(get("type")));
    }
    
    private SignalCustomIndex[] parseRange(String range) throws ParseException {
        SignalPatternParser parser = new SignalPatternParser(range);
        
        List<SignalCustomIndex> indexes = new ArrayList<>();
        while (parser.hasNext()) {
            indexes.add(SignalTarget.parseIndex(parser));
            if (parser.hasNext() && parser.next(true) == ',')
                continue;
            else
                break;
        }
        if (indexes.isEmpty())
            return null;
        return indexes.toArray(new SignalCustomIndex[indexes.size()]);
    }
    
    public abstract class GuiSignalInputOperator {
        
        public final String name;
        
        public GuiSignalInputOperator(String name) {
            this.name = name;
        }
        
        public abstract void load(GuiSignalNodeInput input, GuiParent panel);
        
        public abstract void save(GuiSignalNodeInput input, GuiParent panel);
        
        public Component translatable() {
            return Component.translatable("gui.signal.configuration.input.operation.type." + name);
        }
        
    }
    
}