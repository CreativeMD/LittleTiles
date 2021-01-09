package com.creativemd.littletiles.client.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class InputEventHandler {
    
    private static Minecraft mc = Minecraft.getMinecraft();
    
    private static final Method sendClickBlockToControllerMethod = ReflectionHelper.findMethod(Minecraft.class, "sendClickBlockToController", "func_147115_a", boolean.class);
    
    @SideOnly(Side.CLIENT)
    public static void onHoldClick(boolean leftClick) {
        try {
            HoldLeftClick event = new HoldLeftClick(mc.world, mc.player, leftClick);
            MinecraftForge.EVENT_BUS.post(event);
            sendClickBlockToControllerMethod.invoke(mc, event.getLeftClickResult());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean onMouseClick(RayTraceResult result, EntityPlayer player, World world) {
        LeftClick event = new LeftClick(world, player, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean onMouseWheelClick(EntityPlayer player, World world) {
        WheelClick event = new WheelClick(world, player);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
    @SideOnly(Side.CLIENT)
    public static boolean onPickBlock(RayTraceResult result, EntityPlayer player, World world) {
        PickBlockEvent event = new PickBlockEvent(world, player, result);
        MinecraftForge.EVENT_BUS.post(event);
        return event.isCanceled();
    }
    
}
