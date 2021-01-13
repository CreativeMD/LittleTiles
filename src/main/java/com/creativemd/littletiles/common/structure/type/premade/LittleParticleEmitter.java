package com.creativemd.littletiles.common.structure.type.premade;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.vecmath.Vector3d;

import com.creativemd.creativecore.client.rendering.RenderBox;
import com.creativemd.creativecore.common.utils.math.Rotation;
import com.creativemd.creativecore.common.utils.math.RotationUtils;
import com.creativemd.creativecore.common.utils.mc.ColorUtils;
import com.creativemd.creativecore.common.world.IOrientatedWorld;
import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.client.gui.handler.LittleStructureGuiHandler;
import com.creativemd.littletiles.common.action.LittleActionException;
import com.creativemd.littletiles.common.action.block.LittleActionActivated;
import com.creativemd.littletiles.common.item.ItemLittleWrench;
import com.creativemd.littletiles.common.particle.LittleParticle;
import com.creativemd.littletiles.common.particle.LittleParticlePresets;
import com.creativemd.littletiles.common.particle.LittleParticleTexture;
import com.creativemd.littletiles.common.structure.LittleStructure;
import com.creativemd.littletiles.common.structure.directional.StructureDirectional;
import com.creativemd.littletiles.common.structure.registry.LittleStructureType;
import com.creativemd.littletiles.common.tile.LittleTile;
import com.creativemd.littletiles.common.tile.math.box.LittleBox;
import com.creativemd.littletiles.common.tile.parent.IStructureTileList;
import com.creativemd.littletiles.common.tile.place.PlacePreview;
import com.creativemd.littletiles.common.tile.place.PlacePreviewFacing;
import com.creativemd.littletiles.common.tile.preview.LittlePreviews;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class LittleParticleEmitter extends LittleStructurePremade {
    
    @StructureDirectional
    public EnumFacing facing = EnumFacing.UP;
    public ParticleSettings settings = new ParticleSettings();
    public ParticleSpread spread = new ParticleSpreadRandom();
    public int delay = 10;
    public int count = 1;
    protected int ticker = 0;
    
    public LittleParticleEmitter(LittleStructureType type, IStructureTileList mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public void tick() {
        if (getOutput(0).getState()[0])
            return;
        if (ticker >= delay) {
            if (getWorld().isRemote)
                for (int i = 0; i < count; i++)
                    spawnParticle(getWorld());
                
            ticker = 0;
        } else
            ticker++;
    }
    
    @Override
    public boolean onBlockActivated(World worldIn, LittleTile tile, BlockPos pos, EntityPlayer playerIn, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ, LittleActionActivated action) throws LittleActionException {
        if (!worldIn.isRemote)
            LittleStructureGuiHandler.openGui("particle", new NBTTagCompound(), playerIn, this);
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    private static Method spawnParticle0;
    
    @SideOnly(Side.CLIENT)
    private static Field particleMaxAge;
    
    @SideOnly(Side.CLIENT)
    public void spawnParticle(World world) {
        Minecraft mc = Minecraft.getMinecraft();
        
        if (Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() instanceof ItemLittleWrench || Minecraft.getMinecraft().player.getHeldItemOffhand()
            .getItem() instanceof ItemLittleWrench)
            return;
        
        if (spawnParticle0 == null)
            spawnParticle0 = ReflectionHelper
                .findMethod(RenderGlobal.class, "spawnParticle0", "func_190571_b", int.class, boolean.class, boolean.class, double.class, double.class, double.class, double.class, double.class, double.class, int[].class);
        
        if (particleMaxAge == null)
            particleMaxAge = ReflectionHelper.findField(Particle.class, new String[] { "particleMaxAge", "field_70547_e" });
        
        try {
            AxisAlignedBB bb = getSurroundingBox().getAABB();
            Vector3d pos = new Vector3d(0, 0.5, 0);
            Vector3d speed = spread.generate();
            
            Rotation rotation = null;
            switch (facing) {
            case DOWN:
                pos.scale(-1);
                speed.scale(-1);
                break;
            case EAST:
                rotation = Rotation.Z_COUNTER_CLOCKWISE;
                break;
            case WEST:
                rotation = Rotation.Z_CLOCKWISE;
                break;
            case SOUTH:
                rotation = Rotation.X_CLOCKWISE;
                break;
            case NORTH:
                rotation = Rotation.X_COUNTER_CLOCKWISE;
                break;
            }
            
            if (rotation != null) {
                RotationUtils.rotate(pos, rotation);
                RotationUtils.rotate(speed, rotation);
            }
            
            pos.x *= bb.maxX - bb.minX;
            pos.y *= bb.maxY - bb.minY;
            pos.z *= bb.maxZ - bb.minZ;
            pos.x += (bb.minX + bb.maxX) / 2;
            pos.y += (bb.minY + bb.maxY) / 2;
            pos.z += (bb.minZ + bb.maxZ) / 2;
            
            if (world instanceof IOrientatedWorld) {
                ((IOrientatedWorld) world).getOrigin().transformPointToWorld(pos);
                ((IOrientatedWorld) world).getOrigin().onlyRotateWithoutCenter(speed);
            }
            
            mc.effectRenderer.addEffect(new LittleParticle(world, pos, speed, settings));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadSettings(NBTTagCompound nbt) {
        spread = loadSpread(nbt);
        delay = nbt.getInteger("tickDelay");
        ticker = nbt.getInteger("ticker");
        if (nbt.hasKey("tickCount"))
            count = nbt.getInteger("tickCount");
        else
            count = 1;
        if (nbt.hasKey("settings"))
            settings = new ParticleSettings(nbt.getCompoundTag("settings"));
        else
            settings = LittleParticlePresets.SMOKE.settings.copy();
    }
    
    @Override
    protected void loadFromNBTExtra(NBTTagCompound nbt) {
        loadSettings(nbt);
    }
    
    @Override
    protected void writeToNBTExtra(NBTTagCompound nbt) {
        nbt.setInteger("tickDelay", delay);
        nbt.setInteger("tickCount", count);
        nbt.setInteger("ticker", ticker);
        spread.write(nbt);
        NBTTagCompound settingsData = new NBTTagCompound();
        settings.write(settingsData);
        nbt.setTag("settings", settingsData);
    }
    
    public static class ParticleSettings {
        
        public float gravity = 0;
        public int color = ColorUtils.RGBAToInt(20, 20, 20, 255);
        public int lifetime = 40;
        public int lifetimeDeviation = 5;
        public float startSize = 0.4F;
        public float endSize = 0.5F;
        public float sizeDeviation = 0.04F;
        public LittleParticleTexture texture = LittleParticleTexture.dust_fade_out;
        public boolean randomColor = false;
        
        public ParticleSettings() {
            
        }
        
        public ParticleSettings(float gravity, int color, int lifetime, int lifetimeDeviation, float startSize, float endSize, float sizeDeviation, LittleParticleTexture texture, boolean randomColor) {
            this.gravity = gravity;
            this.color = color;
            this.lifetime = lifetime;
            this.lifetimeDeviation = lifetimeDeviation;
            this.startSize = startSize;
            this.endSize = endSize;
            this.sizeDeviation = sizeDeviation;
            this.texture = texture;
            this.randomColor = randomColor;
        }
        
        public ParticleSettings(NBTTagCompound nbt) {
            gravity = nbt.getFloat("gravity");
            color = nbt.getInteger("color");
            lifetime = nbt.getInteger("lifetime");
            lifetimeDeviation = nbt.getInteger("lifetimeDeviation");
            startSize = nbt.getFloat("startSize");
            endSize = nbt.getFloat("endSize");
            sizeDeviation = nbt.getFloat("sizeDeviation");
            randomColor = nbt.getBoolean("randomColor");
            texture = LittleParticleTexture.get(nbt.getString("texture"));
        }
        
        public void write(NBTTagCompound nbt) {
            nbt.setFloat("gravity", gravity);
            nbt.setInteger("color", color);
            nbt.setInteger("lifetime", lifetime);
            nbt.setInteger("lifetimeDeviation", lifetimeDeviation);
            nbt.setFloat("startSize", startSize);
            nbt.setFloat("endSize", endSize);
            nbt.setFloat("sizeDeviation", sizeDeviation);
            nbt.setString("texture", texture.name());
            nbt.setBoolean("randomColor", randomColor);
        }
        
        public ParticleSettings copy() {
            return new ParticleSettings(gravity, color, lifetime, lifetimeDeviation, startSize, endSize, sizeDeviation, texture, randomColor);
        }
    }
    
    public static ParticleSpread loadSpread(NBTTagCompound nbt) {
        ParticleSpread spread = null;
        for (int i = parser.size() - 1; i >= 0; --i) {
            spread = parser.get(i).apply(nbt);
            if (spread != null)
                break;
        }
        if (spread == null)
            spread = new ParticleSpreadRandom();
        spread.speedY = nbt.getFloat("speedY");
        spread.spread = nbt.getFloat("spread");
        return spread;
    }
    
    private static List<Function<NBTTagCompound, ParticleSpread>> parser = new ArrayList<>();
    
    public static void registerParticleSpreadParser(Function<NBTTagCompound, ParticleSpread> function) {
        parser.add(function);
    }
    
    static {
        registerParticleSpreadParser((x) -> {
            ParticleSpreadRandom spread = new ParticleSpreadRandom();
            
            spread.speedX = x.getFloat("speedX");
            spread.speedZ = x.getFloat("speedZ");
            return spread;
        });
        registerParticleSpreadParser((x) -> {
            if (!x.hasKey("radius"))
                return null;
            
            ParticleSpreadCircular spread = new ParticleSpreadCircular();
            spread.radius = x.getFloat("radius");
            spread.angle = x.getFloat("angle");
            spread.steps = x.getInteger("steps");
            return spread;
        });
    }
    
    public static abstract class ParticleSpread {
        
        public float speedY = 0.1F;
        public float spread = 0.1F;
        
        protected abstract void populate(Vector3d vec);
        
        public Vector3d generate() {
            Vector3d vec = new Vector3d();
            vec.y = speedY;
            populate(vec);
            float half = spread / 2;
            vec.x += Math.random() * spread - half;
            vec.y += Math.random() * spread - half;
            vec.z += Math.random() * spread - half;
            return vec;
        }
        
        public void write(NBTTagCompound nbt) {
            nbt.setFloat("speedY", speedY);
            nbt.setFloat("spread", spread);
        }
        
    }
    
    public static class ParticleSpreadRandom extends ParticleSpread {
        
        public float speedX = 0F;
        public float speedZ = 0F;
        
        public ParticleSpreadRandom() {
            
        }
        
        public ParticleSpreadRandom(float power, float x, float z, float deviation) {
            this.speedY = power;
            this.speedX = x;
            this.speedZ = z;
            this.spread = deviation;
        }
        
        @Override
        protected void populate(Vector3d vec) {
            vec.x = speedX;
            vec.z = speedZ;
        }
        
        @Override
        public void write(NBTTagCompound nbt) {
            super.write(nbt);
            nbt.setFloat("speedX", speedX);
            nbt.setFloat("speedZ", speedZ);
        }
        
    }
    
    public static class ParticleSpreadCircular extends ParticleSpread {
        
        public float radius = 0.1F;
        public float angle = 0;
        public int steps = 30;
        
        public ParticleSpreadCircular() {
            
        }
        
        public ParticleSpreadCircular(float power, float radius, float angle, int steps, float deviation) {
            this.speedY = power;
            this.radius = radius;
            this.angle = angle;
            this.steps = steps;
            this.spread = deviation;
        }
        
        @Override
        protected void populate(Vector3d vec) {
            vec.x = Math.cos(angle) * radius;
            vec.z = Math.sin(angle) * radius;
            angle += (Math.PI * 2) / steps;
        }
        
        @Override
        public void write(NBTTagCompound nbt) {
            super.write(nbt);
            nbt.setFloat("radius", radius);
            nbt.setFloat("degree", angle);
            nbt.setInteger("steps", steps);
        }
    }
    
    public static class LittleStructureTypeParticleEmitter extends LittleStructureTypePremade {
        
        @SideOnly(Side.CLIENT)
        public List<RenderBox> cubes;
        
        public LittleStructureTypeParticleEmitter(String id, String category, Class<? extends LittleStructure> structureClass, int attribute, String modid) {
            super(id, category, structureClass, attribute, modid);
        }
        
        @Override
        public List<PlacePreview> getSpecialTiles(LittlePreviews previews) {
            List<PlacePreview> result = super.getSpecialTiles(previews);
            EnumFacing facing = (EnumFacing) loadDirectional(previews, "facing");
            LittleBox box = previews.getSurroundingBox();
            result.add(new PlacePreviewFacing(box, facing, ColorUtils.RED));
            return result;
        }
        
        @Override
        @SideOnly(Side.CLIENT)
        public List<RenderBox> getRenderingCubes(LittlePreviews previews) {
            if (cubes == null) {
                //float size = (float) ((Math.sqrt(bandwidth) * 1F / 32F) * 1.4);
                cubes = new ArrayList<>();
                cubes.add(new RenderBox(0.2F, 0.2F, 0.2F, 0.8F, 0.8F, 0.8F, LittleTiles.dyeableBlock).setColor(-13619152));
            }
            return cubes;
        }
        
    }
}
