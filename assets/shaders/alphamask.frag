#ifdef GL_ES
precision mediump float;
#endif

varying vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;  // Background texture
uniform sampler2D u_mask;      // Mask texture (levelInfo)
uniform vec2 u_maskOffset;     // Mask top-left position in screen space
uniform vec2 u_maskScale;      // Mask size in screen space
uniform vec2 u_bgOffset;       // Background top-left position in screen space
uniform vec2 u_bgScale;        // Background size in screen space

void main() {
    // Sample the background texture
    vec4 bgColor = texture2D(u_texture, v_texCoords) * v_color;

    // Convert texture coords to screen position (for the background quad)
    vec2 screenPos = u_bgOffset + v_texCoords * u_bgScale;

    // Convert screen position to mask texture coordinates
    vec2 maskCoords = (screenPos - u_maskOffset) / u_maskScale;

    // Check if we're within the mask bounds
    if (maskCoords.x < 0.0 || maskCoords.x > 1.0 || maskCoords.y < 0.0 || maskCoords.y > 1.0) {
        discard; // Outside mask bounds
    }

    // Sample the mask - only show background where mask has alpha
    vec4 maskColor = texture2D(u_mask, maskCoords);

    // Apply mask alpha to background
    gl_FragColor = bgColor;
    gl_FragColor.a *= maskColor.a;

    // Discard fully transparent pixels
    if (gl_FragColor.a < 0.01) {
        discard;
    }
}
