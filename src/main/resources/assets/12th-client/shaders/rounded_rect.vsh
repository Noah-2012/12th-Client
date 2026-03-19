#version 150
in vec2 Position;
out vec2 fragCoord;

void main() {
    fragCoord = Position;
    gl_Position = vec4(Position, 0.0, 1.0);
}