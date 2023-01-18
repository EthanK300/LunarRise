package renderer;

import java.nio.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.*;

public class Texture {
	private String filepath;
	private int texID;
	//the texID should be transient //TODO
	private int width, height;
	
	
	public Texture() {
		texID = -1;
		width = -1;
		height = -1;
	}
	
	public Texture(int width, int height) {
		this.filepath = "Generated";
		
		//generate textures on GPU
		texID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  texID);
		
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_LINEAR);
		GL20.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_LINEAR);
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width, height, 
				0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, 0);
	
	}
	
	public void init(String filepathN) {

		this.filepath = filepathN;
		
		//generate textures on GPU
		texID = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  texID);
		
		//set texture parameters
		//repeat image in both directions if the UV coordinate is bigger then image size
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
		//when stretching, pixelate
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		//when shrinking, pixelate
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		
		//load
		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer channels = BufferUtils.createIntBuffer(1);
		STBImage.stbi_set_flip_vertically_on_load(true);
		ByteBuffer image = STBImage.stbi_load(filepath, width, height, channels, 0);
		//GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 0); //test unpack alignment of the data
		//make sure the image loads ccorrectly if its jpg or png, if its jpg then formatting is differrent from png
		if(image != null) {
			this.width = width.get(0);
			this.height = height.get(0);
			if(channels.get(0) == 3) {
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width.get(0), height.get(0), 
					0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, image);
			}else if(channels.get(0) == 4){
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 
						0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
			}else {
				assert false : "Error: (Texture) Unknown number of channesl '" + channels.get(0) + "'";
			}
		}else {
			assert false: "error could not load image " + filepath;
		}
		//free memory from STBI
		STBImage.stbi_image_free(image);
	}
	
	public void bind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D,  texID);
	}
	
	public void unBind() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public int getId() {
		return texID;
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	@Override 
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		if(!(o instanceof Texture)) {
			return false;
		}
		Texture oTex = (Texture)o;
		return oTex.getWidth() == this.width && oTex.getHeight() == this.height 
				&& oTex.getId() == this.texID && oTex.getFilepath().equals(this.filepath);
		//override equals function
	}
	
}
