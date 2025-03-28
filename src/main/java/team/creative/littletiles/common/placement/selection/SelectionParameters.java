package team.creative.littletiles.common.placement.selection;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public record SelectionParameters(Level level, Player player, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean includeBE, boolean rememberStructure) {
    
    public SelectionParameters(Level level, Player player, boolean includeVanilla, boolean includeCB, boolean includeLT, boolean rememberStructure) {
        this(level, player, includeVanilla, includeCB, includeLT, includeCB || includeLT, rememberStructure);
    }
}
