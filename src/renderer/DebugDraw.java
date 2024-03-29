package renderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.util.AssetPool;
import main.util.JMath;

import static org.lwjgl.opengl.GL20.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import static org.lwjgl.opengl.ARBVertexArrayObject.*;
import main.engine.Window;

public class DebugDraw {
	
	private static int MAX_LINES = 50000;//TODO: find out why this has to be 5000
	
	private static List<Line2D> lines = new ArrayList<>();
	// 6 floats per vertex
	private static float[] vertexArray = new float[MAX_LINES * 6 * 2];
	//TODO:could the above line in conjunction with lines.size() be the error with the random blue lines
	private static Shader shader = AssetPool.getShader("assets/shaders/debugLine2D.glsl");
	
	private static int vaoID;
	private static int vboID;
	
	private static boolean started = false;
	
	public static void start() {
		//generate vao
		vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		
		//create vbo
		vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);
		//enable the vertex array attributes
		
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);
		
		glVertexAttribPointer(1, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);
		
		glLineWidth(2.0f);
		
		
	}
	
	public static void beginFrame() {
		if(!started) {
			start();
			started = true;
		}
		//remove dead lines
		for (int i = 0; i < lines.size(); i++) {
			if(lines.get(i).beginFrame() < 0) {
				lines.remove(i);
				i--;
			}
		}
	}
	
	public static void draw() {
		if(lines.size() <= 0) {
			return;
		}
		int index = 0;
		for(Line2D line : lines) {
			for(int i = 0; i < 2; i++) {
				Vector2f position = i == 0 ? line.getFrom() : line.getTo();
				Vector3f color = line.getColor();
				
				//load position
				vertexArray[index] = position.x;
				vertexArray[index + 1] = position.y;
				vertexArray[index + 2] = -10.0f;
				
				//load color
				vertexArray[index + 3] = color.x;
				vertexArray[index + 4] = color.y;
				vertexArray[index + 5] = color.z;
				
				index +=6;
				
			}
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_DYNAMIC_DRAW);
		
		//use shader
		shader.use();
		shader.uploadMat4f("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4f("uView", Window.getScene().camera().getViewMatrix());
		
		//bind vao
		
		glBindVertexArray(vaoID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		
		//draw batch
		glDrawArrays(GL_LINES, 0, lines.size()  * 2);
		//System.out.println(lines.size());
		//TODO: figure out why the lines error is happening
		//disable location and unbind
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
		
		//unbind shader
		shader.detach();
		
	}
	//add lines
	public static void addLine2D(Vector2f from, Vector2f to) {
		//TODO: add constants for common colors (red, green, blue etc)
		addLine2D(from, to, new Vector3f(0, 1, 0), 1);
		
	}
	
	public static void addLine2D(Vector2f from, Vector2f to, Vector3f color) {
		//TODO: add constants for common colors (red, green, blue etc)
		addLine2D(from, to, color, 1);
		
	}
	
	public static void addLine2D(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
		if(lines.size() >= MAX_LINES) {
			return;
		}
		DebugDraw.lines.add(new Line2D(from, to, color, lifetime));
	}
	
	//add boxes
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation) {
		//TODO: add constants for common colors (red, green, blue etc)
		addBox2D(center, dimensions, rotation, new Vector3f(0, 1, 0), 1);
		
	}
	
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color) {
		//TODO: add constants for common colors (red, green, blue etc)
		addBox2D(center, dimensions, rotation, color, 1);
		
	}
	
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color, int lifetime) {
		
		Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).mul(0.5f));
		Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).mul(0.5f));
		Vector2f[] vertices = {
			new Vector2f(min.x, min.y), new Vector2f(min.x, max.y),
			new Vector2f(max.x, max.y), new Vector2f(max.x, min.y)
		};
		
		if(rotation != 0.0) {
			for(Vector2f vert : vertices) {
				JMath.rotate(vert, rotation, center);
			}
		}
		
		addLine2D(vertices[0], vertices[1], color, lifetime);
		addLine2D(vertices[0], vertices[3], color, lifetime);
		addLine2D(vertices[1], vertices[2], color, lifetime);
		addLine2D(vertices[2], vertices[3], color, lifetime);
	}
	//add circles
	public static void addCircle(Vector2f center, float radius, Vector3f color, int lifetime) {
		Vector2f[] points = new Vector2f[30];
		int increment = 360 / points.length;
		int currentAngle = 0;
		for(int i = 0; i < points.length; i++) {
			Vector2f tmp = new Vector2f(radius, 0);
			JMath.rotate(tmp,  currentAngle, new Vector2f());
			points[i] = new Vector2f(tmp).add(center);
			
			if(i > 0) {
				addLine2D(points[i - 1], points [i], color, lifetime);
			}
			
			currentAngle += increment;
		}
		
		addLine2D(points[points.length - 1], points[0], color, lifetime);
	}
	
	public static void addCircle(Vector2f center, float radius) {
		//TODO: add constants for common colors (red, green, blue etc)
		addCircle(center, radius, new Vector3f(0, 1, 0), 1);
		
	}
	
	public static void addCircle(Vector2f center, float radius, Vector3f color) {
		//TODO: add constants for common colors (red, green, blue etc)
		addCircle(center, radius, color, 1);
		
	}
	
}
