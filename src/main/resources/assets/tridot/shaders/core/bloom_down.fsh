#version 150

// Dual-filter downsample: the source's texel and its four diagonal neighbours (bilinear: 16 texels in all). On the first
// step (Knee > 0) the light is first squeezed softly towards Knee, so a thick stack of light layers (a dome, a sonic boom)
// glows round itself instead of flooding its whole area, while a thin bolt still has light to spare.

uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;
uniform float Knee;

in vec2 texCoord;

out vec4 fragColor;

vec3 tap(vec2 uv) {
    vec3 c = texture(DiffuseSampler, uv).rgb;
    if (Knee > 0.0) {
        c /= 1.0 + max(max(c.r, c.g), c.b) / Knee;
    }
    return c;
}

void main() {
    vec2 h = TexelSize;
    vec3 sum = tap(texCoord) * 4.0;
    sum += tap(texCoord - h);
    sum += tap(texCoord + h);
    sum += tap(texCoord + vec2(h.x, -h.y));
    sum += tap(texCoord - vec2(h.x, -h.y));
    fragColor = vec4(sum / 8.0, 1.0);
}
