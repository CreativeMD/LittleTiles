package team.creative.littletiles.common.action.exception;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittleBuildingConfig;

public class NotAllowedToEditException extends LittleActionException {
    
    public LittleBuildingConfig config;
    
    public NotAllowedToEditException(Player player, LittleBuildingConfig config) {
        super("exception.permission.edit");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.editBlockLimit.value);
    }
    
}