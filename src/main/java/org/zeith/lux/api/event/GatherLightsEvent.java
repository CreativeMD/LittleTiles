package org.zeith.lux.api.event;

import java.util.ArrayList;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.zeitheron.hammercore.api.lighting.ColoredLight;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Event;

//Modified to not cause compiler errors

public class GatherLightsEvent extends Event {
    private final ArrayList<ColoredLight> lights;
    private final float maxDistance;
    private final Vec3d cameraPosition;
    private final Frustum camera;
    private final float partialTicks;
    
    private double furthest;
    
    public GatherLightsEvent(ArrayList<ColoredLight> lights, float maxDistance, Vec3d cameraPosition, Frustum camera, float partialTicks) {
        this.lights = lights;
        this.maxDistance = maxDistance;
        this.cameraPosition = cameraPosition;
        this.camera = camera;
        this.partialTicks = partialTicks;
    }
    
    public float getPartialTicks() {
        return partialTicks;
    }
    
    public ImmutableList<ColoredLight> getLightList() {
        return ImmutableList.copyOf(lights);
    }
    
    public float getMaxDistance() {
        return maxDistance;
    }
    
    public Vec3d getCameraPosition() {
        return cameraPosition;
    }
    
    public ICamera getCamera() {
        return camera;
    }
    
    public void addAll(Stream<ColoredLight> lights) {
        lights.forEach(this::add);
    }
    
    public void addAll(Iterable<ColoredLight> lights) {
        lights.forEach(this::add);
    }
    
    public void add(ColoredLight light) {
        
    }
    
    public void add(ColoredLight.Builder builder) {
        if (builder == null)
            return;
        add(builder.build());
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }
}
