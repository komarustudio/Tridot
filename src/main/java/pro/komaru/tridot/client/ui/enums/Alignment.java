package pro.komaru.tridot.client.ui.enums;

public class Alignment {
    public static final int
            NONE = 0,

    CENTER_X = 1 << 0,
            LEFT = 1 << 1,
            RIGHT = 1 << 2,

    CENTER_Y = 1 << 3,
            TOP = 1 << 4,
            BOTTOM = 1 << 5,

    HORIZONTAL_MASK = LEFT | CENTER_X | RIGHT,

    VERTICAL_MASK = TOP | CENTER_Y | BOTTOM,

    CENTER = CENTER_X | CENTER_Y;

    public static boolean has(int alignment, int other) {
        return (alignment & other) != 0;
    }
}
