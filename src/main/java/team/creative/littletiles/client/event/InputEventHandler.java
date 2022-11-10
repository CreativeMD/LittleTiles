package team.creative.littletiles.client.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

@OnlyIn(Dist.CLIENT)
public class InputEventHandler {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    private static final Method continueAttackMethod = ObfuscationReflectionHelper.findMethod(Minecraft.class, "m_91386_", boolean.class);
    
    // TODO THIS CLASS NEEDS TO BE REWORKED
    public static void onHoldClick(boolean leftClick) {
        try {
            HoldLeftClick event = new HoldLeftClick(mc.level, mc.player, leftClick);
            MinecraftForge.EVENT_BUS.post(event);
            continueAttackMethod.invoke(mc, event.getLeftClickResult());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean onMouseClick(BlockHitResult result, Player player, Level level) {
        LeftClick event = new LeftClick(level, player, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
    public static boolean onMouseWheelClick(Player player, Level level) {
        WheelClick event = new WheelClick(level, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
    public static boolean onPickBlock(BlockHitResult result, Player player, Level level) {
        PickBlockEvent event = new PickBlockEvent(level, player, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
}
