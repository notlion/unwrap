package unwrap.shape;

import java.util.ArrayList;

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import unwrap.Edge;
import unwrap.Tri;


public class TriPolyShape extends PolyShape
{
    public Tri tri;


    public TriPolyShape(Tri _tri, float _hullPadding, float _materialThickness)
    {
        tri = _tri;

        ArrayList<Vec2D> shp = new ArrayList<Vec2D>(3 * 9);
        Edge e;
        float mt2 = _materialThickness / 2;
        for(int i = 0; i < tri.edges.length; i++) {
            e = tri.edges[i];
            shp.add(e.p1);
            if(e.pair != null) {
                Vec2D s1 = e.p1.interpolateTo(e.p2, e.slot1.x);
                Vec2D s2 = e.p1.interpolateTo(e.p2, e.slot2.x);
                Vec2D rn = e.normal2.getRotated(PConstants.HALF_PI).scaleSelf(mt2);
                Vec2D n1 = e.normal2.scale(e.slot1.y);
                Vec2D n2 = e.normal2.scale(e.slot2.y);

                shp.add(new Vec2D(s1.x + rn.x, s1.y + rn.y));
                shp.add(new Vec2D(s1.x - n1.x + rn.x, s1.y - n1.y + rn.y));
                shp.add(new Vec2D(s1.x - n1.x - rn.x, s1.y - n1.y - rn.y));
                shp.add(new Vec2D(s1.x - rn.x, s1.y - rn.y));

                shp.add(new Vec2D(s2.x + rn.x, s2.y + rn.y));
                shp.add(new Vec2D(s2.x - n2.x + rn.x, s2.y - n2.y + rn.y));
                shp.add(new Vec2D(s2.x - n2.x - rn.x, s2.y - n2.y - rn.y));
                shp.add(new Vec2D(s2.x - rn.x, s2.y - rn.y));
            }
        }
        shape = new Vec2D[shp.size()];
        shp.toArray(shape);

        hull = tri.getHull2d(_hullPadding);
    }


    public void draw2d(PGraphics g)
    {
        g.strokeWeight(1);

        // Hull
        g.fill(255, 12);
        g.stroke(255, 24);
        g.beginShape();
        for(int i = 0; i < hull.length; i++)
            g.vertex(hull[i].x, hull[i].y);
        g.endShape(PGraphics.CLOSE);

        // Shape
        g.fill(255, 16);
        g.stroke(255, 48);
        g.beginShape();
        for(int i = 0; i < shape.length; i++)
            g.vertex(shape[i].x, shape[i].y);
        g.endShape(PGraphics.CLOSE);

        // Numbers
        g.textAlign(PGraphics.CENTER);
        g.fill(255, 64);
        drawNumbers(g);
    }

    public void draw2dPdf(PGraphics g)
    {
        g.strokeJoin(PGraphics.ROUND);
        g.strokeWeight(0.001f);

        // Shape
        g.noFill();
        g.stroke(0);
        g.beginShape();
        for(int i = 0; i < shape.length; i++)
            g.vertex(shape[i].x, shape[i].y);
        g.endShape(PGraphics.CLOSE);

        // Numbers
        g.textAlign(PGraphics.CENTER);
        g.noStroke();
        g.fill(255, 0, 0);
        drawNumbers(g);
    }

    private void drawNumbers(PGraphics g)
    {
        for(int i = tri.edges.length; --i >= 0;) {
            Edge e = tri.edges[i];
            if(e.pair != null) {
                g.pushMatrix();
                g.translate(e.center2.x - e.normal2.x * 6, e.center2.y - e.normal2.y * 6);
                g.rotate((float)Math.atan2(e.normal2.y, e.normal2.x) - PConstants.HALF_PI);

                g.textSize(e.magnitude2 * 0.05f);
                g.text(e.pair.id, 0,0);

                g.popMatrix();
            }
        }
    }


    public void draw3d(PGraphics g, PGraphics pdf)
    {
        g.stroke(100);
        g.fill(32);

        g.pushMatrix();
        g.translate(tri.center.x, tri.center.y, tri.center.z);
        g.beginShape();
        if(pdf != null)
            pdf.beginShape();
        Vec3D pnt;
        for(int i = 0; i < shape.length; i++) {
            pnt = tri.getPoint3d(shape[i]);
            g.vertex(pnt.x, pnt.y, pnt.z);
            if(pdf != null)
                pdf.vertex(g.screenX(pnt.x, pnt.y, pnt.z), g.screenY(pnt.x, pnt.y, pnt.z));
        }
        g.endShape(PGraphics.CLOSE);
        if(pdf != null)
            pdf.endShape(PGraphics.CLOSE);
        g.popMatrix();
    }
}