package elucent.albedo.event;

import java.util.ArrayList;

import com.google.common.collect.ImmutableList;

import elucent.albedo.lighting.Light;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

public class GatherLightsEvent extends Event {
    private final ArrayList<Light> lights;
    private final float maxDistance;
    private final Vec3d cameraPosition;
    private final ICamera camera;
    
    public GatherLightsEvent(ArrayList<Light> lights, float maxDistance, Vec3d cameraPosition, ICamera camera) {
        this.lights = lights;
        this.maxDistance = maxDistance;
        this.cameraPosition = cameraPosition;
        this.camera = camera;
    }
    
    public ImmutableList<Light> getLightList() {
        return ImmutableList.copyOf(this.lights);
    }
    
    public float getMaxDistance() {
        return this.maxDistance;
    }
    
    public Vec3d getCameraPosition() {
        return this.cameraPosition;
    }
    
    public ICamera getCamera() {
        return this.camera;
    }
    
    public void add(Light light) {}
    
    @Override
    public boolean isCancelable() {
        return false;
    }
}
