package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.BiFunction;

import net.minecraft.network.chat.Component;
import team.creative.littletiles.common.gui.tool.recipe.GuiTreeItemStructure;
import team.creative.littletiles.common.structure.LittleStructureType;

public record LittleStructureGui(String id, LittleStructureType type, BiFunction<LittleStructureType, GuiTreeItemStructure, LittleStructureGuiControl> factory) {
    
    public Component translatable() {
        return Component.translatable("structure." + id);
    }
    
    public LittleStructureGuiControl create(GuiTreeItemStructure item) {
        return factory.apply(type, item);
    }
    
}
