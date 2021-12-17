/*
 * Hershey Fonts
 * http://paulbourke.net/dataformats/hershey/
 * 
 * Drawn in Processing.
 *
 */

int originX, originY;

// Hershey fonts use coordinates represented by characters'
// integer values relative to ascii 'R'
int offsetR = int('R'); 

// a curly brace from the Hershey characters
String exampleCurly = " 2226 40KYPBRCSDTFTHSJRKQMQOSQ R" + 
"RCSESGRIQJPLPNQPURQTPVPXQZR[S]S_Ra R" +
"SSQUQWRYSZT\\T^S`RaPb";

boolean isInteger(String str) {
  return trim(str).matches("-?\\d+");
}


// Point wrapper -- no tuples in Processing
// (Bonus: can tell you the ascii character pair that
// corresponds to it in the original file)
class Point {
  int x, y;

  Point(int ix, int iy) {
    x = ix;
    y = iy;
  }

  String toString() {
    return "<Point: (" + x + "," + y + ") [" + repr() + "]>";
  }

  String repr() {
    return "" + char(x + offsetR) + char(y + offsetR); // empty string necessary for append
  }
}

class HGlyph {
  int idx, verts, leftPos, rightPos;
  String spec;
  ArrayList<ArrayList<Point>> segments;

  HGlyph(String hspec) {
    segments = new ArrayList<ArrayList<Point>>();

    idx      = int(trim(hspec.substring(0, 5)));
    verts    = int(trim(hspec.substring(5, 8)));
    spec     = trim(hspec.substring(10));

    // TODO: is this needed?
    leftPos  = int(hspec.charAt(8)) - offsetR;
    rightPos = int(hspec.charAt(9)) - offsetR;

    int curX, curY;
    boolean penUp = true;
    ArrayList<Point> currentSeg = new ArrayList<Point>();

    for (int i = 0; i < spec.length() - 1; i += 2) {
      if (spec.charAt(i+1) == 'R' && spec.charAt(i) == ' ') {
        penUp = true;
        segments.add(currentSeg);
        currentSeg = new ArrayList<Point>();
        continue;
      }

      curX = int(spec.charAt(i)) - offsetR;
      curY = int(spec.charAt(i + 1)) - offsetR;
      Point p = new Point(curX, curY);
      currentSeg.add(p);
    }
    segments.add(currentSeg);
  }

  String toString() {
    return "<HGlyph: " + idx + ">";
  }

  void draw() {
    Point pLast, p;
    int cr = 50, cg = 128, cb = 50;
    
    for (int i = 0; i < segments.size(); i++) {
      cb = 50;
      ArrayList<Point> seg = segments.get(i);
      if (seg.size() == 0) {
        continue;
      }
      for (int j = 1; j < seg.size(); j++) {
        
        fill(cr, cg, cb);
        pLast = seg.get(j - 1);

        // annotate pLast
        ellipse(tx(pLast.x, 20.0 + sin(frameCount)), ty(pLast.y, 20.0+ sin(frameCount)), 10, 10);
        //text(pLast.repr(), tx(pLast.x, 20.0) + 20, ty(pLast.y, 20.0) + 5);
        //line(tx(pLast.x, 20.0), ty(pLast.y, 20.0), tx(pLast.x, 20.0) + 20, ty(pLast.y, 20.0));

        p = seg.get(j);
        // connect pLast to p
        line(tx(pLast.x, 20.0 + sin(frameCount)), ty(pLast.y, 20.0 + sin(frameCount)), tx(p.x, 20.0 + sin(frameCount)), ty(p.y, 20.0 + sin(frameCount)));
        
        cb = (cb + 20) % 256;
      }

      // handle last point since we haven't annotated it with an ellipse
      p = seg.get(seg.size() - 1);
      ellipse(tx(p.x, 20.0 + sin(frameCount)), ty(p.y, 20.0 + sin(frameCount)), 10, 10);
      //text(p.repr(), tx(p.x, 20.0) + 20, ty(p.y, 20.0) + 5);
      //line(tx(p.x, 20.0), ty(p.y, 20.0), tx(p.x, 20.0) + 20, ty(p.y, 20.0));
      cr = (cr + 50) % 256;
    }
  }
}

HGlyph curly;
HGlyph randoglyph;
String lines[];
ArrayList<HGlyph> glyphs;
int glyphline = 0;

void setup() {
  size(640, 640);
  frameRate(14);
  originX = width / 2;
  originY = height / 2;
  lines = loadStrings("scripts.jhf");

  glyphs = new ArrayList<HGlyph>();
  String scratch = "";
  HGlyph nextGlyph;
  for (int i = lines.length - 1; i > 0; i--) {
    if (lines[i].length() < 5) {
      continue;
    }
    //    if (lines[i].charAt(0) == ' ') {
    if (isInteger(lines[i].substring(0, 5))) {
      nextGlyph = new HGlyph(lines[i] + scratch);
      //      println("Instantiated glyph " + nextGlyph.idx);
      glyphs.add(nextGlyph);
      scratch = "";
    } 
    else {
      scratch += lines[i];
    }
  }
  //noLoop();  // Draw only one time
  //parseHershey(exampleCurly);
  curly = new HGlyph(exampleCurly);
  //  curly.draw();
  //  randoglyph = new HGlyph(lines[58]);
  //  randoglyph.draw();
  randoglyph = new HGlyph(lines[glyphline]);
  currentGlyph = glyphs.get(glyphline);
}

/*
 * tx, ty - transform from relative coordinates to pixels
 *
 * Relative coordinates are in the [-1 * max, max] range.
 */

int tx(float relX, float max) { 
  return int(originX + (relX / max) * originX);
}
int ty(float relY, float max) { 
  return int(originY + (relY / max) * originY);
}

HGlyph currentGlyph;

void keyReleased() {
  if (keyCode == LEFT) {
    glyphline = glyphline - 1;
    if (glyphline < 0) {
      glyphline = glyphs.size() - 1;
    }
  } 
  else if (keyCode == RIGHT) {
    glyphline = (glyphline + 1) %  glyphs.size();
  }
  currentGlyph = glyphs.get(glyphline);
}

void draw() {
  //  println("draw");
  background(255, 255, 255);
  

  currentGlyph.draw();
  fill(0, 102, 153);
  text("ID: " + currentGlyph.idx, 10, 20);
  text("Internal ID: " + glyphline, 10, 50);
}

