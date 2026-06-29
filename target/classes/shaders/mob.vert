#version 450

layout(binding = 0) uniform UniformBufferObject
{
    mat4 model;
    mat4 view;
    mat4 proj;
    mat4 lightSpaceMatrices[3];
    vec4 cascadeSplits;
} ubo;

layout(push_constant) uniform PushConstants {
    mat4 model;
} pc;

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;
layout(location = 2) in vec3 inNormal;
layout(location = 3) in float inShade;

layout(location = 0) out vec2 fragTexCoord;
layout(location = 1) out vec3 fragNormal;
layout(location = 2) out float fragShade;
layout(location = 3) out vec3 fragWorldPos;
layout(location = 4) out float fragViewDepth;

void main() {
    vec4 worldPos = pc.model * vec4(inPosition, 1.0);
    gl_Position = ubo.proj * ubo.view * worldPos;

    fragTexCoord = inTexCoord;
    fragNormal = mat3(pc.model) * inNormal;
    fragShade = inShade;
    fragWorldPos = worldPos.xyz;
    fragViewDepth = -(ubo.view * worldPos).z;
}