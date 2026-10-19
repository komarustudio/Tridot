package pro.komaru.tridot.api.fx;

import pro.komaru.tridot.util.*;
import pro.komaru.tridot.util.math.*;

import static pro.komaru.tridot.client.render.Draw3D.*;

public class Fx {
    public static final Effect
            none = new Effect(0.0F, e -> {}),

    bigShockwave = new Effect(28f, (e, pose) -> {
        color(e.color, Col.darkGray, e.fin());
        alpha(e.fout(Interp.pow2Out) * 0.25f);
        sphere(pose, 0, 0, 0, e.fin(Interp.pow3Out) * 8);

        color(e.color, Col.white, e.fin());
        alpha(e.fout(Interp.pow2Out) * 0.35f);
        sphere(pose, 0, 0, 0, e.fin(Interp.pow3Out) * 5.5f);

        alpha(1f);

        color(Col.white, e.color, e.fin());
        stroke(e.fout() * 0.55f);
        circle(pose, 0, 0.05f, 0, e.fin(Interp.pow4Out) * 8.0f);

        stroke(e.fout() * 0.25f);
        circle(pose, 0, 0.05f, 0, e.fin(Interp.pow4Out) * 6.0f);

        stroke(e.fout() * 0.08f);
        e.randLenVectors3D(e.id, 18, e.fin(Interp.pow2Out) * 4.5f, (dx, dy, dz) -> {
            lineAngle3D(pose, dx, Math.abs(dy), dz, dx, Math.abs(dy), dz, e.fout() * 1.1f + 0.2f);
        });
    });
}
