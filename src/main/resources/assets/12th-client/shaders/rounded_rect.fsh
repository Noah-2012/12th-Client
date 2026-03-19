#version 150
uniform vec2 u_Size;
uniform float u_Radius;
uniform vec4 u_Color;
in vec2 fragCoord;
out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return length(max(q, 0.0)) - r;
}

void main() {
    vec2 pos = fragCoord * u_Size - u_Size / 2.0;
    float dist = roundedBoxSDF(pos, u_Size / 2.0, u_Radius);
    float alpha = smoothstep(1.0, 0.0, dist);
    fragColor = vec4(u_Color.rgb, u_Color.a * alpha);
}