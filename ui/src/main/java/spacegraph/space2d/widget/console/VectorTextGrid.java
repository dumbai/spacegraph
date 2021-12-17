package spacegraph.space2d.widget.console;


import com.jogamp.opengl.GL2;
import jcog.TODO;
import spacegraph.space2d.ReSurface;
import spacegraph.video.Draw;
import spacegraph.video.font.HersheyFont;

import java.awt.*;


/** vector font console
 * TODO combine with BitmapTextGrid
 * */
@Deprecated public abstract class VectorTextGrid extends AbstractConsoleSurface {

    @Deprecated
    protected static class TextCharacter {

        static final float[] WHITE = { 1, 1, 1};

        public char c;
        public float[] fgColor;
        public float[] bgColor;

        public TextCharacter(char c) {
            this.c = c;
            this.fgColor = WHITE;
        }
    }
    private float[] bg;

    private static final float fontThickness = 3.0f;
    private static final Color TRANSLUCENT = new Color(Transparency.TRANSLUCENT);


    private float fgAlpha = 0.9f;


    protected VectorTextGrid(int cols, int rows) {
        resize(cols, rows);
    }


    @Override
    protected void paintIt(GL2 gl, ReSurface r) {
        Draw.bounds(bounds, gl, gl2 -> doPaint(gl2, r));
    }

    private void doPaint(GL2 gl, ReSurface r) {
        /*
         * percent of each grid cell width filled with the character
         */
        /**
         * percent of each grid cell height filled with the character
         */




        gl.glPushMatrix();


        gl.glScalef(1.0f / cols, 1.0f / rows, 1.0f);

        
        
        


        gl.glLineWidth(fontThickness);

        
        bg = null;

        float dz = 0.0f;
        float charScaleY1 = 0.85f;
        float charScaleY = charScaleY1;
        float charScaleX1 = 0.85f;
        float charScaleX = charScaleX1;
        for (int row = 0; row < rows; row++) {

            gl.glPushMatrix();

            HersheyFont.textStart(gl,
                    charScaleX, charScaleY,
                    
                    0.5f, rows - 1 - row,
                    dz);


            for (int col = 0; col < cols; col++) {


                TextCharacter c = charAt(col, row);


                if (setBackgroundColor(gl, c, col, row)) {
                    Draw.rect(gl,
                            Math.round((col - 0.5f) * 20 / charScaleX), 0,
                            Math.round(20.0f / charScaleX), 24
                    );
                }




                


                char cc = visible(c.c);


                if (cc != 0) {

                    

                    
                    

                    float[] fg = c.fgColor;
                    gl.glColor4f(fg[0], fg[1], fg[2], fgAlpha);

                    HersheyFont.textNext(gl, cc, col / charScaleX);

                }
            }


            gl.glPopMatrix(); 
        }

        int[] cursor = getCursorPos();
        int curx = cursor[0];
        int cury = cursor[1];


        long t = r.frameNS/1_000_000L;
        float p = (1.0f + (float) Math.sin(t / 100.0)) * 0.5f;
        gl.glColor4f(1.0f, 0.7f, 0.0f, 0.4f + p * 0.4f);

        Draw.rect(curx + p / 2.0f, rows - 1 - cury, 1 - p, 1 - p, -dz, gl
        );

        gl.glPopMatrix();

    }

    @Deprecated public TextCharacter charAt(int col, int row) {
        //return new TextCharacter(text.charAt(col), fgColor, bgColor);
        throw new TODO();
    }

    /** x,y aka col,row */
    public abstract int[] getCursorPos();


    private static char visible(char cc) {
        

        

        switch (cc) {
            case ' ':
                return 0;
            case 9474:
                cc = '|';
                break;
            case 9472:
                cc = '-';
                break;
            case 9492:
                
            case 9496:
                cc = '*';
                break;
        }
        return cc;
    }



    /**
     * return true to paint a character's background. if so, then it should set the GL color
     */
    protected boolean setBackgroundColor(GL2 gl, TextCharacter ch, int col, int row) {
        if (ch != null) {
            bg = ch.bgColor;
            if (bg!=null)
                gl.glColor3f(bg[0], bg[1], bg[2]);
            return true;
        }

        return false;
    }


    public VectorTextGrid opacity(float v) {
        fgAlpha = v;
        return this;
    }


    





























































































































}