package team.creative.littletiles.common.action.exception;

import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class NotAllowedToPlaceColorException extends LittleActionException {
    
    public LittlePermissionBuild config;
    
    public NotAllowedToPlaceColorException(LittlePermissionBuild config) {
        super("exception.permission.place.color");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.minimumTransparency);
    }
    
}