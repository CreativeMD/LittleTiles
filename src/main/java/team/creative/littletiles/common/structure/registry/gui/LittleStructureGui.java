package team.creative.littletiles.common.structure.registry.gui;

import java.util.function.BiFunction;

import team.creative.littletiles.common.animation.AnimationGuiHandler;
import team.creative.littletiles.common.structure.LittleStructureType;

public record LittleStructureGui(LittleStructureType type, BiFunction<LittleStructureType, AnimationGuiHandler, LittleStructureGuiControl> factory) {
    
}
