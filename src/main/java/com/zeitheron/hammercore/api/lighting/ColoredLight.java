package com.zeitheron.hammercore.api.lighting;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

//Modified to not cause compiler errors

public class ColoredLight

{
    public static final int FLOAT_SIZE = 3 + 4 + 1;
    
    public float x, y, z;
    public float r, g, b, a;
    public float radius;
    
    public ColoredLight(float x, float y, float z, float r, float g, float b, float a, float radius) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.radius = radius;
    }
    
    public ColoredLight reposition(Entity entity, float partialTicks) {
        x = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks);
        y = (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks);
        z = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
        return this;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static final class Builder {
        private float x = Float.NaN;
        private float y = Float.NaN;
        private float z = Float.NaN;
        
        private float r = Float.NaN;
        private float g = Float.NaN;
        private float b = Float.NaN;
        private float a = Float.NaN;
        
        private float radius = Float.NaN;
        
        public Builder pos(BlockPos pos) {
            return pos(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        }
        
        public Builder pos(Vec3d pos) {
            return pos(pos.x, pos.y, pos.z);
        }
        
        public Builder pos(Entity e) {
            return pos(e.posX + e.width / 2, e.posY + e.height / 2, e.posZ + e.width / 2);
        }
        
        public Builder pos(Entity entity, float partialTicks) {
            float x = (float) (entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks),
                y = (float) (entity.prevPosY + (entity.posY - entity.prevPosY) * partialTicks),
                z = (float) (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
            
            return pos(x + entity.width / 2, y + entity.height / 2, z + entity.width / 2);
        }
        
        public Builder pos(double x, double y, double z) {
            return pos((float) x, (float) y, (float) z);
        }
        
        public Builder pos(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }
        
        public Builder color(Vec3d c, float alpha) {
            return color((float) c.x, (float) c.y, (float) c.z, alpha);
        }
        
        public Builder color(Vec3d c) {
            return color((float) c.x, (float) c.y, (float) c.z, 1F);
        }
        
        public Builder color(int c, boolean hasAlpha) {
            return color(extract(c, 2), extract(c, 1), extract(c, 0), hasAlpha ? extract(c, 3) : 1);
        }
        
        private float extract(int i, int idx) {
            return ((i >> (idx * 8)) & 0xFF) / 255f;
        }
        
        public Builder color(float r, float g, float b) {
            return color(r, g, b, 1f);
        }
        
        public Builder color(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }
        
        public Builder alpha(int alpha) {
            return alpha(alpha / 255F);
        }
        
        public Builder alpha(float alpha) {
            this.a = alpha;
            return this;
        }
        
        public Builder radius(float radius) {
            this.radius = radius;
            return this;
        }
        
        public ColoredLight build() {
            if (Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z) && Float.isFinite(r) && Float.isFinite(g) && Float.isFinite(b) && Float.isFinite(a) && Float.isFinite(radius)) {
                return new ColoredLight(x, y, z, r, g, b, a, radius);
            } else {
                throw new IllegalArgumentException("Position, color, and radius must be set, and cannot be infinite");
            }
        }
    }
}
