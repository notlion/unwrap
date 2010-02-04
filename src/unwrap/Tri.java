package unwrap;

import java.util.ArrayList;

import toxi.geom.Vec2D;
import toxi.geom.Vec3D;


public class Tri
{
    public Vec3D a, b, c;
    public Vec2D a2, b2, c2;

    public Edge ab, bc, ca;
    public Edge[] edges;

    public Vec3D normal, normal_u, normal_v;
    public Vec3D center;


    public Tri(Vec3D _a, Vec3D _b, Vec3D _c)
    {
        a = _a;
        b = _b;
        c = _c;

        Vec3D bsa = b.sub(a);
        Vec3D csa = c.sub(a);

        // Create the uv-space axes
        normal = bsa.cross(csa).normalize();
        normal_u = bsa.getNormalized();
        normal_v = normal_u.cross(normal).normalize();


        // Create 3d center
        center = a.add(b).addSelf(c).scaleSelf(1.0f / 3.0f);


        // Make the 2d points
        a2 = new Vec2D();
        b2 = new Vec2D(
            bsa.dot(normal_u),
            bsa.dot(normal_v)
        );
        c2 = new Vec2D(
            csa.dot(normal_u),
            csa.dot(normal_v)
        );


        // Make the 2d center
        Vec2D ctr = b2.add(c2).scaleSelf(1.0f / 3.0f);

        // Translate 2d points to center
        a2.subSelf(ctr);
        b2.subSelf(ctr);
        c2.subSelf(ctr);


        // Make the edges
        ab = new Edge(this, a, b, c, a2, b2, c2);
        bc = new Edge(this, b, c, a, b2, c2, a2);
        ca = new Edge(this, c, a, b, c2, a2, b2);

        edges = new Edge[]{ ab, bc, ca };
    }


    public Vec2D[] getHull2d(float padding)
    {
        Vec2D norm_ab = ab.normal2.scale(padding);
        Vec2D norm_bc = bc.normal2.scale(padding);
        Vec2D norm_ca = ca.normal2.scale(padding);

        return new Vec2D[]{
            a2.add(norm_ab),
            b2.add(norm_ab),
            b2.add(norm_bc),
            c2.add(norm_bc),
            c2.add(norm_ca),
            a2.add(norm_ca)
        };
    }

    public Vec3D getPoint3d(Vec2D pnt)
    {
        return normal_u.scale(pnt.x).addSelf(normal_v.scale(pnt.y));
    }


    public void addEdgesTo(ArrayList<Edge> edges)
    {
        edges.add(ab);
        edges.add(bc);
        edges.add(ca);
    }
}