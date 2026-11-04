package pro.komaru.tridot.util.comps.phys;


import pro.komaru.tridot.util.phys.AbsRect;
import pro.komaru.tridot.util.phys.CenteredRect;
import pro.komaru.tridot.util.phys.Vec2f;

public interface Rectc extends X,Y {
    @Override
    Rectc x(float value);
    @Override
    Rectc y(float value);

    float w();
    float h();

    Rectc w(float w);
    Rectc h(float h);

    static Rectc init(float x, float y, float w, float h) {
        return new Rectc() {

            float xo = x,yo = y,wo = w,ho = h;

            @Override
            public Rectc x(float value) {
                xo = value;
                return this;
            }

            @Override
            public Rectc y(float value) {
                yo = value;
                return this;
            }

            @Override
            public float w() {
                return wo;
            }

            @Override
            public float h() {
                return ho;
            }

            @Override
            public Rectc w(float w) {
                wo = w;
                return this;
            }

            @Override
            public Rectc h(float h) {
                ho = h;
                return this;
            }

            @Override
            public float x() {
                return xo;
            }

            @Override
            public float y() {
                return yo;
            }
        };
    }

    default Rectc xywh(float x, float y, float w, float h) {
        x(x); y(y); w(w); h(h);
        return this;
    }
    default Rectc xywhTopLeft(float x, float y, float w, float h) {
        return xywh(x+w/2f,y+h/2f,w,h);
    }

    default Rectc translate(float x, float y) {
        x(x()+x); y(y()+y);
        return this;
    }

    default <XY extends X & Y> Rectc scale(XY whscl) {
        return scale(whscl,whscl);
    }
    default Rectc scale(X wscl, Y hscl) {
        return scale(wscl.x(),hscl.y());
    }
    default Rectc scale(float wscl, float hscl) {
        w(w()*wscl);
        h(h()*hscl);
        return this;
    }

    default boolean collides(Rectc rect) {
        Pos2 tl1 = topLeft(), tr1 = topRight(), bl1 = botLeft(), br1 = botRight();
        Pos2 tl2 = rect.topLeft(), tr2 = rect.topRight(), bl2 = rect.botLeft(), br2 = rect.botRight();

        return tr1.x() > tl2.x() && tl1.x() < tr2.x() && br1.y() > tl2.y() && bl1.y() < tr2.y();
    }

    default <XY extends X & Y> boolean inside(XY xy) {
        return inside(xy,xy);
    }
    default boolean inside(X x, Y y) {
        return inside(x.x(),y.y());
    }
    default boolean inside(float x, float y) {
        float otherX = x - x();
        float otherY = y - y();
        return -w()/2f <= otherX && otherY <= w()/2f &&
                -h()/2f <= otherY && otherY <= h()/2f;
    }

    default Vec2f topLeft() {
        return new Vec2f(x(), y()).add(-w()/2f,-h()/2f);
    }
    default Vec2f topRight() {
        return new Vec2f(x(), y()).add(w()/2f,-h()/2f);
    }
    default Vec2f botLeft() {
        return new Vec2f(x(), y()).add(-w()/2f,h()/2f);
    }
    default Vec2f botRight() {
        return new Vec2f(x(), y()).add(w()/2f,h()/2f);
    }

    default CenteredRect centered() {
        return new CenteredRect(x(), y(),w(),h());
    }
    default AbsRect absolute() {
        return AbsRect.xywhCent(x(), y(),w(),h());
    }
}
