package unwrap.layout;

import java.util.ArrayList;


import org.jbox2d.collision.AABB;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.MouseJoint;
import org.jbox2d.dynamics.joints.MouseJointDef;

import processing.core.PApplet;
import processing.core.PGraphics;
import toxi.geom.Vec2D;
import unwrap.shape.PolyShape;


public class Box2dLayoutApp extends PApplet
{
    public World world;
    public AABB worldAABB;
    public float worldScale;

    public Vec2 worldCenter;
    public Vec2 worldGravity;

    public float materialWidth, mw2;
    public float materialHeight, mh2;

    protected boolean walls_on;

    protected ArrayList<PolygonDef> wall_polys;
    protected ArrayList<Body> wall_bodies;

    protected MouseJoint mouseJoint;


    public void setupWorld(float matWidth, float matHeight)
    {
        materialWidth = matWidth;
        materialHeight = matHeight;
        mw2 = materialWidth / 2.0f;
        mh2 = materialHeight / 2.0f;

        worldScale = min(width / materialWidth, height / materialHeight) * 0.75f;

        worldAABB = new AABB();
        worldAABB.lowerBound.set(-materialWidth * 1.5f, -materialHeight * 1.5f);
        worldAABB.upperBound.set(materialWidth * 1.5f, materialHeight * 1.5f);

        worldGravity = new Vec2(0, 20);

        world = new World(worldAABB, worldGravity, true);
        worldCenter = new Vec2();

        wall_polys = new ArrayList<PolygonDef>();
        wall_bodies = new ArrayList<Body>();
        makeBoundaryWalls();
    }

    public void clearWorld()
    {
        Body body = world.getBodyList();
        while(body != null) {
            if(body.isDynamic())
                world.destroyBody(body);
            body = body.getNext();
        }
    }


    public void makeBoundaryWalls()
    {
        float th = Math.min(materialWidth, materialHeight) * 0.025f;
        float wx = (materialWidth + th) / 2;
        float wy = (materialHeight + th) / 2;
        float ww = materialWidth + th*2;
        float wh = materialHeight + th*2;

        makeWall(0,wy, ww,th);
        makeWall(0,-wy, ww,th);
        makeWall(wx,0, th,wh);
        makeWall(-wx,0, th,wh);

        walls_on = true;
    }
    public void destroyBoundaryWalls()
    {
        for(Body b : wall_bodies)
            world.destroyBody(b);
        wall_polys.clear();
        wall_bodies.clear();

        walls_on = false;
    }


    public void setWalls(boolean on)
    {
        if(on != walls_on) {
            if(on) {
                makeBoundaryWalls();
                world.setGravity(worldGravity);
            }
            else {
                destroyBoundaryWalls();
                world.setGravity(new Vec2());
            }
        }
    }


    public void makeWall(float x, float y, float w, float h)
    {
        BodyDef gr = new BodyDef();
        gr.position = new Vec2(x, y);

        PolygonDef gr_box = new PolygonDef();
        gr_box.setAsBox(w / 2.0f, h / 2.0f);

        Body gr_body = world.createBody(gr);
        gr_body.createShape(gr_box);

        wall_polys.add(gr_box);
        wall_bodies.add(gr_body);
    }


    public void worldStep()
    {
        float timeStep = 1.0f / 30.0f;
        int iterations = 16;

        world.step(timeStep, iterations);
    }

    public Vec2 worldPnt(float x, float y)
    {
        return new Vec2(
            (x - width/2.0f) / worldScale,
            (y - height/2.0f) / worldScale
        );
    }


    public boolean worldContainsPnt(Vec2 pnt)
    {
        return pnt.x > worldAABB.lowerBound.x && pnt.y > worldAABB.lowerBound.y &&
            pnt.x < worldAABB.upperBound.x && pnt.y < worldAABB.upperBound.y;
    }
    public boolean materialContainsPnt(Vec2 pnt)
    {
        return pnt.x > -mw2 && pnt.y > -mh2 && pnt.x < mw2 && pnt.y < mh2;
    }


    public void drawBounds()
    {
        noStroke();
        rectMode(CORNERS);

        if(walls_on)
            fill(10,32,64, 128);
        else
            fill(20);
        rect(worldAABB.lowerBound.x, worldAABB.lowerBound.y, worldAABB.upperBound.x, worldAABB.upperBound.y);

        if(walls_on)
            stroke(20,64,128, 128);
        else
            stroke(64, 128);
        strokeWeight(1);
        fill(0);
        rect(-mw2, -mh2, mw2, mh2);

        rectMode(CORNER);
    }

//	public void drawWalls()
//	{
//		fill(10,32,64, 128);
//		stroke(20,64,128);
//
//		beginShape(QUADS);
//		for(int i = wall_polys.size(); --i >= 0;)
//		{
//			XForm xf = wall_bodies.get(i).getXForm();
//			Vec2 vtx[] = wall_polys.get(i).getVertexArray();
//
//			for(int j = 0; j < vtx.length; j++) {
//				Vec2 v = XForm.mul(xf, vtx[j]);
//				vertex(v.x * worldScale, v.y * worldScale);
//			}
//		}
//		endShape();
//	}


    public ArrayList<Body> getHullBodies(ArrayList<PolyShape> shapes)
    {
        int nxy = Math.max(1, (int)Math.sqrt(shapes.size()));

        float mw2 = materialWidth / 2;
        float mh2 = materialHeight / 2;

        ArrayList<Body> hull_bodies = new ArrayList<Body>();

        for(int i = 0, n = shapes.size(); i < n; i++)
        {
            PolyShape shp = shapes.get(i);

            BodyDef bd = new BodyDef();
            if(n >= 4)
                bd.position = new Vec2(
                    map(i % nxy, 0, nxy-1, -mw2, mw2) * 0.75f,
                    map(i / nxy, 0, nxy-1, -mh2, mh2) * 0.75f
                );
            else
                bd.position = new Vec2(
                    random(-mw2, mw2) * 0.75f,
                    random(-mh2, mh2) * 0.75f
                );
            bd.angle = 0.0f;

            Body body = world.createBody(bd);
            body.createShape(getPolygonDef(shp.hull));
            body.setMassFromShapes();

            hull_bodies.add(body);
        }

        return hull_bodies;
    }

    public PolygonDef getPolygonDef(Vec2D[] shape)
    {
        PolygonDef poly = new PolygonDef();
        for(int i = shape.length; --i >= 0;)
            poly.addVertex(new Vec2(shape[i].x, shape[i].y));
        poly.density = 2.0f;

        return poly;
    }


    public static void applyXForm(PGraphics g, XForm xf)
    {
        g.translate(xf.position.x, xf.position.y);
        g.applyMatrix(
            xf.R.col1.x, xf.R.col2.x, 0,
            xf.R.col1.y, xf.R.col2.y, 0
        );
    }


    public void mousePressed()
    {
        Vec2 d = new Vec2(0.001f, 0.001f);
        Vec2 wm = worldPnt(mouseX, mouseY);
        AABB aabb = new AABB(wm.sub(d), wm.add(d));

        // Query the world for overlapping shapes.
        Shape shapes[] = world.query(aabb, 10);

        Body body = null;
        for(int j = 0; j < shapes.length; j++) {
            Body shapeBody = shapes[j].getBody();
            if(
                shapeBody.isStatic() == false &&
                shapes[j].testPoint(shapeBody.getXForm(), wm)
            ) {
                body = shapes[j].m_body;
                break;
            }
        }

        if(body != null) {
            MouseJointDef md = new MouseJointDef();
            md.body1 = world.getGroundBody();
            md.body2 = body;
            md.target.set(wm);
            md.maxForce = 4000.0f * body.m_mass;
            mouseJoint = (MouseJoint)world.createJoint(md);
            body.wakeUp();
        }
    }

    public void mouseDragged()
    {
        if(mouseJoint != null)
            mouseJoint.setTarget(worldPnt(mouseX,mouseY));
    }

    public void mouseReleased()
    {
        destroyMouseJoint();
    }

    protected void destroyMouseJoint()
    {
        if(mouseJoint != null) {
            world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
    }


    public static float px(String numStr)
    {
        String type = numStr.substring(numStr.length()-2);
        String number = numStr.substring(0, numStr.length()-2);

        float num;

        // Check for Fractions
        if(number.indexOf('/') != -1) {
            String[] parts = number.split("/");
            num = Float.parseFloat(parts[0]) / Float.parseFloat(parts[1]);
        }
        else {
            num = Float.parseFloat(number);
        }

        // Convert Units
        if(type.equals("mm"))
            return num / 25.4f * 72;
        if(type.equals("cm"))
            return num / 2.54f * 72;
        if(type.equals("in"))
            return num * 72;

        return num;
    }


    public static float triArea(Vec2 p1, Vec2 p2, Vec2 p3)
    {
        return 0.5f * Math.abs((p1.x * (p2.y - p3.y)) + (p2.x * (p3.y - p1.y)) + (p3.x * (p1.y - p2.y)));
    }
}
