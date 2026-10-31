#version 150

// Dual-filter upsample: a tent of eight taps round the smaller level's texel, added onto the larger level.

uniform sampler2D DiffuseSampler;
uniform vec2 TexelSize;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec2 h = TexelSize;
    vec3 sum = texture(DiffuseSampler, texCoord + vec2(-h.x * 2.0, 0.0)).rgb;
    sum += texture(DiffuseSampler, texCoord + vec2(-h.x, h.y)).rgb * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0, h.y * 2.0)).rgb;
    sum += texture(DiffuseSampler, texCoord + vec2(h.x, h.y)).rgb * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(h.x * 2.0, 0.0)).rgb;
    sum += texture(DiffuseSampler, texCoord + vec2(h.x, -h.y)).rgb * 2.0;
    sum += texture(DiffuseSampler, texCoord + vec2(0.0, -h.y * 2.0)).rgb;
    sum += texture(DiffuseSampler, texCoord + vec2(-h.x, -h.y)).rgb * 2.0;
    fragColor = vec4(sum / 12.0, 1.0);
}
