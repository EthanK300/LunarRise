package renderer;

import main.components.SpriteRenderer;
import main.engine.GameObject;
import main.engine.Window;

import main.items.Item;
import main.player.PlayerController;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import main.util.AssetPool;

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
	private static GameObject player = Window.getScene().getGameObjectWith(PlayerController.class);
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
	private static Texture backDropScreen = AssetPool.getTexture("assets/images/backDrop.png");
	private SpriteRenderer[] sprites;
	private int numSprites;
	private boolean hasRoom;
	private float[] vertices;
	private int[] texSlots = {0,1,2,3,4,5,6,7};
	private static int[] texSlotsStatic = {0,1,2,3,4,5,6,7};
	private List<Texture> textures;
	private int vaoID, vboID;
	private static int backVAO, backVBO, backEBO;
	private int maxBatchSize;
	private int zIndex;
	private Renderer renderer;
	private static float[] backVertices = {
			//position		//color				//tex coords	//tex id and entity id
			3, 1.5f,		1f, 1f, 1f, 1f, 	1, 1,			1,	1,
			3, 1.25f,		1f, 1f, 1f,	1f,		1, 0,			1,	1,
			2, 1.25f,		1f, 1f, 1f,	1f, 	0, 0,			1,	1,
			2, 1.5f,		1f, 1f, 1f,	1f,		0, 1,			1,	1,
	};
	
	public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
		this.zIndex = zIndex;
		this.sprites = new SpriteRenderer[maxBatchSize];
		this.maxBatchSize = maxBatchSize;
		this.renderer = renderer;
		
		//4 vertices "quads"
		
		vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];
		
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

	public static void backInit(){
		//Generate and bind vertex array object
		backVAO = glGenVertexArrays();
		glBindVertexArray(backVAO);

		//allocate necessary space for vertices
		backVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, backVBO);
		glBufferData(GL_ARRAY_BUFFER, backVertices.length * Float.BYTES, GL_STATIC_DRAW);

		//create and upload indices buffer
		int backEBO = glGenBuffers();
		int[] backIndices = {3, 2, 0, 0, 2, 1};
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, backEBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, backIndices, GL_DYNAMIC_DRAW);

		//enable buffer attribute pointers
		glVertexAttribPointer(0, POS_SIZE_STATIC, GL_FLOAT, false, VERTEX_SIZE_BYTES_STATIC, POS_OFFSET_STATIC);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, COLOR_SIZE_STATIC, GL_FLOAT, false, VERTEX_SIZE_BYTES_STATIC, COLOR_OFFSET_STATIC);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, TEX_COORDS_SIZE_STATIC, GL_FLOAT, false, VERTEX_SIZE_BYTES_STATIC, TEX_COORDS_OFFSET_STATIC);
		glEnableVertexAttribArray(2);

		glVertexAttribPointer(3, TEX_ID_SIZE_STATIC, GL_FLOAT, false, VERTEX_SIZE_BYTES_STATIC, TEX_ID_OFFSET_STATIC);
		glEnableVertexAttribArray(3);

		glVertexAttribPointer(4, ENTITY_ID_SIZE_STATIC, GL_FLOAT, false, VERTEX_SIZE_BYTES_STATIC, ENTITY_ID_OFFSET_STATIC);
		glEnableVertexAttribArray(4);
	}
	public static void renderBackDrop(){

		boolean init = false;
		double posx = 0;
		double posy = 0;



		if(!init) {
			backInit();
		}

		try{
			posx = player.transform.position.x;
			posy = player.transform.position.y;
		}catch(NullPointerException n){
			assert false: "Error: player not found";
			System.exit(0);
		}



		glBindBuffer(GL_ARRAY_BUFFER, backVBO);
		glBufferSubData(GL_ARRAY_BUFFER, 0, backVertices);

		Shader shader = Renderer.getBoundShader();
		shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());

		glActiveTexture(GL_TEXTURE1);
		backDropScreen.bind();

		shader.uploadIntArray("uTextures", texSlotsStatic);

		glBindVertexArray(backVAO);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);

		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);

		backDropScreen.unBind();
		shader.detach();

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
