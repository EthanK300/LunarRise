package renderer;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;

import org.joml.Vector2i;

import static org.lwjgl.opengl.GL30.*;



import org.lwjgl.opengl.GL20;

public class PickingTexture {
	private int pickingTextureId;
	private int fbo;
	private int depthTexture;
	
	public PickingTexture(int width, int height) {
		if(!init(width, height)) {
			assert false: "Error initializing picking texture";
		}
	}
	
	public boolean init(int width, int height) {
		fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        
        
        pickingTextureId = glGenTextures();
        // Create the texture to render the data to, and attach it to our framebuffer
        GL20.glBindTexture(GL_TEXTURE_2D, pickingTextureId);
        GL20.glTexParameteri(GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_S, GL20.GL_REPEAT);
        GL20.glTexParameteri(GL_TEXTURE_2D, GL20.GL_TEXTURE_WRAP_T, GL20.GL_REPEAT);
        GL20.glTexParameteri(GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        GL20.glTexParameteri(GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
        GL20.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL20.GL_RGB, GL20.GL_FLOAT, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                this.pickingTextureId, 0);
        
        //create texture for depth(3d if neccesary)
        glEnable(GL_TEXTURE_2D);
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        GL20.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
        
        //disable reading
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);
        
        
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            assert false : "Error: Framebuffer is not complete";
            return false;
        }
        //unbind texture and framebuffer
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        return true;
	}
	
	public void enableWriting() {
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
	}
	
	public void disableWriting() {
		glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
	}
	
	public int readPixel(int x, int y) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		
		float pixels[] = new float[3];
		glReadPixels(x, y, 1, 1, GL_RGB, GL_FLOAT, pixels);
		
		return (int)(pixels[0]) - 1;
		
	}
	
	public float[] readPixels(Vector2i start, Vector2i end) {
		glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		
		Vector2i size = new Vector2i(end).sub(start).absolute();
		int numPixels = size.x * size.y;
		float pixels[] = new float[3 * numPixels];
		glReadPixels(start.x, start.y, size.x, size.y, GL_RGB, GL_FLOAT, pixels);
		
		for(int i=0; i < pixels.length; i++) {
			pixels[i] -= 1;
		}
		return pixels;
		
	}
	
}
