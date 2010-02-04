package unwrap;

import toxi.geom.Vec2D;


public class Util
{
    public static int wrap(int v, int min, int max)
    {
        if(v < min) return max - (min-v) % (max-min);
        else if(v >= max) return (v-min) % (max-min) + min;
        else return v;
    }
    public static float wrap(float v, float min, float max)
    {
        if(v < min) return max - (min-v) % (max-min);
        else if(v >= max) return (v-min) % (max-min)+min;
        else return v;
    }


    public static int clamp(int v, int min, int max)
    {
        return (v < min) ? min : ((v > max) ? max : v);
    }
    public static float clamp(float v, float min, float max)
    {
        return (v < min) ? min : ((v > max) ? max : v);
    }
    public static double clamp(double v, double min, double max)
    {
        return (v < min) ? min : ((v > max) ? max : v);
    }


    public static float min(float val[])
    {
        float min = val[0];
        for(int i = val.length; --i > 0;)
            if(val[i] < min) min = val[i];
        return min;
    }
    public static float max(float val[])
    {
        float max = val[0];
        for(int i = val.length; --i > 0;)
            if(val[i] > max) max = val[i];
        return max;
    }


    public static int sign(int n)
    {
        return n < 0 ? -1 : (n == 0 ? 0 : 1);
    }
    public static float sign(float n)
    {
        return n < 0.0f ? -1.0f : (n == 0.0f ? 0.0f : 1.0f);
    }
    public static double sign(double n)
    {
        return n < 0.0 ? -1.0 : (n == 0.0 ? 0.0 : 1.0);
    }


    public static float map(float v, float in_min, float in_max, float out_min, float out_max)
    {
        return out_min + (out_max-out_min) * ((v-in_min) / (in_max-in_min));
    }
    public static double map(double v, double in_min, double in_max, double out_min, double out_max)
    {
        return out_min + (out_max-out_min) * ((v-in_min) / (in_max-in_min));
    }


    // Linear Interpolation
    //
    public static int lerp(int a, int b, float u)
    {
        return (int)(a + (b - a) * u);
    }
    public static float lerp(float a, float b, float u)
    {
        return a + (b - a) * u;
    }
    public static double lerp(double a, double b, double u)
    {
        return a + (b - a) * u;
    }


    // Distance
    //
    public static float dist(float x1, float y1, float x2, float y2)
    {
        x2 -= x1;
        y2 -= y1;
        return (float)Math.sqrt(x2*x2 + y2*y2);
    }
    public static float distSq(float x1, float y1, float x2, float y2)
    {
        x2 -= x1;
        y2 -= y1;
        return x2*x2 + y2*y2;
    }


    // Vector
    //
    public static Vec2D mean(Vec2D[] vectors)
    {
        Vec2D avg = new Vec2D();
        for(int i = vectors.length; --i >= 0;)
            avg.add(vectors[i]);
        float m = 1.0f / vectors.length;
        return avg.scaleSelf(m, m);
    }


    // Intersection
    //
    public static Vec2D lineIntersection(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
    {
        float bx = x2 - x1;
        float by = y2 - y1;
        float dx = x4 - x3;
        float dy = y4 - y3;

        float b_dot_d_perp = bx * dy - by * dx;

        if(b_dot_d_perp == 0)
            return null;

        float cx = x3 - x1;
        float cy = y3 - y1;

        float t = (cx * dy - cy * dx) / b_dot_d_perp;

        return new Vec2D(x1 + t * bx, y1 + t * by);
    }
}
