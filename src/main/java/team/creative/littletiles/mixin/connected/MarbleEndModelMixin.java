package team.creative.littletiles.mixin.connected;

import java.io.Reader;
import java.util.Map;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import team.creative.littletiles.client.render.material.ConnectedEndModels;
import team.creative.littletiles.client.render.material.RotatedCutFaces;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.creative.littletiles.client.render.material.MarbleEndModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ModelManager.class, remap = false)
public abstract class MarbleEndModelMixin {
    @org.spongepowered.asm.mixin.injection.Inject(method = "lambda$reload$0", at = @At("HEAD"), require = 1, allow = 1)
    private void connected$completeEndModels(net.minecraft.util.profiling.ProfilerFiller profiler,
            Map<ResourceLocation, net.minecraft.client.renderer.block.model.BlockModel> models,
            Map<ResourceLocation, java.util.List<net.minecraft.client.resources.model.BlockStateModelLoader.LoadedJson>> states,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<ModelBakery> result,
            @Share("ltRotatedCutFaces") LocalRef<Map<BlockState, RotatedCutFaces.StateDonor>> cutFaces) {
        var supportedStates = ConnectedEndModels.supportedStates(states);
        cutFaces.set(RotatedCutFaces.resolve(RotatedCutFaces.find(models, supportedStates)));
        ConnectedEndModels.complete(models, supportedStates);
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "lambda$reload$0", at = @At("RETURN"), require = 1, allow = 1)
    private void connected$prepareCutFaces(org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<ModelBakery> result,
            @Share("ltRotatedCutFaces") LocalRef<Map<BlockState, RotatedCutFaces.StateDonor>> cutFaces) {
        RotatedCutFaces.prepare(result.getReturnValue(), cutFaces.get());
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "apply", at = @At("TAIL"), require = 1, allow = 1)
    private void connected$applyCutFaces(CallbackInfo result) {
        RotatedCutFaces.apply(((ModelManager) (Object) this).getModelBakery());
    }

    @ModifyArg(method = "lambda$loadBlockModels$8(Ljava/util/Map$Entry;)Lcom/mojang/datafixers/util/Pair;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/model/BlockModel;fromStream(Ljava/io/Reader;)Lnet/minecraft/client/renderer/block/model/BlockModel;"),
        index = 0, require = 1, allow = 1)
    private static Reader yuushyaConnected$marbleCaps(Reader reader, @Local(argsOnly = true) Map.Entry<ResourceLocation, ?> entry) {
        return MarbleEndModel.complete(entry.getKey(), reader);
    }
}
