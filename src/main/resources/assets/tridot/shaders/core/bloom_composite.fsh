#version 150

// The blurred light over the frame, squeezed by its brightest channel so it keeps its hue (a big ball of cyan light
// glows cyan, not white), and the effects' own light where it stands out over what is round it burning white (the tone
// mapper's highlights): a thin bolt's heart goes white, a big even ball of light keeps its colour.

uniform sampler2D BloomSampler;
uniform sampler2D HdrSampler;
uniform vec2 TexelSize;
uniform float Strength;
uniform float Whiten;

in vec2 texCoord;

out vec4 fragColor;

float peak(vec3 c) {
    return max(max(c.r, c.g), c.b);
}

void main() {
    vec3 bloom = texture(BloomSampler, texCoord).rgb * Strength;
    vec3 hdr = texture(HdrSampler, texCoord).rgb;
    vec2 d = TexelSize * 8.0;
    vec3 around = (texture(HdrSampler, texCoord + vec2(d.x, 0.0)).rgb + texture(HdrSampler, texCoord - vec2(d.x, 0.0)).rgb
            + texture(HdrSampler, texCoord + vec2(0.0, d.y)).rgb + texture(HdrSampler, texCoord - vec2(0.0, d.y)).rgb) * 0.25;
    float over = max(peak(hdr) - max(peak(around), 1.0), 0.0);
    float m = peak(bloom);
    vec3 glow = m > 1.0e-4 ? bloom * ((1.0 - exp(-m)) / m) : vec3(0.0);
    fragColor = vec4(glow + vec3(1.0 - exp(-over * Whiten)), 1.0);
}
