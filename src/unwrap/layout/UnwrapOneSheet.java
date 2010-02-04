package unwrap.layout;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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
import unwrap.shape.PolyShape;


public class UnwrapOneSheet extends Box2dLayoutApp implements MouseWheelListener
{
    public static void main(String[] args)
    {
        PApplet.main(new String[]{ "meshwork.connectors.UnwrapOneSheet" });
    }


    static final float MAT_WIDTH     = px("19.5in");
    static final float MAT_HEIGHT    = px("14.5in");
    static final float MAT_THICKNESS = px("1/16in");

    static final float MAX_DIMENSION = px("8.5in");

    static final float HULL_PADDING  = px("2mm");


    TriangleMesh mesh;
    MeshConnector cnct;

    ArrayList<Body> bodies;

    PFont font;

    float w2, h2;
    float zoom;
    float zoomScale = 4.0f;
    float modelRotX, modelRotY;

    boolean draw3d = false;
    boolean savePdf = false;


    public void setup()
    {
        size(1280, 720, OPENGL);

        w2 = width / 2.0f;
        h2 = height / 2.0f;

        frame.addMouseWheelListener(this);
        frameRate(60);
        hint(ENABLE_OPENGL_4X_SMOOTH);

        font = createFont("Helvetica", 16);
        textFont(font);

        // Load Mesh
        mesh = new STLReader().loadBinary(dataPath("models/blok.stl"));
//        Vec3 ms = mesh.getBoundingBox().getScale();
//        float meshMaxDim = Math.max(Math.max(ms.x, ms.y), ms.z);
//        mesh.scale(MAX_DIMENSION / meshMaxDim);
//        mesh.centerOrigin();

        cnct = new MeshConnector(mesh, HULL_PADDING, MAT_THICKNESS);

        setupWorld(MAT_WIDTH, MAT_HEIGHT);

        bodies = new ArrayList<Body>();
        bodies.addAll(getHullBodies(cnct.shapes));
    }

    public void draw()
    {
        float scaledZoom = zoom * zoomScale;

        background(0);
        translate(w2, h2);
        translate((mouseX - w2) * -scaledZoom, (mouseY - h2) * -scaledZoom);
        scale(scaledZoom + 1.0f);

        if(draw3d) {
            rotateY(modelRotY);
            rotateX(modelRotX);

            lights();
            hint(ENABLE_DEPTH_TEST);
            for(PolyShape shape : cnct.shapes) {
                shape.draw3d(g);
            }
            noLights();
        }
        else {
            worldStep();

            hint(DISABLE_DEPTH_TEST);

            scale(worldScale);
            drawBounds();

            for(int i = bodies.size(); --i >= 0;) {
                XForm xf = bodies.get(i).getXForm();
                pushMatrix();
                    applyXForm(g, xf);
                    cnct.shapes.get(i).draw2d(g);
                popMatrix();
            }
        }

        if(savePdf)
        {
            if(draw3d) {
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
                String pdfFile = sketchPath("out/unwrapped.2d.pdf");
                PGraphics pdf = createGraphics((int)MAT_WIDTH, (int)MAT_HEIGHT, PDF, pdfFile);
                pdf.beginDraw();
                pdf.textFont(font);
                pdf.translate(pdf.width / 2.0f, pdf.height / 2.0f);
                for(int i = bodies.size(); --i >= 0;) {
                    XForm xf = bodies.get(i).getXForm();
                    pdf.pushMatrix();
                        applyXForm(pdf, xf);
                        cnct.shapes.get(i).draw2dPdf(pdf);
                    pdf.popMatrix();
                }
                pdf.endDraw();
                pdf.dispose();
                open(pdfFile);
            }
            savePdf = false;
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
            case TAB: draw3d = !draw3d; break;

            case UP: modelRotX += PI * 0.025f; break;
            case DOWN: modelRotX -= PI * 0.025f; break;
            case LEFT: modelRotY -= PI * 0.025f; break;
            case RIGHT: modelRotY += PI * 0.025f; break;
        }

        switch(key)
        {
            case 's': savePdf = true; break;
        }
    }
}
