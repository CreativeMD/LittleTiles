package team.creative.littletiles.common.placement.shape.type;

import java.util.ArrayList;
import java.util.List;

import team.creative.creativecore.common.util.math.vec.Vec3d;
import team.creative.littletiles.client.tool.shaper.ShapePosition;
import team.creative.littletiles.client.tool.shaper.ShapeSelection;
import team.creative.littletiles.common.math.box.LittleBox;
import team.creative.littletiles.common.math.box.collection.LittleBoxes;
import team.creative.littletiles.common.math.vec.LittleVec;
import team.creative.littletiles.common.placement.shape.LittleShape;
import team.creative.littletiles.common.placement.shape.config.CatenaryConfig;
import team.creative.littletiles.common.placement.shape.config.CatenaryConfig.Mode;

/**
 * Generates a catenary curve between two points with configurable drop and mode.
 * <p>
 * drop: downward distance from the lower endpoint to the lowest point (grid units).
 * mode: BETWEEN &rarr; the lowest point lies between endpoints; BEYOND &rarr; the lowest point lies outside.
 */
public class LittleShapeCatenary extends LittleShape<CatenaryConfig> {

    private static final double EPS = 1e-8;
    private static final double MIN_A = 0.01;
    private static final double MIN_HORIZONTAL_DIST = 0.5;
    private static final int MAX_ITER = 300;
    private static final int LINE_SEARCH_ITER = 20;
    private static final int MAX_VOXELS = 1_000_000;
    private static final int MIN_STEPS = 6;
    private static final double MAX_POINT_SPACING = 0.35;
    private static final double SAFE_SINH_THRESHOLD = 500.0;

    public LittleShapeCatenary() {
        super(2);
    }

    @Override
    protected void build(LittleBoxes boxes, ShapeSelection selection, CatenaryConfig config) {
        if (selection.size() < 2) return;

        ShapePosition pos1 = selection.get(0);
        ShapePosition pos2 = selection.get(1);

        LittleVec rel1 = pos1.getRelative(selection.pos);
        LittleVec rel2 = pos2.getRelative(selection.pos);

        double x1 = rel1.x, y1 = rel1.y, z1 = rel1.z;
        double x2 = rel2.x, y2 = rel2.y, z2 = rel2.z;

        Vec3d p1 = new Vec3d(x1, y1, z1);
        Vec3d p2 = new Vec3d(x2, y2, z2);

        Vec3d delta = p2.copy();
        delta.sub(p1);
        double dx = delta.x, dz = delta.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < MIN_HORIZONTAL_DIST) {
            addColumn(boxes, p1, p2, config.thickness);
            return;
        }

        double ux = dx / horizontalDist;
        double uz = dz / horizontalDist;

        double drop = config.drop;
        double yLow = Math.min(y1, y2);
        double rise1 = y1 - yLow;
        double rise2 = y2 - yLow;

        double[] params;
        if (drop < EPS)
            params = solveDropZero(horizontalDist, rise1, rise2);
        else
            params = solveParams(horizontalDist, rise1, rise2, drop, config.mode);

        if (params == null) {
            drawLine(boxes, p1, p2, horizontalDist, ux, uz, config.thickness);
            return;
        }

        double a = params[0];
        double x0 = params[1];
        double b = -a * safeCosh(x0 / a);

        List<Vec3d> points = generateUniformArcPoints(horizontalDist, a, x0, b, p1, ux, uz);
        if (points == null || points.size() > MAX_VOXELS) {
            drawLine(boxes, p1, p2, horizontalDist, ux, uz, config.thickness);
            return;
        }

        for (Vec3d world : points)
            addBox(boxes, world, config.thickness);
    }

    /* Safe sinh that avoids overflow for large |x|. */
    private static double safeSinh(double x) {
        if (Math.abs(x) > SAFE_SINH_THRESHOLD)
            return Math.signum(x) * 0.5 * Math.exp(Math.abs(x));
        return Math.sinh(x);
    }

    /* Safe cosh that avoids overflow for large |x|. */
    private static double safeCosh(double x) {
        if (Math.abs(x) > SAFE_SINH_THRESHOLD)
            return 0.5 * Math.exp(Math.abs(x));
        return Math.cosh(x);
    }

    /* Java's Math has no asinh; use the identity ln(x + sqrt(x^2 + 1)). */
    private static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    /*
     * Generates points uniformly along the catenary arc length.
     * Uses the analytical arc length L = a * (sinh(t1) - sinh(t0)) and inverts it via asinh.
     * Returns null if any intermediate value overflows, so the caller can fall back to a straight line.
     */
    private List<Vec3d> generateUniformArcPoints(double d, double a, double x0, double b, Vec3d p1, double ux, double uz) {
        double t0 = -x0 / a;
        double t1 = (d - x0) / a;
        double sinh0 = safeSinh(t0);
        double sinh1 = safeSinh(t1);

        if (!Double.isFinite(sinh0) || !Double.isFinite(sinh1))
            return null;

        double totalArcLen = a * (sinh1 - sinh0);
        if (!Double.isFinite(totalArcLen) || totalArcLen > MAX_VOXELS * MAX_POINT_SPACING)
            return null;

        if (totalArcLen < EPS) {
            int numPoints = Math.max(MIN_STEPS, (int) Math.ceil(d / MAX_POINT_SPACING));
            List<Vec3d> points = new ArrayList<>(numPoints + 1);
            for (int i = 0; i <= numPoints; i++) {
                double x = d * i / numPoints;
                double y = a * safeCosh((x - x0) / a) + b;
                if (!Double.isFinite(y)) return null;
                points.add(new Vec3d(p1.x + x * ux, p1.y + y, p1.z + x * uz));
            }
            return points;
        }

        int numPoints = (int) Math.ceil(totalArcLen / MAX_POINT_SPACING);
        numPoints = Math.max(MIN_STEPS, numPoints);
        if (numPoints > MAX_VOXELS)
            return null;

        List<Vec3d> points = new ArrayList<>(numPoints + 1);
        for (int i = 0; i <= numPoints; i++) {
            double s = totalArcLen * i / numPoints;
            double asinhArg = asinh(s / a + sinh0);
            if (!Double.isFinite(asinhArg))
                return null;

            double x = x0 + a * asinhArg;
            if (x < 0) x = 0;
            if (x > d) x = d;

            double y = a * safeCosh((x - x0) / a) + b;
            if (!Double.isFinite(y))
                return null;

            points.add(new Vec3d(p1.x + x * ux, p1.y + y, p1.z + x * uz));
        }

        // Snap endpoints exactly to avoid rounding drift.
        points.set(0, new Vec3d(p1.x, p1.y, p1.z));
        points.set(points.size() - 1, new Vec3d(p1.x + d * ux, p1.y + a * safeCosh((d - x0) / a) + b, p1.z + d * uz));

        return points;
    }

    /*
     * Special case drop = 0: the minimum is at the lower endpoint.
     * Solves a * (cosh(d/a) - 1) = |r1 - r2| by binary search (the LHS decreases as a increases).
     */
    private double[] solveDropZero(double d, double r1, double r2) {
        double heightDiff = Math.abs(r1 - r2);
        if (heightDiff < EPS)
            return new double[]{1e6, 0};

        double aLow = MIN_A, aHigh = 1e6;
        for (int i = 0; i < 100; i++) {
            double aMid = (aLow + aHigh) * 0.5;
            double val = aMid * (safeCosh(d / aMid) - 1);
            if (val > heightDiff)
                aLow = aMid;
            else
                aHigh = aMid;
            if (aHigh - aLow < 1e-8) break;
        }

        double a = (aLow + aHigh) * 0.5;
        if (Double.isNaN(a) || a < MIN_A)
            return null;

        double x0 = (r1 < r2) ? 0.0 : d;
        return new double[]{a, x0};
    }

    /*
     * Solves the two catenary equations
     *   a * (cosh(x0/a) - 1)       = r1 + drop
     *   a * (cosh((d-x0)/a) - 1)   = r2 + drop
     * where r1, r2 are endpoint heights relative to the lower endpoint.
     * mode controls x0: clamped to [0, d] for BETWEEN, unbounded for BEYOND.
     * Returns null if no solution is found (caller falls back to a straight line).
     */
    private double[] solveParams(double d, double r1, double r2, double drop, Mode mode) {
        double a = Math.max((d * d) / (8 * drop + 1e-12), MIN_A);
        double x0 = d * 0.5;

        if (mode == Mode.BEYOND) {
            boolean lowerLeft = r1 < EPS;
            boolean lowerRight = r2 < EPS;
            double maxOffset = Math.min(d * 2.5, Math.max(d * 0.5, 10.0));
            if (lowerLeft)
                x0 = -maxOffset;
            else if (lowerRight)
                x0 = d + maxOffset;
            else
                x0 = -d * 0.5;
        }

        // Newton iteration with simple line search.
        for (int iter = 0; iter < MAX_ITER; iter++) {
            double cosh1 = safeCosh(x0 / a);
            double cosh2 = safeCosh((d - x0) / a);
            double sinh1 = safeSinh(x0 / a);
            double sinh2 = safeSinh((d - x0) / a);

            double F1 = a * (cosh1 - 1) - (r1 + drop);
            double F2 = a * (cosh2 - 1) - (r2 + drop);

            double dF1da = (cosh1 - 1) - (x0 / a) * sinh1;
            double dF1dx0 = sinh1;
            double dF2da = (cosh2 - 1) - ((d - x0) / a) * sinh2;
            double dF2dx0 = -sinh2;

            double det = dF1da * dF2dx0 - dF1dx0 * dF2da;
            if (Math.abs(det) < EPS) break;

            double da = (F1 * dF2dx0 - dF1dx0 * F2) / det;
            double dx0 = (dF1da * F2 - F1 * dF2da) / det;

            double step = 1.0;
            double bestRes = Math.abs(F1) + Math.abs(F2);
            double bestA = a, bestX0 = x0;

            for (int i = 0; i < LINE_SEARCH_ITER; i++) {
                double aNew = Math.max(a - step * da, MIN_A);
                double x0New;
                if (mode == Mode.BETWEEN) {
                    x0New = Math.min(Math.max(x0 - step * dx0, MIN_A), d - MIN_A);
                } else {
                    x0New = x0 - step * dx0;
                    double limit = d * 2.5;
                    if (x0New < -limit) x0New = -limit;
                    if (x0New > d + limit) x0New = d + limit;
                }
                double F1n = aNew * (safeCosh(x0New / aNew) - 1) - (r1 + drop);
                double F2n = aNew * (safeCosh((d - x0New) / aNew) - 1) - (r2 + drop);
                double res = Math.abs(F1n) + Math.abs(F2n);
                if (res < bestRes) {
                    bestA = aNew;
                    bestX0 = x0New;
                    break;
                }
                step *= 0.5;
                if (step < 1e-12) break;
            }
            a = bestA;
            x0 = bestX0;

            if (Math.abs(da) < EPS && Math.abs(dx0) < EPS) break;
        }

        if (Double.isNaN(a) || a < MIN_A) return null;
        if (mode == Mode.BETWEEN && (x0 < 0 || x0 > d)) return null;
        return new double[]{a, x0};
    }

    /*
     * Straight-line fallback used when the solver fails or the curve would be too dense.
     * Steps are derived from the 3D distance so steep drops still produce a dense chain.
     */
    private void drawLine(LittleBoxes boxes, Vec3d p1, Vec3d p2, double d, double ux, double uz, int thickness) {
        double dy = p2.y - p1.y;
        double totalDist = Math.sqrt(d * d + dy * dy);
        int steps = Math.max(MIN_STEPS, (int) Math.ceil(totalDist / MAX_POINT_SPACING));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double x = t * d;
            double y = p1.y + t * dy;
            addBox(boxes, new Vec3d(p1.x + x * ux, y, p1.z + x * uz), thickness);
        }
    }

    /* Adds a single vertical column when endpoints are almost aligned horizontally. */
    private void addColumn(LittleBoxes boxes, Vec3d p1, Vec3d p2, int thickness) {
        int minX = (int) Math.floor(Math.min(p1.x, p2.x));
        int minY = (int) Math.floor(Math.min(p1.y, p2.y));
        int minZ = (int) Math.floor(Math.min(p1.z, p2.z));
        int maxX = (int) Math.ceil(Math.max(p1.x, p2.x));
        int maxY = (int) Math.ceil(Math.max(p1.y, p2.y));
        int maxZ = (int) Math.ceil(Math.max(p1.z, p2.z));
        LittleBox box = new LittleBox(minX, minY, minZ, maxX, maxY, maxZ);
        if (thickness > 1) box.growCentered(thickness - 1);
        boxes.add(box);
    }

    /* Adds a single voxel box centered at the given position, applying thickness. */
    private void addBox(LittleBoxes boxes, Vec3d pos, int thickness) {
        int cx = (int) Math.round(pos.x);
        int cy = (int) Math.round(pos.y);
        int cz = (int) Math.round(pos.z);
        LittleBox box = new LittleBox(cx, cy, cz, cx + 1, cy + 1, cz + 1);
        if (thickness > 1) box.growCentered(thickness - 1);
        boxes.add(box);
    }

    @Override
    protected boolean requiresNoOverlap(ShapeSelection selection, CatenaryConfig config) {
        return true;
    }
}