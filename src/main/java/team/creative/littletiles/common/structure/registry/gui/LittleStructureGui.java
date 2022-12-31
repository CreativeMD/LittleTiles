package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.BiFunction;

import net.minecraft.network.chat.Component;
import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.structure.LittleStructureType;

public record LittleStructureGui(String id, LittleStructureType type, BiFunction<LittleStructureType, AnimationGuiHandler, LittleStructureGuiControl> factory) {
    
    public Component translatable() {
        return Component.translatable("structure." + id);
    }
    
}
