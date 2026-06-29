#version 450

layout(binding = 0) uniform sampler2D texSampler;

layout(push_constant) uniform PC {
    vec4 tint;
} pc;

layout(location = 0) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 texColor = texture(texSampler, fragTexCoord);
    outColor = texColor * pc.tint;
}