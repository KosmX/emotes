#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
    vec2 OutSize;
    vec2 InSize;
};

layout(std140) uniform BlurConfig {
    vec2 BlurDir;
    float Radius;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

// Vanilla's box blur at a radius that needn't be whole: the texels one past the whole radius weigh its fraction.
// As vanilla's, it reads two neighbouring texels a fetch through linear filtering, placed between them by their weights:
// the far edge's fraction with the 1 next to it, then pairs of 1, then the near edge's fraction alone.
void main() {
    vec2 sampleStep = BlurDir / InSize;
    float edge = floor(Radius) + 1.0;
    float fraction = Radius - floor(Radius);

    vec4 blurred = texture(InSampler, texCoord + sampleStep * (1.0 / (1.0 + fraction) - edge)) * (1.0 + fraction);
    for (float a = 2.5 - edge; a < edge; a += 2.0) {
        blurred += texture(InSampler, texCoord + sampleStep * a) * 2.0;
    }
    blurred += texture(InSampler, texCoord + sampleStep * edge) * fraction;
    fragColor = blurred / (2.0 * Radius + 1.0);
}
