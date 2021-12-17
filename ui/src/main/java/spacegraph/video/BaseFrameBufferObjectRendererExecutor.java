package spacegraph.video;

/**
 * *   __ __|_  ___________________________________________________________________________  ___|__ __
 * *  
 * * 
 * *  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /
 * *   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/
 * *  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\
 * * /  \____\                       http:
 * * \  /   "' _________________________________________________________________________ `"   \  /
 * *  \/____.                                                                             .____\/
 * *
 * * Utility wrapper class wich encapsulates the framebuffer-object feature (also known as render-
 * * 2-texture) of OpenGL in a more convenient way. Handles all the boilerplate initialization,
 * * runtime and cleanup code needed to use the FBO. The rendering calls must be provided as
 * * an implementation of the BaseFrameBufferObjectRendererInterface.
 * *
 **/

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import static com.jogamp.opengl.GL2.*;

class BaseFrameBufferObjectRendererExecutor {

    private int mFrameBufferObjectID;
    private int mColorTextureID;
    private int mDepthTextureID;
    private int mTextureWidth;
    private int mTextureHeight;
    private final BaseFrameBufferObjectRendererInterface func;

    BaseFrameBufferObjectRendererExecutor(int inTextureWidth, int inTextureHeight, BaseFrameBufferObjectRendererInterface func) {
        this.func = func;
        resize(inTextureWidth, inTextureHeight);

    }

    private void resize(int inTextureWidth, int inTextureHeight) {
        mTextureWidth = inTextureWidth;
        mTextureHeight = inTextureHeight;
    }

    public int getColorTextureID() {
        return mColorTextureID;
    }

    public int getDepthTextureID() {
        return mDepthTextureID;
    }

    public int getWidth() {
        return mTextureWidth;
    }

    public int getHeight() {
        return mTextureHeight;
    }

    public void start(GL2 inGL) {
        
        
        int[] result = new int[1];
        inGL.glGenFramebuffers(1, result, 0);
        mFrameBufferObjectID = result[0];
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        
        inGL.glGenTextures(1, result, 0);
        mColorTextureID = result[0];
        inGL.glBindTexture(GL_TEXTURE_2D, mColorTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, mTextureWidth, mTextureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
        
        inGL.glGenTextures(1, result, 0);
        mDepthTextureID = result[0];
        inGL.glBindTexture(GL_TEXTURE_2D, mDepthTextureID);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        inGL.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        inGL.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, mTextureWidth, mTextureHeight, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
        
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, mColorTextureID, 0);
        inGL.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, mDepthTextureID, 0);
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        
        checkFrameBufferObjectCompleteness(inGL);
        if (func != null) {
            
            func.init_FBORenderer(inGL);
        } else {
            
        }
    }

    public void renderToFrameBuffer(int inFrameNumber, GL2 inGL) {
        inGL.glPushAttrib(GL_TRANSFORM_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
        
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, mFrameBufferObjectID);
        inGL.glPushAttrib(GL_VIEWPORT_BIT);
        inGL.glViewport(0, 0, mTextureWidth, mTextureHeight);
        if (func != null) {
            func.mainLoop_FBORenderer(inFrameNumber, inGL);
        } else {
            
        }
        inGL.glPopAttrib();
        
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        inGL.glPopAttrib();
    }

    public void prepareForColouredRendering(GL2 inGL, int inTextureUnitID) {
        inGL.glPushAttrib(GL_TEXTURE_BIT);
        inGL.glActiveTexture(inTextureUnitID);
        inGL.glBindTexture(GL_TEXTURE_2D, mColorTextureID);
        
        int textureTarget = GL_TEXTURE_2D;
        inGL.glEnable(textureTarget);
        inGL.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
        inGL.glTexParameteri(textureTarget, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        inGL.glTexParameteri(textureTarget, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        inGL.glTexParameteri(textureTarget, GL_TEXTURE_WRAP_S, GL_REPEAT);
        inGL.glTexParameteri(textureTarget, GL_TEXTURE_WRAP_T, GL_REPEAT);
    }

    public static void stopColouredRendering(GL2 inGL) {
        inGL.glBindTexture(GL_TEXTURE_2D, 0);
        
        inGL.glPopAttrib();
    }

















































    public void stop(GL2 inGL) {
        inGL.glDeleteFramebuffers(1, Buffers.newDirectIntBuffer(mFrameBufferObjectID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mColorTextureID));
        inGL.glDeleteTextures(1, Buffers.newDirectIntBuffer(mDepthTextureID));
        if (func != null) {
            func.cleanup_FBORenderer(inGL);
        } else {
            
        }
    }

    private static void checkFrameBufferObjectCompleteness(GL inGL) {
        
        int tError = inGL.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch (tError) {
            case GL_FRAMEBUFFER_COMPLETE:
            case GL_FRAMEBUFFER_UNSUPPORTED:
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:

                break;
            default:
                
        }
    }

    /**
     **   __ __|_  ___________________________________________________________________________  ___|__ __
     **  
     ** 
     **  \    \  / /  __|  |     |   __|  _  |     |  _  | | |  __|  |     |   __|  |      /\ \  /    /  
     **   \____\/_/  |  |  |  |  |  |  |     | | | |   __| | | |  |  |  |  |  |  |  |__   "  \_\/____/   
     **  /\    \     |_____|_____|_____|__|__|_|_|_|__|    | | |_____|_____|_____|_____|  _  /    /\     
     ** /  \____\                       http:
     ** \  /   "' _________________________________________________________________________ `"   \  /    
     **  \/____.                                                                             .____\/     
     **
     ** Interface to be implemented by all routines wich need to directly render to a texture using
     ** the framebuffer object feature of OpenGL. Provides method prototypes for initialization, 
     ** runtime and end/cleanup of such a routine.
     **
     **/


    interface BaseFrameBufferObjectRendererInterface {

        void init_FBORenderer(GL2 inGL);

        void mainLoop_FBORenderer(int inFrameNumber, GL2 inGL);

        void cleanup_FBORenderer(GL2 inGL);

    }

}