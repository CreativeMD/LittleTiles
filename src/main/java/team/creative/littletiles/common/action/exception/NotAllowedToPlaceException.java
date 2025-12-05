package team.creative.littletiles.common.action.exception;

import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class NotAllowedToPlaceException extends LittleActionException {
    
    public LittlePermissionBuild config;
    
    public NotAllowedToPlaceException(LittlePermissionBuild config) {
        super("exception.permission.place");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.placeBlockLimit.value);
    }
    
}