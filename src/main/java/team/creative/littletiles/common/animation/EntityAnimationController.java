package team.creative.littletiles.common.animation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import team.creative.littletiles.common.animation.entity.EntityAnimation;
import team.creative.littletiles.common.math.transformation.LittleTransformation;
import team.creative.littletiles.common.structure.IAnimatedStructure;

public abstract class EntityAnimationController extends AnimationController {
    
    private static HashMap<String, Class<? extends EntityAnimationController>> controllerTypes = new HashMap<>();
    private static HashMap<Class<? extends EntityAnimationController>, String> controllerTypesInv = new HashMap<>();
    
    public static void registerControllerType(String id, Class<? extends EntityAnimationController> controllerType) {
        controllerTypes.put(id, controllerType);
        controllerTypesInv.put(controllerType, id);
    }
    
    static {
        registerControllerType("door", DoorController.class);
    }
    
    public EntityAnimationController() {
        
    }
    
    public EntityAnimation parent;
    
    public void setParent(EntityAnimation parent) {
        this.parent = parent;
        if (parent.structure != null && parent.structure instanceof IAnimatedStructure)
            ((IAnimatedStructure) parent.structure).setAnimation(parent);
    }
    
    public CompoundTag save(CompoundTag nbt) {
        nbt.putString("id", controllerTypesInv.get(this.getClass()));
        saveExtra(nbt);
        return nbt;
    }
    
    protected abstract void saveExtra(CompoundTag nbt);
    
    protected abstract void load(CompoundTag nbt);
    
    public Player activator() {
        return null;
    }
    
    public boolean noClip() {
        return false;
    }
    
    public void onServerApproves() {
        
    }
    
    public void onServerPlaces() {
        
    }
    
    public abstract void transform(LittleTransformation transformation);
    
    public static EntityAnimationController parseController(EntityAnimation animation, CompoundTag nbt) {
        Class<? extends EntityAnimationController> controllerType = controllerTypes.get(nbt.getString("id"));
        if (controllerType == null)
            throw new RuntimeException("Unkown controller type '" + nbt.getString("id") + "'");
        
        try {
            EntityAnimationController controller = controllerType.getConstructor().newInstance();
            controller.setParent(animation);
            controller.load(nbt);
            return controller;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
