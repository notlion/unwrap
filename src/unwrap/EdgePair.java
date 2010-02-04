package unwrap;

import processing.core.PConstants;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;


public class EdgePair
{
    public Edge e1, e2;

    public Vec3D normal, normal_u, normal_v;

    public float angle;
    public int id;


    public EdgePair(Edge _e1, Edge _e2, int _id, float _materialThickness)
    {
        e1 = _e1;
        e2 = _e2;

        id = _id;

        e1.setPair(this);
        e2.setPair(this);

        float mt2 = _materialThickness * 2;

        e1.clampSlotDepths(mt2, e2.slot2.y, mt2, e2.slot1.y);
        e2.clampSlotDepths(mt2, e1.slot2.y, mt2, e1.slot1.y);

        angle = (float)(Math.PI - Math.acos(e1.tri.normal.dot(e2.tri.normal)));

        float dot = e1.tri.normal.dot(e2.vOpposite.sub(e1.vOpposite).normalize());

        normal = e1.tri.normal.add(e2.tri.normal).normalize();

        Vec3D unit = e1.v2.sub(e1.v1).normalize();
        normal_v = e1.tri.normal.add(e2.tri.normal).scaleSelf(dot > 0 ? 1.0f : -1.0f).normalize();
        normal_u = normal_v.getRotatedAroundAxis(unit, PConstants.HALF_PI);
    }


    public Vec3D getPoint3d(Vec2D pnt)
    {
        return normal_u.scale(pnt.x).addSelf(normal_v.copy().scaleSelf(pnt.y));
    }
}