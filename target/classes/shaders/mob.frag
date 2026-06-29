#version 450

layout(binding = 1) uniform sampler2D texSampler;
layout(binding = 2) uniform sampler2DArrayShadow shadowMap;

layout(binding = 0) uniform UniformBufferObject
{
    mat4 model;
    mat4 view;
    mat4 proj;
    mat4 lightSpaceMatrices[3];
    vec4 cascadeSplits;
} ubo;

layout(location = 0) in vec2 fragTexCoord;
layout(location = 1) in vec3 fragNormal;
layout(location = 2) in float fragShade;
layout(location = 3) in vec3 fragWorldPos;
layout(location = 4) in float fragViewDepth;

layout(location = 0) out vec4 outColor;

float sampleShadow(int cascadeIndex)
{
    vec4 lightSpacePos = ubo.lightSpaceMatrices[cascadeIndex] * vec4(fragWorldPos, 1.0);
    vec3 projCoords = lightSpacePos.xyz / lightSpacePos.w;

    projCoords.xy = projCoords.xy * 0.5 + 0.5;

    if (projCoords.z < 0.0 || projCoords.z > 1.0)
        return 1.0;

    if (projCoords.x < 0.0 || projCoords.x > 1.0 || projCoords.y < 0.0 || projCoords.y > 1.0)
        return 1.0;

    return texture(shadowMap, vec4(projCoords.xy, float(cascadeIndex), projCoords.z));
}

void main()
{
    vec4 texColor = texture(texSampler, fragTexCoord);

    if (texColor.a < 0.1)
        discard;

    int cascadeIndex = 2;

    if (fragViewDepth < ubo.cascadeSplits.x)
        cascadeIndex = 0;
    else if (fragViewDepth < ubo.cascadeSplits.y)
        cascadeIndex = 1;
    else if (fragViewDepth < ubo.cascadeSplits.z)
        cascadeIndex = 2;

    float shadow = sampleShadow(cascadeIndex);

    float brightness = max(fragShade, 0.3);

    outColor = vec4(texColor.rgb * brightness * mix(0.6, 1.0, shadow), texColor.a);
}