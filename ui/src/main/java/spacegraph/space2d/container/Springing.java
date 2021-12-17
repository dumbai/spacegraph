/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package spacegraph.space2d.container;

import spacegraph.space2d.Surface;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A Surface subclass that lays out children using auto sizing settings.
 * adapted from: https://github.com/reportmill/SnapKit
 *
 * TODO
 */
public class Springing extends ContainerSurface {

    // The last set size
    float _ow;
    float _oh;
    
    // The SpringInfos for children
    Map <Object,SpringInfo> _sinfos = new HashMap();
//
//    // A PropChangeListener to resetSpringInfo when child bounds change outside of layout
//    PropChangeListener       _pcl = pce -> childPropChange(pce);
    
/**
 * Override to add layout info.
 */
public void addChild(Surface aChild, int anIndex)
{
//    super.addChild(aChild, anIndex);
//    addSpringInfo(aChild);
//    aChild.addPropChangeListener(_pcl);
}

///**
// * Override to remove layout info.
// */
//public Surface removeChild(int anIndex)
//{
//    Surface child = super.removeChild(anIndex);
//    removeSpringInfo(child);
//    child.removePropChangeListener(_pcl);
//    return child;
//}

/**
 * Resets spring info for given child (or all children if null).
 */
public void resetSpringInfo(Surface aChild)
{
//    if(aChild!=null)
//        addSpringInfo(aChild);
//    else for(Surface v : getChildren())
//        addSpringInfo(v);
}

/**
 * Returns spring info for child.
 */
protected SpringInfo getSpringInfo(Surface aChild)  { return _sinfos.get(aChild); }

/**
 * Adds spring info for child.
 */
protected void addSpringInfo(Surface aChild)
{
//    float pw = getWidth(), ph = getHeight();
//    float x = aChild.getX(), y = aChild.getY(), w = aChild.getWidth(), h = aChild.getHeight();
//    SpringInfo sinfo = new SpringInfo(x,y,w,h,pw,ph);
//    _sinfos.put(aChild, sinfo); _ow = _oh = 0;
}

/**
 * Removes spring info for child.
 */
protected void removeSpringInfo(Surface aChild)  { _sinfos.remove(aChild); _ow = _oh = 0; }

///**
// * Returns preferred width.
// */
//protected float getPrefWidthImpl(float aH)  { return getWidth(); }
//
///**
// * Returns preferred height.
// */
//protected float getPrefHeightImpl(float aW)  { return getHeight(); }

/**
 * Override to perform layout.
 */
protected void layoutImpl()
{
//    Surface children[] = getChildren();
//    float pw = getWidth(), ph = getHeight(); if(pw==_ow && ph==_oh) return;
//    for(Surface child : children) layoutChild(child, pw, ph);
//    _ow = pw; _oh = ph;
}

/**
 * Returns the child rects for given parent height.
 */
protected void layoutChild(Surface aChild, float newPW, float newPH)
{
    SpringInfo sinfo = getSpringInfo(aChild);
//    String asize = aChild.getAutosizing();
//    float oldPW = sinfo.pwidth, oldPH = sinfo.pheight;
//    boolean lms = asize.charAt(0)=='~', ws = asize.charAt(1)=='~', rms = asize.charAt(2)=='~';
//    boolean tms = asize.charAt(4)=='~', hs = asize.charAt(5)=='~', bms = asize.charAt(6)=='~';
//    float x1 = sinfo.x, y1 = sinfo.y, w1 = sinfo.width, h1 = sinfo.height;
//    float sw = (lms? x1 : 0) + (ws? w1 : 0) + (rms? oldPW - (x1 + w1) : 0), dw = newPW - oldPW;
//    float sh = (tms? y1 : 0) + (hs? h1 : 0) + (bms? oldPH - (y1 + h1) : 0), dh = newPH - oldPH;
//
//    // Calculate new bounds and setAt
//    float x2 = (!lms || sw==0)? x1 : (x1 + dw*x1/sw);
//    float y2 = (!tms || sh==0)? y1 : (y1 + dh*y1/sh);
//    float w2 = (!ws || sw==0)? w1 : (w1 + dw*w1/sw);
//    float h2 = (!hs || sh==0)? h1 : (h1 + dh*h1/sh);
//    aChild.setBounds(x2,y2,w2,h2);
}

    @Override
    protected void doLayout(float dtS) {

    }

    @Override
    public int childrenCount() {
        return 0;
    }

    @Override
    public void forEach(Consumer<Surface> o) {

    }

    @Override
    public boolean whileEach(Predicate<Surface> o) {
        return false;
    }

    @Override
    public boolean whileEachReverse(Predicate<Surface> o) {
        return false;
    }

///**
// * Called when child property changes.
// */
//protected void childPropChange(Surface s)
//{
//    if(isInLayout()) return;
//    String pname = aPCE.getPropertyName();
//    if(pname==X_Prop || pname==Y_Prop || pname==Width_Prop || pname==Height_Prop)
//        resetSpringInfo((View)aPCE.getSource());
//}

///**
// * XML Archival.
// */
//public void fromXMLView(XMLArchiver anArchiver, XMLElement anElement)
//{
//    // Unarchive basic view attributes
//    super.fromXMLView(anArchiver, anElement);
//    setPrefSize(getWidth(), getHeight());
//}

/**
 * A class to hold info for a spring child.
 */
public static class SpringInfo {
    
    // The bounds and original parent width/height
    float x;
    float y;
    float width;
    float height;
    float pwidth;
    float pheight;
    
    /** Creates a SpringInfo. */
    public SpringInfo(float aX, float aY, float aW, float aH, float aPW, float aPH) {
        x = aX; y = aY; width = aW; height = aH; pwidth = aPW; pheight = aPH; }

    // Sets the rect
    public void setRect(float aX, float aY, float aW, float aH)  { x = aX; y = aY; width = aW; height = aH; }
}

}