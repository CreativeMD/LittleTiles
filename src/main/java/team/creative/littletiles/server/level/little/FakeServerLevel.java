package team.creative.littletiles.server.level.little;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import team.creative.creativecore.common.util.math.matrix.IVecOrigin;
import team.creative.creativecore.common.util.math.matrix.VecOrigin;
import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.level.little.FakeClientLevel;
import team.creative.littletiles.common.level.little.LittleLevel;

public class FakeServerLevel extends LittleServerLevel {
    
    public static LittleLevel createFakeLevel(String name, boolean client) {
        if (client)
            return createClient(name);
        return new FakeServerLevel(ServerLifecycleHooks.getCurrentServer(), new FakeServerLevelInfo(Difficulty.PEACEFUL, false, true), false, 0);
    }
    
    public static FakeClientLevel createClient(String name) {
        return FakeClientLevel.createFakeWorldClient(name, new ClientLevelData(Difficulty.PEACEFUL, false, true));
    }
    
    private final ServerScoreboard scoreboard;
    private DimensionSpecialEffects effects;
    
    protected FakeServerLevel(MinecraftServer server, ServerLevelData worldInfo, boolean debug, long seed) {
        super(server, worldInfo, OVERWORLD, debug, seed, server.registryAccess());
        this.scoreboard = new ServerScoreboard(server);
    }
    
    @Override
    public UUID key() {
        return null;
    }
    
    @Override
    public IVecOrigin getOrigin() {
        return origin;
    }
    
    @Override
    public void setOrigin(Vec3d vec) {
        this.origin = new VecOrigin(vec);
    }
    
    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return this.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
    }
    
    @Override
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        boolean flag = effects.constantAmbientLight();
        if (!p_230487_2_) {
            return flag ? 0.9F : 1.0F;
        } else {
            switch (p_230487_1_) {
                case DOWN:
                    return flag ? 0.9F : 0.5F;
                case UP:
                    return flag ? 0.9F : 1.0F;
                case NORTH:
                case SOUTH:
                    return 0.8F;
                case WEST:
                case EAST:
                    return 0.6F;
                default:
                    return 1.0F;
            }
        }
    }
    
    @Override
    public void playSound(Player p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundSource p_184148_9_, float p_184148_10_, float p_184148_11_) {}
    
    @Override
    public void playSound(Player p_217384_1_, Entity p_217384_2_, SoundEvent p_217384_3_, SoundSource p_217384_4_, float p_217384_5_, float p_217384_6_) {}
    
    @Override
    public void playSeededSound(Player p_220363_, double p_220364_, double p_220365_, double p_220366_, SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {}
    
    @Override
    public void playSeededSound(Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_220374_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {}
    
    @Override
    public ServerScoreboard getScoreboard() {
        return scoreboard;
    }
    
    @Override
    public RecipeManager getRecipeManager() {
        if (isClientSide)
            return Minecraft.getInstance().getConnection().getRecipeManager();
        return getServer().getRecipeManager();
    }
    
    @Override
    public void levelEvent(Player p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {}
    
    @Override
    public void gameEvent(Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {}
    
    @Override
    public MapItemSavedData getMapData(String id) {
        return null;
    }
    
    @Override
    public void setMapData(String id, MapItemSavedData data) {}
    
    @Override
    public String toString() {
        return "FakeServerLevel";
    }
    
}
