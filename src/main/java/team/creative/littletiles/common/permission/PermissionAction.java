package team.creative.littletiles.common.permission;

import net.minecraft.world.entity.player.Player;

public abstract class PermissionAction {
    
    public abstract void test(Player player) throws PermissionException;
    
}
