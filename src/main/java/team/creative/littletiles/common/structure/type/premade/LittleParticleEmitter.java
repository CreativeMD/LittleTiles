package team.creative.littletiles.common.structure.type.premade;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.box.RenderBox;
import team.creative.creativecore.common.gui.creator.GuiCreator;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.transformation.Rotation;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.creativecore.common.util.mc.ColorUtils;
import team.creative.littletiles.LittleTilesRegistry;
import team.creative.littletiles.common.block.little.tile.LittleTileContext;
import team.creative.littletiles.common.block.little.tile.group.LittleGroup;
import team.creative.littletiles.common.block.little.tile.parent.IStructureParentCollection;
import team.creative.littletiles.common.entity.particle.LittleParticle;
import team.creative.littletiles.common.entity.particle.LittleParticlePresets;
import team.creative.littletiles.common.entity.particle.LittleParticleTexture;
import team.creative.littletiles.common.gui.handler.LittleStructureGuiCreator;
import team.creative.littletiles.common.gui.structure.GuiParticle;
import team.creative.littletiles.common.item.ItemLittleWrench;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBox;
import team.creative.littletiles.common.placement.box.LittlePlaceBoxFacing;
import team.creative.littletiles.common.structure.attribute.LittleAttributeBuilder;
import team.creative.littletiles.common.structure.directional.StructureDirectional;

public class LittleParticleEmitter extends LittleStructurePremade {
    
    public static final LittleStructureGuiCreator GUI = GuiCreator
            .register("particle", new LittleStructureGuiCreator((nbt, player, structure) -> new GuiParticle((LittleParticleEmitter) structure)));
    
    @StructureDirectional
    public Facing facing = Facing.UP;
    public ParticleSettings settings = new ParticleSettings();
    public ParticleSpread spread = new ParticleSpreadRandom();
    public int delay = 10;
    public int count = 1;
    protected int ticker = 0;
    
    public LittleParticleEmitter(LittleStructureTypeParticleEmitter type, IStructureParentCollection mainBlock) {
        super(type, mainBlock);
    }
    
    @Override
    public void tick() {
        if (getOutput(0).getState().any())
            return;
        if (ticker >= delay) {
            if (isClient())
                for (int i = 0; i < count; i++)
                    spawnParticle(getLevel());
                
            ticker = 0;
        } else
            ticker++;
    }
    
    @Override
    public boolean canInteract() {
        return true;
    }
    
    @Override
    public InteractionResult use(Level level, LittleTileContext context, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide)
            GUI.open(player, this);
        return InteractionResult.SUCCESS;
    }
    
    @OnlyIn(Dist.CLIENT)
    public void spawnParticle(Level level) {
        Minecraft mc = Minecraft.getInstance();
        
        if (mc.player.getMainHandItem().getItem() instanceof ItemLittleWrench || mc.player.getOffhandItem().getItem() instanceof ItemLittleWrench)
            return;
        
        try {
            AABB bb = getSurroundingBox().getAABB();
            Vec3d pos = new Vec3d(0, 0.5, 0);
            Vec3d speed = spread.generate();
            
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
                case UP:
                    break;
                default:
                    break;
            }
            
            if (rotation != null) {
                rotation.transform(pos);
                rotation.transform(speed);
            }
            
            pos.x *= bb.maxX - bb.minX;
            pos.y *= bb.maxY - bb.minY;
            pos.z *= bb.maxZ - bb.minZ;
            pos.x += (bb.minX + bb.maxX) / 2;
            pos.y += (bb.minY + bb.maxY) / 2;
            pos.z += (bb.minZ + bb.maxZ) / 2;
            
            if (level instanceof IOrientatedLevel) {
                ((IOrientatedLevel) level).getOrigin().transformPointToWorld(pos);
                ((IOrientatedLevel) level).getOrigin().onlyRotateWithoutCenter(speed);
            }
            
            mc.particleEngine.add(new LittleParticle((ClientLevel) level, pos, speed, settings));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadSettings(CompoundTag nbt) {
        spread = loadSpread(nbt);
        delay = nbt.getInt("tickDelay");
        ticker = nbt.getInt("ticker");
        if (nbt.contains("tickCount"))
            count = nbt.getInt("tickCount");
        else
            count = 1;
        if (nbt.contains("settings"))
            settings = new ParticleSettings(nbt.getCompound("settings"));
        else
            settings = LittleParticlePresets.SMOKE.settings.copy();
    }
    
    @Override
    protected void loadExtra(CompoundTag nbt) {
        loadSettings(nbt);
    }
    
    @Override
    protected void saveExtra(CompoundTag nbt) {
        nbt.putInt("tickDelay", delay);
        nbt.putInt("tickCount", count);
        nbt.putInt("ticker", ticker);
        spread.write(nbt);
        CompoundTag settingsData = new CompoundTag();
        settings.write(settingsData);
        nbt.put("settings", settingsData);
    }
    
    public static class ParticleSettings {
        
        public float gravity = 0;
        public int color = ColorUtils.rgba(20, 20, 20, 255);
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
        
        public ParticleSettings(CompoundTag nbt) {
            gravity = nbt.getFloat("gravity");
            color = nbt.getInt("color");
            lifetime = nbt.getInt("lifetime");
            lifetimeDeviation = nbt.getInt("lifetimeDeviation");
            startSize = nbt.getFloat("startSize");
            endSize = nbt.getFloat("endSize");
            sizeDeviation = nbt.getFloat("sizeDeviation");
            randomColor = nbt.getBoolean("randomColor");
            texture = LittleParticleTexture.get(nbt.getString("texture"));
        }
        
        public void write(CompoundTag nbt) {
            nbt.putFloat("gravity", gravity);
            nbt.putInt("color", color);
            nbt.putInt("lifetime", lifetime);
            nbt.putInt("lifetimeDeviation", lifetimeDeviation);
            nbt.putFloat("startSize", startSize);
            nbt.putFloat("endSize", endSize);
            nbt.putFloat("sizeDeviation", sizeDeviation);
            nbt.putString("texture", texture.name());
            nbt.putBoolean("randomColor", randomColor);
        }
        
        public ParticleSettings copy() {
            return new ParticleSettings(gravity, color, lifetime, lifetimeDeviation, startSize, endSize, sizeDeviation, texture, randomColor);
        }
    }
    
    public static ParticleSpread loadSpread(CompoundTag nbt) {
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
    
    private static List<Function<CompoundTag, ParticleSpread>> parser = new ArrayList<>();
    
    public static void registerParticleSpreadParser(Function<CompoundTag, ParticleSpread> function) {
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
            if (!x.contains("radius"))
                return null;
            
            ParticleSpreadCircular spread = new ParticleSpreadCircular();
            spread.radius = x.getFloat("radius");
            spread.angle = x.getFloat("angle");
            spread.steps = x.getInt("steps");
            return spread;
        });
    }
    
    public static abstract class ParticleSpread {
        
        public float speedY = 0.1F;
        public float spread = 0.1F;
        
        protected abstract void populate(Vec3d vec);
        
        public Vec3d generate() {
            Vec3d vec = new Vec3d();
            vec.y = speedY;
            populate(vec);
            float half = spread / 2;
            vec.x += Math.random() * spread - half;
            vec.y += Math.random() * spread - half;
            vec.z += Math.random() * spread - half;
            return vec;
        }
        
        public void write(CompoundTag nbt) {
            nbt.putFloat("speedY", speedY);
            nbt.putFloat("spread", spread);
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
        protected void populate(Vec3d vec) {
            vec.x = speedX;
            vec.z = speedZ;
        }
        
        @Override
        public void write(CompoundTag nbt) {
            super.write(nbt);
            nbt.putFloat("speedX", speedX);
            nbt.putFloat("speedZ", speedZ);
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
        protected void populate(Vec3d vec) {
            vec.x = Math.cos(angle) * radius;
            vec.z = Math.sin(angle) * radius;
            angle += (Math.PI * 2) / steps;
        }
        
        @Override
        public void write(CompoundTag nbt) {
            super.write(nbt);
            nbt.putFloat("radius", radius);
            nbt.putFloat("degree", angle);
            nbt.putInt("steps", steps);
        }
    }
    
    public static class LittleStructureTypeParticleEmitter extends LittlePremadeType {
        
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> cubes;
        
        public <T extends LittleParticleEmitter> LittleStructureTypeParticleEmitter(String id, Class<T> structureClass, BiFunction<LittleStructureTypeParticleEmitter, IStructureParentCollection, T> factory, LittleAttributeBuilder attribute, String modid) {
            super(id, structureClass, factory, attribute, modid);
        }
        
        @Override
        public List<LittlePlaceBox> getSpecialBoxes(LittleGroup group) {
            List<LittlePlaceBox> result = super.getSpecialBoxes(group);
            Facing facing = (Facing) loadDirectional(group, "facing");
            LittleBox box = group.getSurroundingBox();
            result.add(new LittlePlaceBoxFacing(box, facing, ColorUtils.RED));
            return result;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public List<RenderBox> getItemPreview(LittleGroup previews, boolean translucent) {
            if (cubes == null) {
                cubes = new ArrayList<>();
                cubes.add(new RenderBox(0.2F, 0.2F, 0.2F, 0.8F, 0.8F, 0.8F, LittleTilesRegistry.CLEAN.get().defaultBlockState()).setColor(-13619152));
            }
            return cubes;
        }
        
    }
}
