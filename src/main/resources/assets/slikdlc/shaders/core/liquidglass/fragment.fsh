#version 150

#moj_import <slikdlc:common.glsl>

in vec2 FragCoord;
in vec2 TexCoord;
in vec4 FragColor;

uniform sampler2D Sampler0;
uniform vec2 Size;
uniform vec4 Radius;
uniform float Smoothness;
uniform float CornerSmoothness;
uniform float GlobalAlpha;
uniform float Time;
uniform float Freq;
uniform vec4 Color;

out vec4 OutColor;

// Simplex 3D Noise
vec4 permute(vec4 x){ return mod(((x*34.0)+1.0)*x, 289.0); }
vec4 taylorInvSqrt(vec4 r){ return 1.79284291400159 - 0.85373472095314 * r; }

float snoise(vec3 v){
    const vec2  C = vec2(1.0/6.0, 1.0/3.0);
    const vec4  D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 =   v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + 1.0 * C.xxx;
    vec3 x2 = x0 - i2 + 2.0 * C.xxx;
    vec3 x3 = x0 - 1. + 3.0 * C.xxx;

    i = mod(i, 289.0);
    vec4 p = permute(permute(permute(
    i.z + vec4(0.0, i1.z, i2.z, 1.0))
    + i.y + vec4(0.0, i1.y, i2.y, 1.0))
    + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 1.0/7.0;
    vec3  ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z *ns.z);

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt(vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0, x0), dot(p1, x1),
    dot(p2, x2), dot(p3, x3)));
}

float roundedBoxSDF(vec2 p, vec2 b, vec4 r, float smoothness) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;
    vec2 q = abs(p) - b + r.x;
    vec2 q_clamped = max(q, 0.0);
    float len = pow(pow(q_clamped.x, smoothness) + pow(q_clamped.y, smoothness), 1.0/smoothness);
    return min(max(q.x, q.y), 0.0) + len - r.x;
}

void main() {
    vec2 center = Size * 0.5;
    vec2 box_half_size = center - 1.0;
    vec2 pos = (FragCoord * Size) - center;

    float distance = roundedBoxSDF(-pos, box_half_size, Radius, CornerSmoothness);
    float alpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, distance);

    if (alpha < 0.01) {
        discard;
    }

    // Создаем 3D позицию для шума
    vec3 FragPos = vec3(FragCoord * 5.0, Time * 0.5);
    
    // Вычисляем нормаль с шумом
    vec3 normal = normalize(vec3(FragCoord - 0.5, 0.5) + snoise(FragPos) * Freq);
    
    // Вектор направления с шумом
    vec3 I = normalize(vec3(FragCoord - 0.5, 1.0) + snoise(FragPos) * Freq);
    
    // Отражение
    vec3 R = reflect(I, normal + snoise(FragPos) * Freq);
    vec3 cubeDirection = normalize(R);

    // CUBE MAPPING
    float x = cubeDirection.x;
    float y = cubeDirection.y;
    float z = cubeDirection.z;

    float absX = abs(x);
    float absY = abs(y);
    float absZ = abs(z);

    bool isXPositive = x > 0.0;
    bool isYPositive = y > 0.0;
    bool isZPositive = z > 0.0;

    float maxAxis, uc, vc;

    if (isXPositive && absX >= absY && absX >= absZ) {
        maxAxis = absX;
        uc = -z;
        vc = y;
    }
    else if (!isXPositive && absX >= absY && absX >= absZ) {
        maxAxis = absX;
        uc = z;
        vc = y;
    }
    else if (isYPositive && absY >= absX && absY >= absZ) {
        maxAxis = absY;
        uc = x;
        vc = -z;
    }
    else if (!isYPositive && absY >= absX && absY >= absZ) {
        maxAxis = absY;
        uc = x;
        vc = z;
    }
    else if (isZPositive && absZ >= absX && absZ >= absY) {
        maxAxis = absZ;
        uc = x;
        vc = y;
    }
    else {
        maxAxis = absZ;
        uc = -x;
        vc = y;
    }

    // Convert range from -1 to 1 to 0 to 1
    vec2 reflectionUV = 0.5 * (vec2(uc, vc) / maxAxis + 1.0);
    
    // Получаем фон из текстуры
    vec4 bgTexture = texture(Sampler0, TexCoord);
    vec3 bgColor = bgTexture.rgb;
    
    // Применяем эффект жидкого стекла - смешиваем фон с цветом темы
    vec3 glassColor = mix(bgColor, Color.rgb, 0.4);
    
    // Добавляем эффект отражения
    vec3 reflection = mix(glassColor, Color.rgb * 1.2, 0.3);
    
    // === КРАСИВЫЙ КОНТУР ===
    float borderWidth = 1.5;
    float borderDistance = roundedBoxSDF(-pos, box_half_size - vec2(borderWidth), Radius, CornerSmoothness);
    float borderAlpha = 1.0 - smoothstep(1.0 - Smoothness, 1.0, borderDistance);
    
    // Создаем маску контура
    float borderMask = alpha - borderAlpha;
    
    // Цвет контура с анимацией
    float gradient = sin(Time * 2.0 + (FragCoord.x + FragCoord.y) * 10.0) * 0.5 + 0.5;
    vec3 borderColor = mix(
        Color.rgb * 0.8,
        Color.rgb * 1.2,
        gradient
    );
    
    // Смешиваем основной цвет с контуром
    vec3 finalColor = mix(reflection, borderColor, borderMask * 0.9);
    
    OutColor = vec4(finalColor, alpha * GlobalAlpha);
}
