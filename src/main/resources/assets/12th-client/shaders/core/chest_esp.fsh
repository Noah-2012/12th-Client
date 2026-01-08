#version 150

uniform sampler2D DepthSampler;
uniform vec4 Color;

out vec4 fragColor;

void main() {
    // Bildschirmkoordinate normalisieren
    vec2 uv = gl_FragCoord.xy / vec2(textureSize(DepthSampler, 0));

    float sceneDepth = texture(DepthSampler, uv).r;
    float espDepth = gl_FragCoord.z;

    // Wenn hinter Wand → andere Farbe
    if (espDepth > sceneDepth + 0.0005) {
        fragColor = vec4(1.0, 0.0, 0.0, Color.a); // rot durch Wand
    } else {
        fragColor = Color; // sichtbar
    }
}
