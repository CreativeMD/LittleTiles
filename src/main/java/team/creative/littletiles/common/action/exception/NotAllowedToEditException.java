package team.creative.littletiles.common.action.exception;

import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class NotAllowedToEditException extends LittleActionException {
    
    public LittlePermissionBuild config;
    
    public NotAllowedToEditException(LittlePermissionBuild config) {
        super("exception.permission.edit");
        this.config = config;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), config.editBlockLimit.value);
    }
    
}