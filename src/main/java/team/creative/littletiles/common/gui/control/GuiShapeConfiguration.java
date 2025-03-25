package team.creative.littletiles.common.gui.control;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.Side;
import team.creative.creativecore.common.config.gui.GuiConfigSubControlNested;
import team.creative.creativecore.common.gui.GuiParent;
import team.creative.creativecore.common.gui.control.collection.GuiComboBoxTree;
import team.creative.creativecore.common.gui.control.parent.GuiScrollY;
import team.creative.creativecore.common.gui.extension.GuiExtensionCreator.ExtensionDirection;
import team.creative.creativecore.common.gui.flow.GuiFlow;
import team.creative.creativecore.common.util.inventory.ContainerSlotView;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.api.common.tool.ILittleShaper;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.LittleShapeInstance;
import team.creative.littletiles.common.placement.shape.ShapeRegistry;
import team.creative.littletiles.common.placement.shape.config.LittleShapeConfig;

public class GuiShapeConfiguration extends GuiParent {
    
    protected final Player player;
    protected final ContainerSlotView tool;
    
    public GuiShapeConfiguration(Player player, String name, ContainerSlotView tool) {
        super(name);
        this.flow = GuiFlow.STACK_Y;
        this.player = player;
        this.tool = tool;
        GuiComboBoxTree<LittleShape> box = new GuiComboBoxTree<>("shape", ShapeRegistry.SHAPE_TREE, x -> Component.translatable("shape." + x)).setDirection(
            ExtensionDirection.BELOW_OR_ABOVE_ANY_SIZE);
        box.setExpandableX();
        var shapeInstance = ((ILittleShaper) tool.get().getItem()).getShape(tool.get());
        box.select(shapeInstance.shape);
        add(box);
        
        add(new GuiScrollY("settings").setHovered().setDim(20, 60).setExpandable());
        
        changed();
        
        registerEventChanged(x -> {
            if (x.control.is("shape"))
                changed();
        });
    }
    
    @Override
    public Provider provider() {
        return player.registryAccess();
    }
    
    public void changed() {
        GuiComboBoxTree<LittleShape> box = get("shape");
        GuiScrollY scroll = get("settings");
        
        scroll.clear();
        LittleShapeConfig config;
        var in = tool.get().get(LittleTilesRegistry.SHAPE);
        if (in != null)
            config = in.loadOtherConfig(provider(), box.selected(), Side.CLIENT);
        else
            config = ShapeRegistry.createConfigDefault(box.selected());
        if (config != null)
            scroll.add(ShapeRegistry.SHAPE_CONFIG_REGISTRY.create("config", config, Side.CLIENT));
        scroll.reflow();
    }
    
    public LittleShapeInstance save() {
        GuiComboBoxTree<LittleShape> box = get("shape");
        GuiScrollY scroll = get("settings");
        LittleShape shape = box.selected(((ILittleShaper) tool.get().getItem()).defaultShape());
        GuiConfigSubControlNested shapeConfig = scroll.get("config");
        LittleShapeConfig config = null;
        if (shapeConfig != null) {
            shapeConfig.save();
            config = (LittleShapeConfig) shapeConfig.value;
        }
        
        LittleShapeInstance in = tool.get().get(LittleTilesRegistry.SHAPE);
        if (in == null)
            in = LittleShapeInstance.of(shape);
        return in.configure(provider(), shape, config, Side.CLIENT);
    }
    
}
