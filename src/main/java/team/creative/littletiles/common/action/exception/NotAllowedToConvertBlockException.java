package team.creative.littletiles.common.action.exception;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class NotAllowedToConvertBlockException extends LittleActionException {
    
    public LittlePermissionBuild config;
    
    public NotAllowedToConvertBlockException(Player player, LittlePermissionBuild config) {
        super("exception.permission.convert");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.affectedBlockLimit.value);
    }
}