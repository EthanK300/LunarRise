package renderer;

import main.components.SpriteRenderer;
import main.engine.GameObject;
import main.engine.Window;

import main.items.Item;
import main.util.Time;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import main.util.AssetPool;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch>{
	//Vertex
	//======
	//POS				Color						tex coords			tex id
	//float,float		float,float,float,float		float , float		float
	private final int POS_SIZE = 2;
	private static int POS_SIZE_STATIC = 2;
	private final int COLOR_SIZE = 4;
	private static int COLOR_SIZE_STATIC = 4;
	private final int TEX_COORDS_SIZE = 2;
	private static int TEX_COORDS_SIZE_STATIC = 2;
	private final int TEX_ID_SIZE = 1;
	private static int TEX_ID_SIZE_STATIC = 1;
	private final int ENTITY_ID_SIZE = 1;
	private static int ENTITY_ID_SIZE_STATIC = 1;
	
	private final int POS_OFFSET = 0;
	private static final int POS_OFFSET_STATIC = 0;
	private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
	private static final int COLOR_OFFSET_STATIC = POS_OFFSET_STATIC + POS_SIZE_STATIC * Float.BYTES;
	private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
	private final static int TEX_COORDS_OFFSET_STATIC = COLOR_OFFSET_STATIC + COLOR_SIZE_STATIC * Float.BYTES;
	private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
	private final static int TEX_ID_OFFSET_STATIC = TEX_COORDS_OFFSET_STATIC + TEX_COORDS_SIZE_STATIC * Float.BYTES;
	private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;
	private final static int ENTITY_ID_OFFSET_STATIC = TEX_ID_OFFSET_STATIC + TEX_ID_SIZE_STATIC * Float.BYTES;
	private final int VERTEX_SIZE = 10;
	private static final int VERTEX_SIZE_STATIC = 10;
	private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;
	private static final int VERTEX_SIZE_BYTES_STATIC = VERTEX_SIZE_STATIC * Float.BYTES;
	
	private SpriteRenderer[] sprites;
	private int numSprites;
	private boolean hasRoom;
	private float[] vertices;
	private static float[] vertices2;
	private int[] texSlots = {0,1,2,3,4,5,6,7};
	
	private List<Texture> textures;
	private int vaoID, vboID;
	private int maxBatchSize;
	private static int maxBatchSizeStatic = 1000;
	private int zIndex;
	private static Texture backDropScreen;
	private static int vaoID2, vboID2, eboID2;
	
	private Renderer renderer;

	private static float[] vertexArray = {
			// position               // color                  // UV Coordinates
			100f,   0f, 0.0f,       1.0f, 0.0f, 0.0f, 1.0f,     1, 1, // Bottom right 0
			0f, 100f, 0.0f,       0.0f, 1.0f, 0.0f, 1.0f,     0, 0, // Top left     1
			100f, 100f, 0.0f ,      1.0f, 0.0f, 1.0f, 1.0f,     1, 0, // Top right    2
			0f,   0f, 0.0f,       1.0f, 1.0f, 0.0f, 1.0f,     0, 1  // Bottom left  3
	};

	private static int[] elementArray = {
			/*
                    x        x
                    x        x
             */
			2, 1, 0, // Top right triangle
			0, 1, 3 // bottom left triangle
	};
	
	public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
		this.zIndex = zIndex;
		this.sprites = new SpriteRenderer[maxBatchSize];
		this.maxBatchSize = maxBatchSize;
		this.renderer = renderer;
		
		//4 vertices "quads"
		
		vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];
		vertices2 = vertices;
		this.numSprites = 0;
		this.hasRoom = true;
		this.textures = new ArrayList<>();
		
	}	
	
	public void start() {
		//Generate and bind vertex array object
		vaoID = glGenVertexArrays();

		glBindVertexArray(vaoID);
		
		//allocate necessary space for vertices
		vboID = glGenBuffers();

		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);
		
		//create and upload indices buffer
		int eboID = glGenBuffers();
		int[] indices = generateIndices();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
		
		//enable buffer attribute pointers
		glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_OFFSET);
		glEnableVertexAttribArray(0);
		
		glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, COLOR_OFFSET);
		glEnableVertexAttribArray(1);
		
		glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
		glEnableVertexAttribArray(2);
		
		glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
		glEnableVertexAttribArray(3);
		
		glVertexAttribPointer(4, ENTITY_ID_SIZE, GL_FLOAT, false, VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
		glEnableVertexAttribArray(4);
	}
	
	public void addSprite(SpriteRenderer spr) {
		//get index and add sprite object(sprite renderer)
		int index = this.numSprites;
		// [0,1,2,3,4,5]
		this.sprites[index] = spr;
		this.numSprites++;
		
		if(spr.getTexture() != null) {
			if(!textures.contains(spr.getTexture())) {
				textures.add(spr.getTexture());
			}
		}
		
		//add properties to local vertices array
		loadVertexProperties(index);
		
		if(numSprites >= this.maxBatchSize) {
			this.hasRoom = false;
		}
		
	}
	
	public void render() {
		boolean rebufferData = false;
		for(int i = 0; i < numSprites; i++) {
			SpriteRenderer spr = sprites[i];
			if(spr.isDirty()) {
				if(!hasTexture(spr.getTexture())) {
					this.renderer.destroyGameObject(spr.gameObject);
					this.renderer.add(spr.gameObject);
				}else {
					loadVertexProperties(i);
					spr.setClean();
					rebufferData = true;
				}
				
			}
			
			if(spr.gameObject.transform.zIndex != this.zIndex) {
				destroyIfExists(spr.gameObject);
				renderer.add(spr.gameObject);
				i--;
			}
		}
		if(rebufferData == true) {
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
		}
		//use shader
		Shader shader = Renderer.getBoundShader();
		shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());
		
		for(int i = 0; i < textures.size(); i++) {
			glActiveTexture(GL_TEXTURE0 + i + 1);
			textures.get(i).bind();
		}
		shader.uploadIntArray("uTextures", texSlots);
		
		glBindVertexArray(vaoID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		for(int i = 0; i < textures.size(); i++) {
			textures.get(i).unBind();
		}
		shader.detach();
		
	}
	public static void renderBackDrop(){
		Shader shader = AssetPool.getShader("assets/shaders/backDrop.glsl");
		backDropScreen = AssetPool.getTexture("assets/images/backDrop.png");

		// ============================================================
		// Generate VAO, VBO, and EBO buffer objects, and send to GPU
		// ============================================================
		vaoID2 = glGenVertexArrays();
		glBindVertexArray(vaoID2);

		// Create a float buffer of vertices
		FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
		vertexBuffer.put(vertexArray).flip();

		// Create VBO upload the vertex buffer
		vboID2 = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID2);
		glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

		// Create the indices and upload
		IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
		elementBuffer.put(elementArray).flip();

		eboID2 = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID2);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

		// Add the vertex attribute pointers
		int positionsSize = 3;
		int colorSize = 4;
		int uvSize = 2;
		int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;
		glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * Float.BYTES);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize + colorSize) * Float.BYTES);
		glEnableVertexAttribArray(2);

		//use

		shader.use();

		// Upload texture to shader
		shader.uploadTexture("TEX_SAMPLER", 0);
		glActiveTexture(GL_TEXTURE0);
		backDropScreen.bind();

		shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());
		shader.uploadFloat("uTime", Time.getTime());
		// Bind the VAO that we're using
		glBindVertexArray(vaoID2);

		// Enable the vertex attribute pointers
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

		// Unbind everything
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);

		glBindVertexArray(0);

		shader.detach();
	}
	
	public boolean destroyIfExists(GameObject go) {
		SpriteRenderer sprite = go.getComponent(SpriteRenderer.class);
		for(int i=0; i < numSprites; i++) {
			if(sprites[i] == sprite) {
				for(int j = i; j < numSprites - 1; j++) {
					sprites[j] = sprites[j + 1];
					sprites[j].setDirty();
					
				}
				numSprites--;
				return true;
			}
		}
		return false;
	}
	
	private void loadVertexProperties(int index) {
		SpriteRenderer sprite = this.sprites[index];
		
		//find offest within array (4 vertices per sprite)
		int offset = index * 4 * VERTEX_SIZE;
		//type: float, float,	float, float, float, float
		
		Vector4f color = sprite.getColor();
		Vector2f[] texCoords = sprite.getTexCoords();
		
		int texID = 0;
		// mock list: [0, tex, tex, tex, tex, tex, ....]
		if(sprite.getTexture() != null) {
			for(int i = 0; i < textures.size(); i++) {
				 if(textures.get(i).equals(sprite.getTexture())) {
					 texID = i + 1;
					 break;
				 }
			}
		}
		
		boolean isRotated = sprite.gameObject.transform.rotation != 0.0f;
		Matrix4f transformMatrix = new Matrix4f().identity();
		if(isRotated) {
			transformMatrix.translate(sprite.gameObject.transform.position.x, 	
					sprite.gameObject.transform.position.y, 0f);
				transformMatrix.rotate((float)Math.toRadians(sprite.gameObject.transform.rotation), 0, 0, 1);
				transformMatrix.scale(sprite.gameObject.transform.scale.x, sprite.gameObject.transform.scale.y ,1);
				
		}
		
		//add vertices with the appropriate properties
		float xAdd = 0.5f;
		float yAdd = 0.5f;
		for(int i = 0; i < 4; i++) {
			if(i == 1) {
				yAdd = -0.5f;
			}else if(i == 2) {
				xAdd = -0.5f;
			}else if(i == 3) {
				yAdd = 0.5f;
			}
			
			Vector4f currentPos = new Vector4f(sprite.gameObject.transform.position.x + (xAdd * sprite.gameObject.transform.scale.x), sprite.gameObject.transform.position.y + (yAdd * sprite.gameObject.transform.scale.y), 0 ,1);
			
			if(isRotated) {
				currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
			}
			
			//load position
			vertices[offset] = currentPos.x;
			vertices[offset + 1] = currentPos.y;
			
			//load color
			vertices[offset + 2] = color.x;
			vertices[offset + 3] = color.y;
			vertices[offset + 4] = color.z;
			vertices[offset + 5] = color.w;
			
			//load texture coordinates
			vertices[offset + 6] = texCoords[i].x;
			vertices[offset + 7] = texCoords[i].y;
			//load texture ID
			vertices[offset + 8] = texID;
			
			//load entity id
			vertices[offset + 9] = sprite.gameObject.getUid() + 1;
			
			
			offset += VERTEX_SIZE;
			
		}
	}
	
	private int[] generateIndices() {
		// 6 indices per quad
		int[] elements = new int[6 * maxBatchSize];
		for(int i = 0; i < maxBatchSize; i++) {
			loadElementIndices(elements, i);
		}
		return elements;
	}
	
	private void loadElementIndices(int[] elements, int index) {
		int offsetArrayIndex = 6 * index;
		int offset = 4 * index;
		
		//3,2,0,0,2,1		7,6,4,4,6,5
		//triangle 1
		elements[offsetArrayIndex] = offset + 3;
		elements[offsetArrayIndex + 1] = offset + 2;
		elements[offsetArrayIndex + 2] = offset + 0;
		
		//triangle 2
		elements[offsetArrayIndex + 3] = offset + 0;
		elements[offsetArrayIndex + 4] = offset + 2;
		elements[offsetArrayIndex + 5] = offset + 1;
		
	}
	
	public boolean hasRoom() {
		return this.hasRoom;
	}
	
	public boolean hasTextureRoom() {
		return this.textures.size() < 7;
	}
	
	public boolean hasTexture(Texture tex) {
		return this.textures.contains(tex);
	}
	
	public int zIndex() {
		return this.zIndex;
	}

	@Override
	public int compareTo(RenderBatch o) {
		return Integer.compare(this.zIndex, o.zIndex);
	}

}
