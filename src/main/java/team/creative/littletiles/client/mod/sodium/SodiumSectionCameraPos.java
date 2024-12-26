package team.creative.littletiles.client.mod.sodium;

import org.joml.Vector3dc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.caffeinemc.mods.sodium.client.render.chunk.translucent_sorting.data.CombinedCameraPos;

public class SodiumSectionCameraPos implements CombinedCameraPos {
    
    protected final Vector3dc absoluteCameraPos;
    protected final Vector3fc cameraPos;
    
    public SodiumSectionCameraPos(Vector3dc absolute, int x, int y, int z) {
        this.absoluteCameraPos = absolute;
        this.cameraPos = new Vector3f((float) (absoluteCameraPos.x() - x), (float) (absoluteCameraPos.y() - y), (float) (absoluteCameraPos.z() - z));
    }
    
    @Override
    public Vector3fc getRelativeCameraPos() {
        return this.cameraPos;
    }
    
    @Override
    public Vector3dc getAbsoluteCameraPos() {
        return this.absoluteCameraPos;
    }
    
}
