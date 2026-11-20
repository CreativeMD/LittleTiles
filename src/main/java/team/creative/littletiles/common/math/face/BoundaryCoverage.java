package team.creative.littletiles.common.math.face;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Conservative proof that axis-aligned rectangles cover the entire unit face. */
public final class BoundaryCoverage {
    public record Rect(double minU, double minV, double maxU, double maxV) {}
    private BoundaryCoverage() {}

    public static boolean full(List<Rect> rectangles) {
        if (rectangles.isEmpty()) return false;
        for (Rect r : rectangles) {
            if (r.minU <= 0 && r.minV <= 0 && r.maxU >= 1 && r.maxV >= 1) return true;
        }
        double[] u = new double[2 * rectangles.size() + 2], v = new double[u.length];
        u[1] = v[1] = 1;
        int nu = 2, nv = 2;
        for (Rect r : rectangles) {
            if (r.minU > 0 && r.minU < 1) u[nu++] = r.minU;
            if (r.maxU > 0 && r.maxU < 1) u[nu++] = r.maxU;
            if (r.minV > 0 && r.minV < 1) v[nv++] = r.minV;
            if (r.maxV > 0 && r.maxV < 1) v[nv++] = r.maxV;
        }
        nu = unique(u, nu); nv = unique(v, nv);
        boolean sweepU = nu <= nv;
        double[] edges = sweepU ? u : v;
        int count = sweepU ? nu : nv;
        Rect[] sorted = rectangles.toArray(Rect[]::new);
        // One order works for every strip; choose the axis with fewer strip boundaries.
        Arrays.sort(sorted, sweepU ? BY_V : BY_U);
        for (int i = 1; i < count; i++) {
            double left = edges[i - 1], right = edges[i];
            double covered = 0;
            for (Rect r : sorted) {
                double start = sweepU ? r.minV : r.minU, end = sweepU ? r.maxV : r.maxU;
                if ((sweepU ? r.minU > left || r.maxU < right : r.minV > left || r.maxV < right) || end <= start) continue;
                if (start > covered) break;
                covered = Math.max(covered, end);
                if (covered >= 1) break;
            }
            if (covered < 1) return false;
        }
        return true;
    }
    private static final Comparator<Rect> BY_U = Comparator.comparingDouble(Rect::minU);
    private static final Comparator<Rect> BY_V = Comparator.comparingDouble(Rect::minV);

    private static int unique(double[] edges, int count) {
        Arrays.sort(edges, 0, count);
        int size = 1;
        for (int i = 1; i < count; i++)
            if (edges[i] != edges[size - 1]) edges[size++] = edges[i];
        return size;
    }
}
