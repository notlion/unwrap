package unwrap;

import processing.core.PConstants;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;


public class Edge
{
    public Tri tri;

    public Vec3D v1, v2, vOpposite;
    public Vec2D p1, p2, pOpposite;
    public Vec2D slot1, slot2;

    public Vec3D center;

    public Vec2D normal2;
    public Vec2D center2;
    public EdgePair pair;

    public float magnitude2;


    public Edge(Tri _tri, Vec3D _v1, Vec3D _v2, Vec3D _vOpposite, Vec2D _p1, Vec2D _p2, Vec2D _pOpposite)
    {
        tri = _tri;

        v1 = _v1;
        v2 = _v2;
        vOpposite = _vOpposite;

        p1 = _p1;
        p2 = _p2;
        pOpposite = _pOpposite;

        center = v1.interpolateTo(v2, 0.5f);

        normal2 = p2.sub(p1).rotate(PConstants.HALF_PI).normalize();
        center2 = p1.interpolateTo(p2, 0.5f);

        magnitude2 = p1.distanceTo(p2);
    }


    public void setPair(EdgePair _pair)
    {
        pair = _pair;
        calculateSlotDepths(0.35f, 0.65f);
    }

    public void clampSlotDepths(float min1, float max1, float min2, float max2)
    {
        slot1.y = Util.clamp(slot1.y, min1, max1);
        slot2.y = Util.clamp(slot2.y, min2, max2);
    }

    private void calculateSlotDepths(float u1, float u2)
    {
        slot1 = new Vec2D(u1, getSlotDepth(u1));
        slot2 = new Vec2D(u2, getSlotDepth(u2));
    }

    private float getSlotDepth(float u)
    {
        Vec2D is, s = p1.interpolateTo(p2, u);

        float depth = Float.MAX_VALUE;

        is = Util.lineIntersection(
            s.x, s.y, s.x - normal2.x, s.y - normal2.y,
            p1.x, p1.y, pOpposite.x, pOpposite.y
        );
        if(is != null)
            depth = Math.min(depth, s.distanceTo(is));

        is = Util.lineIntersection(
            s.x, s.y, s.x - normal2.x, s.y - normal2.y,
            p2.x, p2.y, pOpposite.x, pOpposite.y
        );
        if(is != null)
            depth = Math.min(depth, s.distanceTo(is));

        return Math.min(depth * 0.25f, magnitude2 * 0.1f);
    }


    public boolean equals(Edge edge)
    {
        return
            (p1.x == edge.p1.x && p1.y == edge.p1.y && p2.x == edge.p2.x && p2.y == edge.p2.y) ||
            (p1.x == edge.p2.x && p1.y == edge.p2.y && p2.x == edge.p1.x && p2.y == edge.p1.y)
        ;
    }

    public boolean hasSameVertices(Edge edge)
    {
        return (v1 == edge.v1 && v2 == edge.v2) || (v1 == edge.v2 && v2 == edge.v1);
    }
}