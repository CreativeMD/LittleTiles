package team.creative.littletiles.common.item.component;

import java.util.Arrays;
import java.util.function.IntFunction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import team.creative.creativecore.common.util.math.base.Facing;
import team.creative.creativecore.common.util.math.box.BoxCorner;
import team.creative.creativecore.common.util.math.box.BoxEdge;
import team.creative.creativecore.common.util.math.box.BoxFace;
import team.creative.creativecore.common.util.mc.PlayerUtils;
import team.creative.littletiles.client.render.overlay.PreviewRenderer;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.common.math.box.LittleBoxAbsolute;
import team.creative.littletiles.common.math.box.LittleBoxLevel;

public enum TransformMode implements StringRepresentable {
    
    CORNER {
        @Override
        public BoxCorner[] corners(int marked) {
            if (marked >= 0 && marked < BoxCorner.values().length)
                return new BoxCorner[] { BoxCorner.values()[marked] };
            return null;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public int select(PreviewRenderer renderer, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            return renderer.selectBox(box.context, Arrays.asList(corners));
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean move(PreviewRenderer renderer, int marked, ShapePosition position, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            if (marked >= 0 && marked < BoxCorner.values().length) {
                corners[marked] = position.box().copy();
                return true;
            }
            return false;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(PreviewRenderer renderer, PoseStack pose, BlockPos pos, int marked, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            for (int i = 0; i < corners.length; i++) {
                var bb = corners[i].toABB();
                renderer.drawABB(pose, bb, marked == i);
            }
        }
    },
    EDGE {
        @Override
        public BoxCorner[] corners(int marked) {
            if (marked >= 0 && marked < BoxEdge.values().length)
                return BoxEdge.values()[marked].corners;
            return null;
        }
        
        public Vec3 getCorner(BoxCorner corner, LittleBoxAbsolute[] corners) {
            var c = corners[corner.ordinal()];
            return c.getCorner(corner).getVec3();
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public int select(PreviewRenderer renderer, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            int index = -1;
            double distance = Double.MAX_VALUE;
            var player = renderer.player();
            float partialTickTime = renderer.partialTickTime();
            Vec3 pos = player.getEyePosition(partialTickTime);
            double reach = PlayerUtils.getReach(player);
            Vec3 view = player.getViewVector(partialTickTime);
            Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
            
            pos = box.context.toFakeWorld(pos);
            look = box.context.toFakeWorld(look);
            
            double radius = 0.2;
            
            for (int i = 0; i < BoxEdge.values().length; i++) {
                BoxEdge edge = BoxEdge.values()[i];
                double tempDistance = BoxEdge.distanceRayToSegment(pos, look, getCorner(edge.first(), corners), getCorner(edge.second(), corners));
                if (tempDistance <= radius && tempDistance < distance) {
                    distance = tempDistance;
                    index = i;
                }
            }
            return index;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean move(PreviewRenderer renderer, int marked, ShapePosition position, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            return marked >= 0 && marked < BoxEdge.values().length;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(PreviewRenderer renderer, PoseStack pose, BlockPos pos, int marked, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            pose.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            if (marked >= 0 && marked < BoxEdge.values().length) {
                var edge = BoxEdge.values()[marked];
                renderer.drawLine(pose, getCorner(edge.first(), corners), getCorner(edge.second(), corners), true);
            }
        }
    },
    FACE {
        @Override
        public BoxCorner[] corners(int marked) {
            if (marked >= 0 && marked < Facing.VALUES.length)
                return BoxFace.get(Facing.VALUES[marked]).corners;
            return null;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public int select(PreviewRenderer renderer, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            var player = renderer.player();
            float partialTickTime = renderer.partialTickTime();
            Vec3 pos = player.getEyePosition(partialTickTime);
            double reach = PlayerUtils.getReach(player);
            Vec3 view = player.getViewVector(partialTickTime);
            Vec3 look = pos.add(view.x * reach, view.y * reach, view.z * reach);
            
            pos = box.context.toFakeWorld(pos);
            look = box.context.toFakeWorld(look);
            
            var result = box.rayTrace(pos, look);
            if (result != null)
                return Facing.get(result.getDirection()).ordinal();
            return -1;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public boolean move(PreviewRenderer renderer, int marked, ShapePosition position, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            return marked >= 0 && marked < Facing.VALUES.length;
        }
        
        @Override
        @OnlyIn(Dist.CLIENT)
        public void render(PreviewRenderer renderer, PoseStack pose, BlockPos pos, int marked, LittleBoxLevel box, LittleBoxAbsolute[] corners) {
            var builder = renderer.createTesselatorBuilder(true);
            if (marked >= 0 && marked < Facing.VALUES.length) {
                var bb = box.getRenderingBoxWithoutOffset();
                bb.buildLinesFace(pose, builder, 255, Facing.VALUES[marked], bb.getCenter(), 0.001, true);
                renderer.drawLines(builder.build(), true);
            }
        }
    };
    
    public static final Codec<TransformMode> CODEC = StringRepresentable.fromEnum(TransformMode::values);
    public static final IntFunction<TransformMode> BY_ID = ByIdMap.continuous(TransformMode::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, TransformMode> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, TransformMode::ordinal);
    public static final TransformMode DEFAULT = CORNER;
    
    public abstract BoxCorner[] corners(int marked);
    
    @OnlyIn(Dist.CLIENT)
    public abstract int select(PreviewRenderer renderer, LittleBoxLevel box, LittleBoxAbsolute[] corners);
    
    @OnlyIn(Dist.CLIENT)
    public abstract boolean move(PreviewRenderer renderer, int marked, ShapePosition position, LittleBoxLevel box, LittleBoxAbsolute[] corners);
    
    @OnlyIn(Dist.CLIENT)
    public abstract void render(PreviewRenderer renderer, PoseStack pose, BlockPos pos, int marked, LittleBoxLevel box, LittleBoxAbsolute[] corners);
    
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
    
    public Component translatable() {
        return Component.translatable("building.transform." + name().toLowerCase());
    }
}
