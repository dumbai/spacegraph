// License: CC0 (http://creativecommons.org/publicdomain/zero/1.0/)
// from: http://www.madebyevan.com/shaders/grid/
// not working yet

#extension GL_OES_standard_derivatives : enable

varying vec3 vertex;
varying vec4 gl_Color; // readable on the fragment shader

void main() {
  // Pick a coordinate to visualize in a grid
  //vec2 coord = vertex.xy;

  // Compute anti-aliased world-space grid lines
  //vec2 grid = abs(fract(coord - 0.5) - 0.5) / fwidth(coord);
  //vec2 grid = abs(fract(coord - 0.5) - 0.5) / 1;
  //float line = min(grid.x, grid.y);

  // Just visualize the grid lines directly
  gl_FragColor = vec4(vec3(1.0 - min(line, 1.0)), 1.0);
}