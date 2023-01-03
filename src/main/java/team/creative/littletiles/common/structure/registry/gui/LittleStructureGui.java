package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.BiFunction;

import net.minecraft.network.chat.Component;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public record LittleStructureGui(String id, LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory, boolean supportsName) {
    
    public LittleStructureGui(String id, LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory, boolean supportsName) {
        this.id = id;
        this.type = type;
        this.factory = factory;
        this.supportsName = supportsName;
    }
    
    public LittleStructureGui(String id, LittleStructureType type, BiFunction<LittleStructureGui, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
        this(id, type, factory, true);
    }
    
    public Component translatable() {
        return Component.translatable("structure." + id);
    }
    
    public LittleStructureGuiControl create(GuiTreeItemStructure item) {
        return factory.apply(this, item);
    }
    
}
