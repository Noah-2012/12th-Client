#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float BlendFactor = 0.75;

out vec4 fragColor;

void main() {
    vec4 CurrTexel = texture(DiffuseSampler, texCoord);
    vec4 PrevTexel = texture(PrevSampler, texCoord);

    fragColor = mix(CurrTexel, PrevTexel, BlendFactor) + vec4(0.2, 0.0, 0.0, 0.0);
    fragColor.w = 1.0;
}