#ifdef GL_ES
precision mediump float;
#endif

varying vec2 v_texCoords;
uniform sampler2D u_texture;
uniform float u_punch; // 0.0 = no effect, 1.0 = max effect

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 tex_coords = v_texCoords;
    
    // --- Lens Distortion ---
    vec2 to_center = center - tex_coords;
    float dist = length(to_center);
    // Warp coordinates away from the center based on punch intensity
    tex_coords += to_center * dist * u_punch * 0.2;
    
    // --- Chromatic Aberration ---
    // Sample the texture at slightly offset coordinates for each color channel
    float r = texture2D(u_texture, tex_coords + vec2(0.01, 0.0) * u_punch).r;
    float g = texture2D(u_texture, tex_coords).g;
    float b = texture2D(u_texture, tex_coords - vec2(0.01, 0.0) * u_punch).b;
    
    gl_FragColor = vec4(r, g, b, 1.0);
}
