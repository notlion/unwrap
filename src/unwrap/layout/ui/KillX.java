package unwrap.layout.ui;

import java.awt.event.MouseEvent;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import processing.core.PApplet;
import processing.core.PGraphics;
import unwrap.Util;
import unwrap.layout.Box2dLayoutApp;
import unwrap.layout.UnwrapMultiSheet;


public class KillX
{
    private UnwrapMultiSheet _app;

    public Body body;

    private boolean _over, _down;
    public float radius = 12;


    public KillX(UnwrapMultiSheet app, Body _body)
    {
        body = _body;
        _app = app;
        _app.registerMouseEvent(this);
    }


    public void draw(PGraphics g)
    {
        g.pushMatrix();
        Box2dLayoutApp.applyXForm(g, body.getXForm());

        g.scale(1 / _app.worldScale);

        g.strokeWeight(2);
        if(_over) {
            g.stroke(255, 128);
            g.fill(255, 32);
        }
        else {
            g.stroke(255, 0, 0, 128);
            g.fill(255, 0, 0, 32);
        }
        g.ellipseMode(PGraphics.CENTER);
        g.ellipse(0, 0, radius * 2, radius * 2);

        float r = radius * 0.33f;
        g.line(-r, -r, r, r);
        g.line(-r, r, r, -r);

        g.popMatrix();
    }


    public void mouseEvent(MouseEvent e)
    {
        switch(e.getID()) {
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_DRAGGED:
                _over = hitTest(e.getX(), e.getY());
                if(!_over)
                    _down = false;
                break;
            case MouseEvent.MOUSE_PRESSED:
                if(_over)
                    _down = true;
                break;
            case MouseEvent.MOUSE_RELEASED:
                if(_down) {
                    _down = false;
                    _app.deferShape(body);
                }
        }
    }

    public boolean hitTest(float x, float y)
    {
        Vec2 pos = body.getPosition();
        Vec2 pnt = _app.worldPnt(x, y);
        return Util.distSq(pos.x, pos.y, pnt.x, pnt.y) < PApplet.sq(radius / _app.worldScale);
    }
}
