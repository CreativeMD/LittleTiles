package team.creative.littletiles.mixin.oculus;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.coderbot.iris.compat.sodium.impl.vertex_format.terrain_xhfp.XHFPTerrainVertex;
import net.coderbot.iris.vertices.ExtendedDataHelper;
import team.creative.creativecore.common.util.math.vec.Vec3d;

@Mixin(XHFPTerrainVertex.class)
public class XHFPTerrainVertexMixin {
    
    @Unique
    public Vec3d center;
    
    @Redirect(remap = false, at = @At(value = "INVOKE", target = "Lnet/coderbot/iris/vertices/ExtendedDataHelper;computeMidBlock(FFFIII)I"),
            method = "write(JLme/jellysquid/mods/sodium/client/render/vertex/type/ChunkVertexEncoder$Vertex;I)J")
    public int computeMidBlock(float x, float y, float z, int localPosX, int localPosY, int localPosZ) {
        if (center != null)
            return ExtendedDataHelper.packMidBlock((float) (localPosX + center.x), (float) (localPosY + center.y), (float) (localPosZ + center.z));
        return ExtendedDataHelper.computeMidBlock(x, y, z, localPosX, localPosY, localPosZ);
    }
    
    public void setCenter(Vec3d center) {
        this.center = center;
    }
    
}
