package team.creative.littletiles.common.action.exception;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittleBuildingConfig;

public class GridTooHighException extends LittleActionException {
    
    public LittleBuildingConfig config;
    public int attempted;
    
    public GridTooHighException(Player player, LittleBuildingConfig config, int attempted) {
        super("exception.permission.grid");
        this.config = config;
        this.attempted = attempted;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), attempted, config.gridLimit.value);
    }
    
}