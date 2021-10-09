package com.creativemd.littletiles.common.util.tooltip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import team.creative.creativecore.client.render.GuiRenderHelper;

public class ActionMessage {
    
    private static HashMap<Class, ActionMessageObjectType> typeClasses = new HashMap<>();
    private static List<ActionMessageObjectType> types = new ArrayList<>();
    
    public static void registerType(ActionMessageObjectType type) {
        type.index = types.size();
        
        typeClasses.put(type.clazz, type);
        types.add(type);
    }
    
    public static ActionMessageObjectType getType(int index) {
        return types.get(index);
    }
    
    public static ActionMessageObjectType getType(Object object) {
        ActionMessageObjectType type = typeClasses.get(object.getClass());
        if (type == null)
            throw new RuntimeException("Invalid object in action message " + object + " clazz: " + object.getClass());
        return type;
    }
    
    public String text;
    public Object[] objects;
    
    public ActionMessage(String text, Object[] objects) {
        this.text = text;
        this.objects = objects;
    }
    
    static {
        registerType(new ActionMessageObjectType<ItemStack>(ItemStack.class) {
            
            @Override
            public void write(ItemStack object, FriendlyByteBuf buf) {
                buf.writeItem(object);
            }
            
            @Override
            public ItemStack read(FriendlyByteBuf buf) {
                return buf.readItem();
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            public int width(ItemStack object) {
                return 20;
            }
            
            @Override
            @OnlyIn(Dist.CLIENT)
            public void render(PoseStack stack, ItemStack object, int color, float alpha) {
                stack.pushPose();
                stack.translate(2, -4, 0);
                GuiRenderHelper.drawItemStack(stack, object, alpha);
                stack.popPose();
            }
            
        });
    }
    
    public static abstract class ActionMessageObjectType<T> {
        
        public final Class<T> clazz;
        private int index;
        
        public ActionMessageObjectType(Class<T> clazz) {
            this.clazz = clazz;
        }
        
        public int index() {
            return index;
        }
        
        public abstract void write(T object, FriendlyByteBuf buf);
        
        public abstract T read(FriendlyByteBuf buf);
        
        @OnlyIn(Dist.CLIENT)
        public abstract int width(T object);
        
        @OnlyIn(Dist.CLIENT)
        public abstract void render(PoseStack stack, T object, int color, float alpha);
        
    }
    
}
