package team.creative.littletiles.common.structure.animation.curve;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import team.creative.creativecore.common.util.math.vec.Vec1d;
import team.creative.creativecore.common.util.math.vec.Vec3d;

public enum ValueInterpolation {
    
    LINEAR {
        @Override
        public ValueCurveInterpolation<Vec1d> create1d() {
            return new ValueCurveInterpolation.LinearCurve<>();
        }
        
        @Override
        public ValueCurveInterpolation<Vec3d> create3d() {
            return new ValueCurveInterpolation.LinearCurve<>();
        }
    },
    COSINE {
        @Override
        public ValueCurveInterpolation<Vec1d> create1d() {
            return new ValueCurveInterpolation.CosineCurve<>();
        }
        
        @Override
        public ValueCurveInterpolation<Vec3d> create3d() {
            return new ValueCurveInterpolation.CosineCurve<>();
        }
    },
    CUBIC {
        @Override
        public ValueCurveInterpolation<Vec1d> create1d() {
            return new ValueCurveInterpolation.CubicCurve<>();
        }
        
        @Override
        public ValueCurveInterpolation<Vec3d> create3d() {
            return new ValueCurveInterpolation.CubicCurve<>();
        }
    },
    HERMITE {
        @Override
        public ValueCurveInterpolation<Vec1d> create1d() {
            return new ValueCurveInterpolation.HermiteCurve<>();
        }
        
        @Override
        public ValueCurveInterpolation<Vec3d> create3d() {
            return new ValueCurveInterpolation.HermiteCurve<>();
        }
    };
    
    public abstract ValueCurveInterpolation<Vec1d> create1d();
    
    public abstract ValueCurveInterpolation<Vec3d> create3d();
    
    public MutableComponent translate() {
        return Component.translatable("gui.interpolation." + name().toLowerCase());
    }
}
