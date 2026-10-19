package pro.komaru.tridot.util.math;

import pro.komaru.tridot.util.phys.*;
import pro.komaru.tridot.util.struct.data.*;

public class Mathf {
    public static final int[] signs = {-1, 1};
    public static final int[] zeroOne = {0, 1};
    public static final boolean[] booleans = {true, false};
    public static final float PI = MthConst.pi, pi = PI, halfPi = PI/2;
    public static final float PI2 = MthConst.tau;
    public static final float E = MthConst.e;
    public static final float radiansToDegrees = 1f / MthConst.rad;
    public static final float radDeg = radiansToDegrees;
    /** multiply by this to convert from degrees to radians */
    public static final float degreesToRadians = MthConst.rad;
    public static final float degRad = degreesToRadians;
    private static final int sinBits = 14; // 16KB. Adjust for accuracy.
    private static final int sinMask = ~(-1 << sinBits);
    private static final int sinCount = sinMask + 1;
    private static final float[] sinTable = new float[sinCount];
    private static final float radFull = PI * 2;
    private static final float degFull = 360;
    private static final float radToIndex = sinCount / radFull;
    private static final float degToIndex = sinCount / degFull;
    static{
        for(int i = 0; i < sinCount; i++)
            sinTable[i] = (float)Math.sin((i + 0.5f) / sinCount * radFull);
        for(int i = 0; i < 360; i += 90)
            sinTable[(int)(i * degToIndex) & sinMask] = (float)Math.sin(i * degreesToRadians);

        sinTable[0] = 0f;
        sinTable[(int)(90 * degToIndex) & sinMask] = 1f;
        sinTable[(int)(180 * degToIndex) & sinMask] = 0f;
        sinTable[(int)(270 * degToIndex) & sinMask] = -1f;
    }
    /** Distance between two 3D points*/
    public static double dst(double x, double y, double z, double x2, double y2, double z2) {
        double dx = x-x2;
        double dy = y-y2;
        double dz = z-z2;
        return Math.sqrt(dx*dx+dy*dy+dz*dz);
    }

    public static float distanceSq(Vec3f vec1, Vec3f vec2) {
        float dx = vec1.x - vec2.x;
        float dy = vec1.y - vec2.y;
        float dz = vec1.z - vec2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static float distance(Vec3f vec1, Vec3f vec2) {
        return (float)Math.sqrt(distanceSq(vec1, vec2));
    }

    public static double distanceSq(net.minecraft.world.phys.Vec3 vec1, net.minecraft.world.phys.Vec3 vec2) {
        double dx = vec1.x - vec2.x;
        double dy = vec1.y - vec2.y;
        double dz = vec1.z - vec2.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static double distance(net.minecraft.world.phys.Vec3 vec1, net.minecraft.world.phys.Vec3 vec2) {
        return Math.sqrt(distanceSq(vec1, vec2));
    }

    /**Lerps from one point to another with specified progress*/
    public static float lerp(float from, float to, float dst) {
        return (to-from)*clamp(dst)+from;
    }
    /** Lerps from one point to another with specified progress without clamping*/
    public static float lerpf(float from, float to, float dst) {
        return (to-from)*dst+from;
    }

    /** Clamps to [min,max]. */
    public static int clamp(int value, int min, int max){
        return Math.max(Math.min(value, max), min);
    }
    /** Clamps to [min,max]. */
    public static long clamp(long value, long min, long max){
        return Math.max(Math.min(value, max), min);
    }
    /** Clamps to [min,max]. */
    public static float clamp(float value, float min, float max){
        return Math.max(Math.min(value, max), min);
    }
    /** Clamps to [min,max]. */
    public static double clamp(double value, double min, double max){
        return Math.max(Math.min(value, max), min);
    }
    /** Clamps to [0, 1]. */
    public static float clamp(float value){
        return clamp(value, 0f, 1f);
    }
    /** Clamps floating point to specified points */
    public static float toFixed(float val, int points) {
        int pow = powTen(points);
        return (float) Math.round(val * pow) / pow;
    }
    /** 10^pow */
    public static int powTen(int pow) {
        return (int) Math.pow(10,pow);
    }
    /** Returns the sine in radians from a lookup table. */
    public static float sin(float radians){
        return sinTable[(int)(radians * radToIndex) & sinMask];
    }

    /** Returns the cosine in radians from a lookup table. */
    public static float cos(float radians){
        return sinTable[(int)((radians + PI / 2) * radToIndex) & sinMask];
    }
    /** Returns: the input 0-1 value scaled to 0-1-0. */
    public static float slope(float fin){
        return 1f - Math.abs(fin - 0.5f) * 2f;
    }

    public static float catmullrom(Seq<Float> points, int cur, int next, float t) {
        int p0Index = Math.max(cur - 1, 0);
        int p1Index = Math.max(cur, 0);
        int p2Index = Math.min(next, points.size - 1);
        int p3Index = Math.min(next + 1, points.size - 1);

        float p0 = points.get(p0Index);
        float p1 = points.get(p1Index);
        float p2 = points.get(p2Index);
        float p3 = points.get(p3Index);

        float t2 = t * t;
        float t3 = t2 * t;

        return 0.5f * (
                (-t3 + 2f * t2 - t) * p0 +
                        (3f * t3 - 5f * t2 + 2f) * p1 +
                        (-3f * t3 + 4f * t2 + t) * p2 +
                        (t3 - t2) * p3
        );
    }
    public static float catmullrom(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;

        return 0.5f * (
                (-t3 + 2 * t2 - t) * p0 +
                        (3 * t3 - 5 * t2 + 2) * p1 +
                        (-3 * t3 + 4 * t2 + t) * p2 +
                        (t3 - t2) * p3
        );
    }
    public static float catmullromLength(float p0, float p1, float p2, float p3, int steps) {
        float length = 0f;
        float prevValue = catmullrom(p0, p1, p2, p3, 0);

        for (int i = 1; i <= steps; i++) {
            float t = (float) i / steps;
            float currentValue = catmullrom(p0, p1, p2, p3, t);
            length += Math.abs(currentValue - prevValue);
            prevValue = currentValue;
        }

        return length;
    }
    public static float catmullromLength(Seq<Float> points, int cur, int next, int steps) {
        int p0Index = Math.max(cur - 1, 0);
        int p1Index = Math.max(cur, 0);
        int p2Index = Math.min(next, points.size - 1);
        int p3Index = Math.min(next + 1, points.size - 1);

        float p0 = points.get(p0Index);
        float p1 = points.get(p1Index);
        float p2 = points.get(p2Index);
        float p3 = points.get(p3Index);

        return catmullromLength(p0,p1,p2,p3,steps);
    }
    public static float interpLength(Interp f, float a, float b, int steps) {
        float length = 0f;
        float stepSize = (b - a) / steps;

        for (int i = 0; i < steps; i++) {
            float x1 = a + i * stepSize;
            float x2 = x1 + stepSize;

            // Вычисляем производную приближённо
            float df_dx = (f.apply(x2) - f.apply(x1)) / stepSize;

            // Длина шага
            length += (float) (Math.sqrt(1 + df_dx * df_dx) * stepSize);
        }

        return length;
    }
}
