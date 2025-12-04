package team.creative.littletiles.common.action.exception;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class AreaTooLarge extends LittleActionException {
    
    public LittlePermissionBuild config;
    
    public AreaTooLarge(Player player, LittlePermissionBuild config) {
        super("exception.permission.blueprint.size");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.blueprintSizeLimit);
    }
    
}