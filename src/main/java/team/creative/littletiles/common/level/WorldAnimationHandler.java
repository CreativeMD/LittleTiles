package team.creative.littletiles.common.level;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.spongepowered.asm.mixin.MixinEnvironment.Side;

import com.creativemd.creativecore.common.packet.PacketHandler;
import com.creativemd.creativecore.common.utils.math.box.CreativeAxisAlignedBB;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.WorldTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.StartTracking;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.creative.creativecore.common.level.IOrientatedLevel;
import team.creative.littletiles.client.event.HoldLeftClick;
import team.creative.littletiles.client.event.LeftClick;
import team.creative.littletiles.client.event.WheelClick;
import team.creative.littletiles.client.level.LittleAnimationHandlerClient;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.packet.LittleEntityRequestPacket;
import team.creative.littletiles.server.level.LittleAnimationHandlerServer;

public class WorldAnimationHandler {
    
    @SideOnly(Side.CLIENT)
    public static LittleAnimationHandler client;
    public static LinkedHashMap<Level, LittleAnimationHandler> levels = new LinkedHashMap<>();
    
    @SideOnly(Side.CLIENT)
    private static void createClientHandler() {
        client = new LittleAnimationHandlerClient(Minecraft.getMinecraft().world);
    }
    
    @SideOnly(Side.CLIENT)
    public static LittleAnimationHandler getHandlerClient() {
        if (client == null)
            createClientHandler();
        return client;
    }
    
    public static LittleAnimationHandler getHandler(Level level) {
        if (level.isClientSide)
            return getHandlerClient();
        
        if (level instanceof IOrientatedLevel)
            level = ((IOrientatedLevel) level).getRealLevel();
        LittleAnimationHandler handler = levels.get(level);
        if (handler == null) {
            handler = new LittleAnimationHandlerServer(level);
            levels.put(level, handler);
        }
        return handler;
    }
    
    public static EntityAnimation findAnimation(boolean client, UUID uuid) {
        if (client)
            return findAnimationClient(uuid);
        return findAnimationServer(uuid);
    }
    
    @SideOnly(Side.CLIENT)
    public static EntityAnimation findAnimationClient(UUID uuid) {
        return getHandlerClient().findAnimation(uuid);
    }
    
    public static EntityAnimation findAnimationServer(UUID uuid) {
        for (LittleAnimationHandler handler : worlds.values()) {
            EntityAnimation animation = handler.findAnimation(uuid);
            if (animation != null)
                return animation;
        }
        return null;
    }
    
    @SubscribeEvent
    public static void chunkUnload(ChunkEvent.Unload event) {
        if (event.getWorld().isRemote) {
            if (client != null)
                client.chunkUnload(event);
        } else {
            LittleAnimationHandler handler = worlds.get(event.getWorld());
            if (handler != null)
                handler.chunkUnload(event);
        }
    }
    
    @SubscribeEvent
    public static void worldCollision(GetCollisionBoxesEvent event) {
        if (event.getWorld().isRemote) {
            if (client != null)
                client.worldCollision(event);
        } else {
            LittleAnimationHandler handler = worlds.get(event.getWorld());
            if (handler != null)
                handler.worldCollision(event);
        }
    }
    
    @SubscribeEvent
    public static void unloadWorld(Unload event) {
        if (event.getWorld().isRemote) {
            if (client != null)
                client.worldUnload(event);
            client = null;
        } else {
            LittleAnimationHandler handler = worlds.get(event.getWorld());
            if (handler != null)
                handler.worldUnload(event);
            worlds.remove(event.getWorld());
        }
    }
    
    @SubscribeEvent
    public static void trackEntity(StartTracking event) {
        if (event.getTarget() instanceof EntityAnimation && ((EntityAnimation) event.getTarget()).controller.activator() != event.getEntityPlayer()) {
            EntityAnimation animation = (EntityAnimation) event.getTarget();
            PacketHandler.sendPacketToPlayer(new LittleEntityRequestPacket(animation.getUniqueID(), animation
                    .writeToNBT(new NBTTagCompound()), animation.enteredAsChild), (EntityPlayerMP) event.getEntityPlayer());
        }
    }
    
    @SubscribeEvent
    public static void tick(WorldTickEvent event) {
        if (!event.world.isRemote) {
            LittleAnimationHandler handler = worlds.get(event.world);
            if (handler != null)
                ((LittleAnimationHandlerServer) handler).tick(event);
        }
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void rightClick(PlayerInteractEvent event) {
        if (event.getWorld().isRemote && client != null && event.getHand() == EnumHand.MAIN_HAND)
            ((LittleAnimationHandlerClient) client).rightClick(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void mouseWheel(WheelClick event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).mouseWheel(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void holdClick(HoldLeftClick event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).holdClick(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void leftClick(LeftClick event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).leftClick(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void tickClient(ClientTickEvent event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).tickClient(event);
    }
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void renderLast(RenderWorldLastEvent event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).renderLast(event);
    }
    
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public static void drawHighlight(DrawBlockHighlightEvent event) {
        if (client != null)
            ((LittleAnimationHandlerClient) client).drawHighlight(event);
    }
    
    private static Field wasPushedByDoor = ReflectionHelper.findField(EntityPlayerMP.class, "wasPushedByDoor");
    
    public static void setPushedByDoor(EntityPlayerMP player) {
        try {
            wasPushedByDoor.setInt(player, 10);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean checkIfEmpty(List<AxisAlignedBB> boxes, EntityPlayerMP player) {
        try {
            if (wasPushedByDoor.getInt(player) > 0)
                return true;
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (boxes.isEmpty())
            return true;
        
        for (int i = 0; i < boxes.size(); i++)
            if (!(boxes.get(i) instanceof CreativeAxisAlignedBB))
                return false;
        return true;
    }
}
