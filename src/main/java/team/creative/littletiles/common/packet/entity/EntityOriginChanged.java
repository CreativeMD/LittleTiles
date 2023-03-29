package team.creative.littletiles.common.packet.entity;

import net.minecraft.world.entity.player.Player;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.littletiles.common.entity.LittleEntity;

public class EntityOriginChanged extends LittleEntityPacket {
    
    public double offX;
    public double offY;
    public double offZ;
    public double rotX;
    public double rotY;
    public double rotZ;
    
    public EntityOriginChanged() {}
    
    public EntityOriginChanged(LittleEntity entity) {
        super(entity);
        IVecOrigin origin = entity.getOrigin();
        offX = origin.offX();
        offY = origin.offY();
        offZ = origin.offZ();
        rotX = origin.rotX();
        rotY = origin.rotY();
        rotZ = origin.rotZ();
    }
    
    @Override
    public void execute(Player player, LittleEntity entity) {
        entity.physic.set(offX, offY, offZ, rotX, rotY, rotZ);
    }
    
}
