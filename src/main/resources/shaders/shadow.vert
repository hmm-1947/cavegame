#version 450

layout(push_constant) uniform PushConstants {
    mat4 lightSpaceMatrix;
} pc;

layout(location = 0) in vec3 inPosition;

void main() {
    gl_Position = pc.lightSpaceMatrix * vec4(inPosition, 1.0);
}