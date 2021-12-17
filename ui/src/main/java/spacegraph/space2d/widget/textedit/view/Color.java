package spacegraph.space2d.widget.textedit.view;

import com.jogamp.opengl.GL2;

class Color {
  private SmoothValue red = new SmoothValue(1);
  private SmoothValue green = new SmoothValue(1);
  private SmoothValue blue = new SmoothValue(1);
  private SmoothValue alpha = new SmoothValue(1);

  public SmoothValue getRed() {
    return red;
  }

  public void setRed(SmoothValue red) {
    this.red = red;
  }

  public SmoothValue getGreen() {
    return green;
  }

  public void setGreen(SmoothValue green) {
    this.green = green;
  }

  public SmoothValue getBlue() {
    return blue;
  }

  public void setBlue(SmoothValue blue) {
    this.blue = blue;
  }

  public SmoothValue getAlpha() {
    return alpha;
  }

  public void setAlpha(SmoothValue alpha) {
    this.alpha = alpha;
  }

  public boolean isAnimated() {
    return red.isAnimated() && green.isAnimated() && blue.isAnimated() && alpha.isAnimated();
  }

  public void updateColor(GL2 gl) {
    gl.glColor4d(red.value(), green.value(), blue.value(), alpha.value());
  }

  public void update(double red, double blue, double green, double alpha) {
    this.red.set(red);
    this.blue.set(blue);
    this.green.set(green);
    this.alpha.set(alpha);
  }

  public void updateWithoutSmooth(double red, double blue, double green, double alpha) {
    this.red.setWithoutSmooth(red);
    this.blue.setWithoutSmooth(blue);
    this.green.setWithoutSmooth(green);
    this.alpha.setWithoutSmooth(alpha);
  }
}
