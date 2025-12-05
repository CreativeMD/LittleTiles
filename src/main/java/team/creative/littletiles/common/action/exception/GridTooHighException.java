package team.creative.littletiles.common.action.exception;

import team.creative.creativecore.common.util.mc.LanguageUtils;
import team.creative.littletiles.common.config.LittlePermissionBuild;

public class GridTooHighException extends LittleActionException {
    
    public LittlePermissionBuild config;
    public int attempted;
    
    public GridTooHighException(LittlePermissionBuild config, int attempted) {
        super("exception.permission.grid");
        this.config = config;
        this.attempted = attempted;
    }
    
    @Override
    public String getLocalizedMessage() {
        return LanguageUtils.translate(getMessage(), attempted, config.gridLimit.value);
    }
    
}