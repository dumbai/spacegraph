package spacegraph.video;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES3;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import com.jogamp.opengl.util.glsl.ShaderState;
import jcog.tree.rtree.rect.RectF;
import spacegraph.SpaceGraph;
import spacegraph.space2d.ReSurface;
import spacegraph.space2d.container.PaintSurface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;

/** TODO */
public class GLSL extends PaintSurface {

    private ShaderState st;

    public static void main(String[] args) {
        RectF r = RectF.XYXY(1, 1, 500, 500);
        SpaceGraph.window(new GLSL().pos(r), 800, 600);
    }

    private static final boolean updateUniformVars = true;
    private int vertexShaderProgram;
    private int fragmentShaderProgram;
    private int shaderprogram;

    private static final float x = -2;
    private static final float y = -2;
    private static final float height = 4;
    private static final float width = 4;
    private static final int iterations = 1;


    private boolean init = false;


    private String[] loadShaderSrc(String name) {
        String sb = "";
        try {
            InputStream is = getClass().getResourceAsStream(name);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            sb = br.lines().map(line -> line + '\n').collect(Collectors.joining());
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Shader is " + sb);
        return new String[]{sb};
    }


    private void initShaders(GL2 gl) {
        if (!gl.hasGLSL()) {
            System.err.println("No GLSL available, no rendering.");
            return;
        }

        st = new ShaderState();
        





        CharSequence fsrc = null;
        try {
            fsrc = new StringBuilder(new String(GLSL.class.getClassLoader().getResourceAsStream(

                    "glsl/16seg.glsl"

            ).readAllBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }



        /*vp0, */
        ShaderCode fp0 = new ShaderCode(GL_FRAGMENT_SHADER, 1, new CharSequence[][]{{fsrc}});
        
        fp0.defaultShaderCustomization(gl, true, true);
        ShaderProgram sp0 = new ShaderProgram();
        
        sp0.add(gl, fp0, System.err);
        st.attachShaderProgram(gl, sp0, true);
        
        
        gl.glFinish(); 

    }




































    @Override
    public void paint(GL2 gl, ReSurface reSurface) {
        Draw.bounds(gl, this, this::doPaint);
    }

    private void doPaint(GL2 gl) {
        if (!gl.hasGLSL()) {
            return;
        }

        if (!init) {
            try {
                initShaders(gl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            init = true;
        }





        gl.glEnable(GL.GL_TEXTURE);

        gl.glColor3f(1.0f, 1.0f, 1.0f);
        Draw.rect(gl, -1, -1, 1, 1);

        st.useProgram(gl, true);



        gl.glBegin(GL2ES3.GL_QUADS);
        {
            gl.glTexCoord2f(0.0f, 800.0f);
            gl.glVertex3f(0.0f, 1.0f, 1.0f);  
            gl.glTexCoord2f(800.0f, 800.0f);
            gl.glVertex3f(1.0f, 1.0f, 1.0f);   
            gl.glTexCoord2f(800.0f, 0.0f);
            gl.glVertex3f(1.0f, 0.0f, 1.0f);  
            gl.glTexCoord2f(0.0f, 0.0f);
            gl.glVertex3f(0.0f, 0.0f, 1.0f); 
        }
        gl.glEnd();
        











        


        st.useProgram(gl, false);

    }





















}