package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL2;

class Scale {
  private SmoothValue x = new SmoothValue(1);
  private SmoothValue y = new SmoothValue(1);
  private SmoothValue z = new SmoothValue(1);

  public SmoothValue getX() {
    return x;
  }

  public void setX(SmoothValue x) {
    this.x = x;
  }

  public SmoothValue getY() {
    return y;
  }

  public void setY(SmoothValue y) {
    this.y = y;
  }

  public SmoothValue getZ() {
    return z;
  }

  public void setZ(SmoothValue z) {
    this.z = z;
  }

  public boolean isAnimated() {
    return x.isAnimated() && y.isAnimated() && z.isAnimated();
  }

  public void update(double x, double y, double z) {
    this.x.set(x);
    this.y.set(y);
    this.z.set(z);
  }

  public void updateWithoutSmooth(double x, double y, double z) {
    this.x.setWithoutSmooth(x);
    this.y.setWithoutSmooth(y);
    this.z.setWithoutSmooth(z);
  }

  public void updateScale(GL2 gl) {
    double _x = x.value();
    double _y = y.value();
    double _z = z.value();
    if (_x == 1 && _y == 1 && _z == 1) {
      return;
    }
    gl.glScaled(_x, _y, _z);
  }

}
