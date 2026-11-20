package team.creative.littletiles.mixin.connected;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

/** Probe bytecode without initializing optional client backends. */
public final class ConnectedTexturesMixinPlugin implements IMixinConfigPlugin {
    private boolean continuity;
    private boolean fusion;
    private boolean sodium;
    public static boolean present(String name) {
        try { return MixinService.getService().getBytecodeProvider().getClassNode(name) != null; }
        catch (Exception | LinkageError absent) { return false; }
    }
    @Override public void onLoad(String packageName) {
        fusion = present("com.supermartijn642.fusion.Fusion");
        continuity = present("me.pepperbell.continuity.client.model.CtmBakedModel")
            && present("net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel");
        sodium = present("net.caffeinemc.mods.sodium.client.world.LevelSlice");
    }
    @Override public boolean shouldApplyMixin(String target, String mixin) {
        if (mixin.endsWith(".MarbleEndModelMixin")) return fusion;
        if (mixin.endsWith("SodiumRenderViewMixin")) return sodium;
        if (mixin.endsWith("BakedModelMixin") || mixin.endsWith("ProcessingContextMixin")) return continuity;
        return true;
    }
    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> own, Set<String> others) {}
    @Override public List<String> getMixins() { return null; }
    @Override public void preApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
    @Override public void postApply(String target, ClassNode node, String mixin, IMixinInfo info) {}
}
