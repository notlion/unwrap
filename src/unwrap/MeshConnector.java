package unwrap;

import java.util.ArrayList;


import org.jbox2d.common.Settings;

import processing.core.PGraphics;
import toxi.geom.Matrix4x4;
import toxi.geom.util.TriangleMesh;
import toxi.geom.util.TriangleMesh.Face;
import unwrap.shape.ConnectorPolyShape;
import unwrap.shape.PolyShape;
import unwrap.shape.TriPolyShape;


public class MeshConnector
{
    private ArrayList<Tri> tris;
    private ArrayList<Edge> edges;
    private ArrayList<EdgePair> edgePairs;

    public ArrayList<PolyShape> shapes;


    public MeshConnector(TriangleMesh mesh, float _hullPadding, float _materialThickness)
    {
        if(Settings.maxPolygonVertices < 9)
            System.err.println("org.jbox2d.common.Settings.maxPolygonVertices must be set to a minimum of 10");

        tris = new ArrayList<Tri>(mesh.getNumFaces());
        edges = new ArrayList<Edge>(mesh.getNumFaces() * 3);
        edgePairs = new ArrayList<EdgePair>();

        for(Face face : mesh.faces) {
            Tri t = new Tri(face.a, face.b, face.c);
            t.addEdgesTo(edges);
            tris.add(t);
        }

        // Match Edges
        Edge e1, e2;
        for(int i = edges.size(); --i >= 0;) {
            e1 = edges.get(i);
            for(int j = edges.size(); --j >= 0;) {
                e2 = edges.get(j);
                if(e1 != e2 && e2.pair == null && e1.hasSameVertices(e2)) {
                    EdgePair ep = new EdgePair(e1, e2, edgePairs.size(), _materialThickness);
                    edgePairs.add(ep);
                }
            }
        }

        // Gen PolyShapes
        shapes = new ArrayList<PolyShape>();
        for(Tri tri : tris) {
            PolyShape shp = new TriPolyShape(tri, _hullPadding, _materialThickness);
            shapes.add(shp);
        }
        for(EdgePair pair : edgePairs) {
            shapes.add(new ConnectorPolyShape(pair, _hullPadding, _materialThickness, false));
            shapes.add(new ConnectorPolyShape(pair, _hullPadding, _materialThickness, true));
        }
    }


    public void drawTris(PGraphics g)
    {
        g.beginShape(PGraphics.TRIANGLES);
        for(Tri tri : tris) {
            g.vertex(tri.a2.x, tri.a2.y);
            g.vertex(tri.b2.x, tri.b2.y);
            g.vertex(tri.c2.x, tri.c2.y);
        }
        g.endShape();
    }

//    public void drawEdgeNumbers(PGraphics g, float size, float rx, float ry)
//    {
//        g.fill(255, 0, 0);
//        g.textSize(size);
//        g.textAlign(PGraphics.CENTER, PGraphics.CENTER);
////        Matrix4x4 m = Matrix4x4.fromDirectionAndUp(Vec3.normalized(facingDir), new Vec3(0,1,0));
//        for(EdgePair pair : edgePairs) {
//            g.pushMatrix();
//            g.translate(
//                pair.e1.center.x + pair.normal.x * size,
//                pair.e1.center.y + pair.normal.y * size,
//                pair.e1.center.z + pair.normal.z * size
//            );
//            g.scale(1, -1, 1); // HACK
//            g.rotateX(rx);
//            g.rotateY(ry);
////            applyMatrix(g, m);
//            g.text(pair.id, 0,0,0);
//            g.popMatrix();
//        }
//    }

    private void applyMatrix(PGraphics g, Matrix4x4 matrix)
    {
        double[][] m = matrix.matrix;
        g.applyMatrix(
            (float)m[0][0], (float)m[0][1], (float)m[0][2], (float)m[0][3],
            (float)m[1][0], (float)m[1][1], (float)m[1][2], (float)m[1][3],
            (float)m[2][0], (float)m[2][1], (float)m[2][2], (float)m[2][3],
            (float)m[3][0], (float)m[3][1], (float)m[3][2], (float)m[3][3]
        );
    }
}
