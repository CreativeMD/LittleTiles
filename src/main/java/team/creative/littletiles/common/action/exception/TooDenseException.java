package team.creative.littletiles.common.action.exception;

import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.LittleTiles;

public class TooDenseException extends LittleActionException {
    
    public TooDenseException() {
        super("exception.permission.density");
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), LittleTiles.CONFIG.general.maxAllowedDensity);
    }
    
}