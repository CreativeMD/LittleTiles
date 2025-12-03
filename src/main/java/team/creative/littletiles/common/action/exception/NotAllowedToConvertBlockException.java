package team.creative.littletiles.common.action.exception;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittleBuildingConfig;

public class NotAllowedToConvertBlockException extends LittleActionException {
    
    public LittleBuildingConfig config;
    
    public NotAllowedToConvertBlockException(Player player, LittleBuildingConfig config) {
        super("exception.permission.convert");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.affectedBlockLimit.value);
    }
}