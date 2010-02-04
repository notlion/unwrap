package unwrap.shape;

import processing.core.PGraphics;
import toxi.geom.Vec2D;


public abstract class PolyShape
{
    public Vec2D[] shape;
    public Vec2D[] hull;

    public abstract void draw2d(PGraphics g);
    public abstract void draw2dPdf(PGraphics g);
    public abstract void draw3d(PGraphics g, PGraphics pdf);

    public void draw3d(PGraphics g)
    {
        draw3d(g, null);
    }

    public float getHullArea()
    {
        if(hull.length < 3)
            return 0;

        float area = 0;
        for(int i = 0, j = hull.length - 1; i < hull.length; i++) {
            area += (hull[j].x * hull[i].y) - (hull[j].y * hull[i].x);
            j = i;
        }

        return Math.abs(area) * 0.5f;
    }
}