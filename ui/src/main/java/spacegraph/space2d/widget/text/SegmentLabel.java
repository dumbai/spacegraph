
//const vec2 ch_size  = vec2(0.9, 2.0);              // character size
//const vec2 ch_space = ch_size + vec2(0.7, 1.0);    // character distance
//const vec2 ch_start = vec2 (-8.8, 3.0); // start position
//      vec2 ch_pos   = vec2 (0.0, 0.0);             // character position
//float d = 1e6;
//
///* 16 segment display...
//Segment bit positions:
//  __2__ __1__
// |\    |    /|
// | \   |   / |
// 3  11 10 9  0
// |   \ | /   |
// |    \|/    |
//  _12__ __8__
// |           |
// |    /|\    |
// 4   / | \   7
// | 13 14  15 |
// | /   |   \ |
//  __5__|__6__
//
//15 12 11 8 7  4 3  0
// |  | |  | |  | |  |
// 0000 0000 0000 0000
//
//example: letter A
//
//   12    8 7  4 3210
//    |    | |  | ||||
// 0001 0001 1001 1111
//
// binary to hex -> 0x119F
//*/
//
//#define n0 ddigit(0x22FF,uv);
//        #define n1 ddigit(0x0281,uv);
//        #define n2 ddigit(0x1177,uv);
//        #define n3 ddigit(0x11E7,uv);
//        #define n4 ddigit(0x5508,uv);
//        #define n5 ddigit(0x11EE,uv);
//        #define n6 ddigit(0x11FE,uv);
//        #define n7 ddigit(0x2206,uv);
//        #define n8 ddigit(0x11FF,uv);
//        #define n9 ddigit(0x11EF,uv);
//
//        #define A ddigit(0x119F,uv);
//        #define B ddigit(0x927E,uv);
//        #define C ddigit(0x007E,uv);
//        #define D ddigit(0x44E7,uv);
//        #define E ddigit(0x107E,uv);
//        #define F ddigit(0x101E,uv);
//        #define G ddigit(0x807E,uv);
//        #define H ddigit(0x1199,uv);
//        #define I ddigit(0x4466,uv);
//        #define J ddigit(0x4436,uv);
//        #define K ddigit(0x9218,uv);
//        #define L ddigit(0x0078,uv);
//        #define M ddigit(0x0A99,uv);
//        #define N ddigit(0x8899,uv);
//        #define O ddigit(0x00FF,uv);
//        #define P ddigit(0x111F,uv);
//        #define Q ddigit(0x80FF,uv);
//        #define R ddigit(0x911F,uv);
//        #define S ddigit(0x8866,uv);
//        #define T ddigit(0x4406,uv);
//        #define U ddigit(0x00F9,uv);
//        #define V ddigit(0x2218,uv);
//        #define W ddigit(0xA099,uv);
//        #define X ddigit(0xAA00,uv);
//        #define Y ddigit(0x4A00,uv);
//        #define Z ddigit(0x2266,uv);
//        #define _ ch_pos.x += ch_space.x;
//        #define s_dot     ddigit(0,uv);
//        #define s_minus   ddigit(0x1100,uv);
//        #define s_plus    ddigit(0x5500,uv);
//        #define s_greater ddigit(0x2800,uv);
//        #define s_less    ddigit(0x8200,uv);
//        #define s_sqrt    ddigit(0x0C02,uv);
//
//        float dseg(vec2 p0, vec2 p1, vec2 uv)
//        {
//        vec2 dir = normalize(p1 - p0);
//        vec2 cp = (uv - ch_pos - p0) * mat2(dir.x, dir.y,-dir.y, dir.x);
//        return distance(cp, clamp(cp, vec2(0), vec2(distance(p0, p1), 0)));
//        }
//
//        bool bit(int n, int b)
//        {
//        return mod(floor(float(n) / exp2(floor(float(b)))), 2.0) != 0.0;
//        }
//
//        void ddigit(int n, vec2 uv)
//        {
//        float v = 1e6;
//        vec2 cp = uv - ch_pos;
//        if (n == 0)     v = min(v, dseg(vec2(-0.505, -1.000), vec2(-0.500, -1.000), uv));
//        if (bit(n,  0)) v = min(v, dseg(vec2( 0.500,  0.063), vec2( 0.500,  0.937), uv));
//        if (bit(n,  1)) v = min(v, dseg(vec2( 0.438,  1.000), vec2( 0.063,  1.000), uv));
//        if (bit(n,  2)) v = min(v, dseg(vec2(-0.063,  1.000), vec2(-0.438,  1.000), uv));
//        if (bit(n,  3)) v = min(v, dseg(vec2(-0.500,  0.937), vec2(-0.500,  0.062), uv));
//        if (bit(n,  4)) v = min(v, dseg(vec2(-0.500, -0.063), vec2(-0.500, -0.938), uv));
//        if (bit(n,  5)) v = min(v, dseg(vec2(-0.438, -1.000), vec2(-0.063, -1.000), uv));
//        if (bit(n,  6)) v = min(v, dseg(vec2( 0.063, -1.000), vec2( 0.438, -1.000), uv));
//        if (bit(n,  7)) v = min(v, dseg(vec2( 0.500, -0.938), vec2( 0.500, -0.063), uv));
//        if (bit(n,  8)) v = min(v, dseg(vec2( 0.063,  0.000), vec2( 0.438, -0.000), uv));
//        if (bit(n,  9)) v = min(v, dseg(vec2( 0.063,  0.063), vec2( 0.438,  0.938), uv));
//        if (bit(n, 10)) v = min(v, dseg(vec2( 0.000,  0.063), vec2( 0.000,  0.937), uv));
//        if (bit(n, 11)) v = min(v, dseg(vec2(-0.063,  0.063), vec2(-0.438,  0.938), uv));
//        if (bit(n, 12)) v = min(v, dseg(vec2(-0.438,  0.000), vec2(-0.063, -0.000), uv));
//        if (bit(n, 13)) v = min(v, dseg(vec2(-0.063, -0.063), vec2(-0.438, -0.938), uv));
//        if (bit(n, 14)) v = min(v, dseg(vec2( 0.000, -0.938), vec2( 0.000, -0.063), uv));
//        if (bit(n, 15)) v = min(v, dseg(vec2( 0.063, -0.063), vec2( 0.438, -0.938), uv));
//        ch_pos.x += ch_space.x;
//        d = min(d, v);
//        }
//
//        void main( void )
//        {
//        vec2 texcoord = ( gl_FragCoord.xy / resolution.xy );
//        //vec2 texcoord = vec2(gl_TexCoord[0].s, 1.0 - gl_TexCoord[0].t);
//        vec2 uv = texcoord - vec2(0.5);
//        uv *= 20.0 + sin(time);// set zoom size
//
//        ch_pos = ch_start + vec2(sin(uv.x)*sin(time)*.3,sin(uv.x+uv.y)*sin(time*.5)*.36);// set start position
//
//        G I L D A _ M A R I E L
//        ch_pos = ch_start + vec2(sin(uv.x)*sin(time)*.3 + 6.5,sin(uv.x+uv.y)*sin(time*.5)*.36);
//        ch_pos.y -= 3.0;
//        E R E S
//        ch_pos = ch_start + vec2(sin(uv.x)*sin(time)*.3,sin(uv.x+uv.y)*sin(time*.5)*.36);
//        ch_pos.y -= 6.0;
//        _ U N A _ D I V I N A
//
//        //vec3 color = mix(ch_color, bg_color, 1.0- (0.08 / d));// shading
//        vec3 color = mix(vec3(sin(time)+1.0,1.0,cos(time)+1.0), vec3(0.0,0.0,0.0), smoothstep(0.0, 0.0, d) - (0.10 / d));
//        if (length(color) > 0.6) gl_FragColor = vec4(color, 1.0);
//        else gl_FragColor = shape_fractal(texcoord);
//
