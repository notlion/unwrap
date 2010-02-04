package unwrap.layout;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.ArrayList;


import org.jbox2d.common.XForm;
import org.jbox2d.dynamics.Body;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import toxi.geom.util.STLReader;
import toxi.geom.util.TriangleMesh;
import unwrap.MeshConnector;
import unwrap.Util;
import unwrap.layout.ui.KillX;
import unwrap.shape.PolyShape;


public class UnwrapMultiSheet extends Box2dLayoutApp implements MouseWheelListener
{
    public static void main(String[] args)
    {
        PApplet.main(new String[]{ "unwrap.layout.UnwrapMultiSheet" });
    }


    static final float MAT_WIDTH     = px("14.5in");
    static final float MAT_HEIGHT    = px("9.5in");
    static final float MAT_THICKNESS = px("1.55mm");  // 1/8 in cardboard
    static final float MAT_AREA      = MAT_WIDTH * MAT_HEIGHT;

    static final float MODEL_UNIT_SCALE = px("1cm");

    static final float HULL_PADDING  = px("2mm");

    static final String MODEL_FILENAME = "blok.stl";


    TriangleMesh mesh;
    MeshConnector cnct;

    ArrayList<PolyShape> shapes, shapes_next;
    ArrayList<Body> bodies;
    ArrayList<KillX> exes;

    PFont font;

    float w2, h2;
    float zoom;
    float zoomScale = 4.0f;
    float modelRotX, modelRotY;

    boolean draw_3d = false;
    boolean save_pdf = false;

    int pdfPageNumber = 1;


    public void setup()
    {
        size(1280, 720, OPENGL);

        w2 = width / 2.0f;
        h2 = height / 2.0f;

        frame.addMouseWheelListener(this);
        frameRate(60);
        hint(ENABLE_OPENGL_4X_SMOOTH);

        font = createFont("Helvetica", 64);
        textFont(font);

        // Load Mesh
        mesh = new STLReader().loadBinary(dataPath("models" + File.separator + MODEL_FILENAME));
//        mesh.scale(MODEL_UNIT_SCALE);
//        mesh.centerOrigin();

        cnct = new MeshConnector(mesh, HULL_PADDING, MAT_THICKNESS);

        setupWorld(MAT_WIDTH, MAT_HEIGHT);

        bodies = new ArrayList<Body>();
        shapes = new ArrayList<PolyShape>();
        shapes_next = new ArrayList<PolyShape>();
        shapes_next.addAll(cnct.shapes);

        populateWorld();
    }

    public void populateWorld()
    {
        setWalls(false);

        for(Body b : bodies)
            world.destroyBody(b);

        // Queue up the next shapes
        shapes.clear();
        float totalArea = 0;
        while(shapes_next.size() > 0 && totalArea < MAT_AREA) {  // yeah i know this is inefficient
            int i = (int)random(shapes_next.size());
            totalArea += shapes_next.get(i).getHullArea();
            shapes.add(shapes_next.remove(i));
        }

        bodies.clear();
        bodies.addAll(getHullBodies(shapes));

        exes = new ArrayList<KillX>(bodies.size());
        for(Body b : bodies)
            exes.add(new KillX(this, b));
    }

    public void deferShape(Body body)
    {
        int index = bodies.indexOf(body);
        if(index > -1) {
            destroyMouseJoint();
            world.destroyBody(body);
            shapes_next.add(shapes.get(index));
            shapes.remove(index);
            bodies.remove(index);
            exes.remove(index);
        }
    }
    public void deferShapesOutsideMaterial()
    {
        Body body;
        for(int i = bodies.size(); --i >= 0;) {
            body = bodies.get(i);
            if(!materialContainsPnt(body.getXForm().position))
                deferShape(body);
        }
    }

    public void retry()
    {
        shapes_next.addAll(shapes);
        populateWorld();
    }


    public void draw()
    {
        float scaledZoom = zoom * zoomScale;

        background(0);
        translate(w2, h2);
        translate((mouseX - w2) * -scaledZoom, (mouseY - h2) * -scaledZoom);
        scale(scaledZoom + 1.0f);

        if(draw_3d) {
            scale(0.1f);
            scale(1, -1, 1);
            rotateY(modelRotY);
            rotateX(modelRotX);

            lights();
            hint(ENABLE_DEPTH_TEST);
            for(PolyShape shape : cnct.shapes) {
                shape.draw3d(g);
            }
            noLights();
//            cnct.drawEdgeNumbers(g, 50, -modelRotX, -modelRotY);
        }
        else {  // 2d
            worldStep();

            hint(DISABLE_DEPTH_TEST);

            scale(worldScale);
            drawBounds();

            // Page
            fill(255, 128);
            textSize(8);
            textAlign(LEFT);
            text("page " + pdfPageNumber, -mw2, mh2 + 13);
            textAlign(RIGHT);
            text("packing " + shapes.size() + " shapes\n" + "deferred " + shapes_next.size() + " shapes", mw2, mh2 + 13);
            textAlign(CENTER);
            text("walls " + (walls_on ? "on" : "off"), 0, mh2 + 13);

            Body body; XForm xf;
            for(int i = bodies.size(); --i >= 0;) {
                body = bodies.get(i);
                xf = body.getXForm();

                // Attract to center
                if(!walls_on || !materialContainsPnt(xf.position)) {
                    body.m_linearVelocity.mulLocal(0.75f).addLocal(
                        worldCenter.sub(xf.position).mulLocal(0.02f)
                    );
                }

                if(!walls_on || worldContainsPnt(xf.position)) {
                    pushMatrix();
                        applyXForm(g, xf);
                        shapes.get(i).draw2d(g);
                    popMatrix();
                }
                else {
                    deferShape(body);
                }
            }

            KillX ex;
            for(int i = exes.size(); --i >= 0;) {
                ex = exes.get(i);
                ex.draw(g);
            }
        }

        if(save_pdf)
        {
            if(draw_3d) {
                String pdfFile = sketchPath("out/unwrapped.3d.pdf");
                PGraphics pdf = createGraphics(width, height, PDF, pdfFile);
                pdf.beginDraw();
                pdf.noFill();
                pdf.strokeJoin(ROUND);
                pdf.strokeWeight(0.1f);
                for(PolyShape shape : cnct.shapes) {
                    shape.draw3d(g, pdf);
                }
                pdf.endDraw();
                pdf.dispose();
                open(pdfFile);
            }
            else {  // 2d
                deferShapesOutsideMaterial();

                String pdfFile = sketchPath("out/unwrapped_page" + nf(pdfPageNumber, 2) + ".pdf");
                PGraphics pdf = createGraphics((int)MAT_WIDTH, (int)MAT_HEIGHT, PDF, pdfFile);
                pdf.beginDraw();

                // Registration Mark
                pdf.strokeWeight(0.001f);
                pdf.stroke(0);
                pdf.noFill();
                pdf.beginShape();
                pdf.vertex(0, 5);
                pdf.vertex(0, 0);
                pdf.vertex(5, 0);
                pdf.endShape();
                pdf.beginShape();
                pdf.vertex(pdf.width, pdf.height - 5);
                pdf.vertex(pdf.width, pdf.height);
                pdf.vertex(pdf.width - 5, pdf.height);
                pdf.endShape();

                pdf.textFont(font);
                pdf.translate(pdf.width / 2.0f, pdf.height / 2.0f);
                for(int i = bodies.size(); --i >= 0;) {
                    XForm xf = bodies.get(i).getXForm();
                    pdf.pushMatrix();
                        applyXForm(pdf, xf);
                        shapes.get(i).draw2dPdf(pdf);
                    pdf.popMatrix();
                }
                pdf.endDraw();
                pdf.dispose();
                open(pdfFile);

                // Next Page
                if(shapes_next.size() > 0) {
                    pdfPageNumber++;
                    populateWorld();
                }
                else {
                    exit();
                }
            }
            save_pdf = false;
        }
    }


    public void mouseWheelMoved(MouseWheelEvent e)
    {
        zoom = Util.clamp(zoom + e.getWheelRotation() * -0.0125f, 0, 1);
    }

    public void keyPressed()
    {
        switch(keyCode)
        {
            case TAB: draw_3d = !draw_3d; break;

            case UP: modelRotX += PI * 0.025f; break;
            case DOWN: modelRotX -= PI * 0.025f; break;
            case LEFT: modelRotY -= PI * 0.025f; break;
            case RIGHT: modelRotY += PI * 0.025f; break;
        }

        switch(key)
        {
            case 's': save_pdf = true; break;
            case 'w': setWalls(!walls_on); break;
            case ' ': retry();
        }
    }
}
