package unwrap.shape;

import processing.core.PConstants;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import toxi.geom.Vec3D;
import unwrap.EdgePair;
import unwrap.Util;


public class ConnectorPolyShape extends PolyShape
{
    public EdgePair pair;

    private Vec2D center;
    private Vec2D slot1, slot2;


    public ConnectorPolyShape(EdgePair _pair, float _hullPadding, float _materialThickness, boolean _flip)
    {
        pair = _pair;

        if(_flip) {
            slot1 = pair.e1.slot2;
            slot2 = pair.e2.slot1;
        }
        else {
            slot1 = pair.e1.slot1;
            slot2 = pair.e2.slot2;
        }

        float mt2 = _materialThickness / 2.0f;
        float mpad = _materialThickness * 2.5f;
        float mcombi = mt2 + mpad;

        float s1 = slot1.y;
        float s2 = slot2.y;
        float oh1 = Math.max(_materialThickness * 2, s1 * 0.75f);
        float oh2 = Math.max(_materialThickness * 2, s2 * 0.75f);

        float a2 = pair.angle / 2.0f;

        Vec2D cp1 = new Vec2D(-mcombi, 0).rotate(a2);
        Vec2D cp2 = new Vec2D(-mcombi, -(s1 + oh1)).rotate(a2);
        Vec2D cp3 = new Vec2D(mcombi, 0).rotate(-a2);
        Vec2D cp4 = new Vec2D(mcombi, s2 + oh2).rotate(-a2);
        Vec2D corner = Util.lineIntersection(cp1.x, cp1.y, cp2.x, cp2.y, cp3.x, cp3.y, cp4.x, cp4.y);

        shape = new Vec2D[]{
            corner == null ? new Vec2D(-mcombi, 0) : corner,

            new Vec2D(-mcombi, s1 + oh1).rotate(a2),
            new Vec2D(-mt2, s1 + oh1).rotate(a2),
            new Vec2D(-mt2, s1).rotate(a2),
            new Vec2D(mt2, s1).rotate(a2),
            new Vec2D(mt2, s1 + oh1).rotate(a2),
            new Vec2D(mt2 + mpad, s1 + oh1).rotate(a2),

            new Vec2D(-mcombi, s2 + oh2).rotate(-a2),
            new Vec2D(-mt2, s2 + oh2).rotate(-a2),
            new Vec2D(-mt2, s2).rotate(-a2),
            new Vec2D(mt2, s2).rotate(-a2),
            new Vec2D(mt2, s2 + oh2).rotate(-a2),
            new Vec2D(mcombi, s2 + oh2).rotate(-a2)
        };

        if(pair.angle == (float)Math.PI) {
            center = Util.mean(new Vec2D[]{ shape[1], shape[6], shape[7], shape[12] });
            hull = getPaddedHull(shape, new int[]{ 1, 6, 7, 12 }, _hullPadding);
        }
        else {
            center = Util.mean(new Vec2D[]{ shape[0], shape[1], shape[6], shape[7], shape[12] });
            hull = getPaddedHull(shape, new int[]{ 0, 1, 6, 7, 12 }, _hullPadding);
        }
    }


    // You need to up org.jbox2d.common.Settings.maxPolygonVertices to 9 for this to work
    private Vec2D[] getPaddedHull(Vec2D[] pnts, int[] indices, float padding)
    {
        Vec2D[] h = new Vec2D[indices.length * 2];
        Vec2D nrm;

        for(int i = 0, n = indices.length, i1; i < n; i++) {
            i1 = (i+1) % n;
            nrm = pnts[indices[i1]].sub(pnts[indices[i]]).rotate(PConstants.HALF_PI).normalizeTo(padding);
            h[i * 2] = pnts[indices[i]].add(nrm);
            h[i * 2 + 1] = pnts[indices[i1]].add(nrm);
        }

        return h;
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

        // Number
        g.textAlign(PGraphics.CENTER);
        g.fill(255, 64);
        drawNumber(g);
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

        // Number
        g.textAlign(PGraphics.CENTER);
        g.noStroke();
        g.fill(255, 0, 0);
        drawNumber(g);
    }

    private void drawNumber(PGraphics g)
    {
        float ts = (slot1.y + slot2.y) * 0.25f;

        g.pushMatrix();
            g.translate(center.x, center.y);
            g.translate(0, ts / 2);
            g.textSize(ts);
            g.text(pair.id, 0,0);
        g.popMatrix();
    }


    public void draw3d(PGraphics g, PGraphics pdf)
    {
        Vec3D ori = pair.e1.v1.interpolateTo(pair.e1.v2, slot1.x);

        g.stroke(128);
        g.fill(48);

        g.pushMatrix();
        g.translate(ori.x, ori.y, ori.z);
        g.beginShape();
        if(pdf != null)
            pdf.beginShape();
        Vec3D pnt;
        for(int i = 0; i < shape.length; i++) {
            pnt = pair.getPoint3d(shape[i]);
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