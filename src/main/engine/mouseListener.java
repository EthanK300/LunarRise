package main.engine;
import static org.lwjgl.glfw.GLFW.*;

import java.util.Arrays;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class mouseListener {
	private static mouseListener instance;
	private double scrollX;
	private double scrollY;
	private double xPos;
	private double yPos;
	
	private int mouseButtonDown = 0;
	
	private double worldX, worldY, lastWorldX, lastWorldY, lastX, lastY;
	
	private boolean mouseButtonPressed[] = new boolean[9];
	private boolean isDragging;
	
	private Vector2f gameViewportPos = new Vector2f();
	private Vector2f gameViewportSize = new Vector2f();
	
	private mouseListener() {
		this.scrollX = 0.0;
		this.scrollY = 0.0;
		this.xPos = 0.0;
		this.yPos = 0.0;
		this.lastX = 0.0;
		this.lastY = 0.0;
	}
	
	public static void endFrame() {
		get().scrollY = 0.0f;
		get().scrollX = 0.0f;
	}
	
	public static void clear() {
		get().scrollX = 0.0f;
		get().scrollY = 0.0f;
		get().xPos = 0.0;
		get().yPos = 0.0;
		get().lastX = 0.0;
		get().lastY = 0.0;
		get().mouseButtonDown = 0;
		get().isDragging = false;
		Arrays.fill(get().mouseButtonPressed, false);
	}
	
	//creates the new mouse listener instance if there isn't one already
	public static mouseListener get() {
		if(mouseListener.instance == null) {
			mouseListener.instance = new mouseListener();
		}
		return mouseListener.instance;
	}
	//method for mouse callbacks
	public static void mousePosCallback(long window, double xpos, double ypos) {
		
		if(!Window.getImGuiLayer().getGameViewWindow().getWantCaptureMouse()) {
			clear();
		}
		
		if(get().mouseButtonDown > 0) {
			get().isDragging = true;
		}
		get().lastX = get().xPos;
		get().lastY = get().yPos;
		get().lastWorldX = get().worldX;
		get().lastWorldY = get().worldY;
		get().xPos = xpos;
		get().yPos = ypos;
	}
	//reading for mouse button presses
	public static void mouseButtonCallback(long window, int button, int action, int mods ) {
		if(action == GLFW_PRESS) {
			get().mouseButtonDown++;
			
			if(button < get().mouseButtonPressed.length) {
				get().mouseButtonPressed[button] = true;		
			}
		}else if(action == GLFW_RELEASE) {
			
			get().mouseButtonDown--;
			
			if(button < get().mouseButtonPressed.length) {
				get().mouseButtonPressed[button] = false;
				get().isDragging = false;	
			}
		}
	}
	//listening for scroll wheel data
	public static void mouseScrollCallback(long window, double xOffSet, double yOffSet) {
		get().scrollX = xOffSet;
		get().scrollY = yOffSet;
	}
	
	//returns x and y positions, changes in positions, and scroll wheel
	public static float getX() {
		return (float)get().xPos;
	}
	
	public static float getY() {
		return (float)get().yPos;
	}
	/**
	public static float getDx() {
		return (float)(get().lastX - get().xPos);
	}
	
	public static float getDy() {
		return (float)(get().lastY - get().yPos);
	}
	**/
	public static float getWorldDx() {
		return (float)(get().lastWorldX - get().worldX);
	}
	
	public static float getWorldDy() {
		return (float)(get().lastWorldY - get().worldY);
	}
	
	public static float getScrollX() {
		return(float)get().scrollX;
	}
	
	public static float getScrollY() {
		return(float)get().scrollY;
	}
	
	//returns if mouse is dragging
	public static boolean isDragging() {
		return get().isDragging;
	}
	
	public static boolean mouseButtonDown(int button) {
		if(button < get().mouseButtonPressed.length) {
			return get().mouseButtonPressed[button];
		}else {
			return false;
		}
	}
	
	public static Vector2f screenToWorld(Vector2f screenCoords) {
		Vector2f normalizedScreenCoords = new Vector2f(
				screenCoords.x / Window.getWidth(),
				screenCoords.y / Window.getHeight()
				);
		normalizedScreenCoords.mul(2.0f).sub(new Vector2f(1.0f, 1.0f));
		
		Camera camera =  Window.getScene().camera();
		Vector4f tmp = new Vector4f(normalizedScreenCoords.x, normalizedScreenCoords.y, 0, 1);
		Matrix4f inverseView = new Matrix4f(camera.getInverseView());
		Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjection());
		tmp.mul(inverseView.mul(inverseProjection));
		return new Vector2f(tmp.x, tmp.y);
		//SC = P * V * M
		//WC = SC * iV * iP
	}
	public static Vector2f worldToScreen(Vector2f worldCoords) {
		Camera camera = Window.getScene().camera();
		Vector4f ndcSpacePos = new Vector4f(worldCoords.x, worldCoords.y, 0, 1);
		Matrix4f view = new Matrix4f(camera.getViewMatrix());	
		Matrix4f projection = new Matrix4f(camera.getProjectionMatrix());
		ndcSpacePos.mul(projection.mul(view));
		Vector2f windowSpace = new Vector2f(ndcSpacePos.x, ndcSpacePos.y).mul(1.0f / ndcSpacePos.w);
		windowSpace.add(new Vector2f(1.0f, 1.0f).mul(0.5f));
		windowSpace.mul(new Vector2f(Window.getWidth(), Window.getHeight()));
		return windowSpace;
	}
	
	public static float getScreenX() {
		return getScreen().x; //returns screen x
	}
	
	public static float getScreenY() {
		return getScreen().y; //returns screen y
	}
	
	public static Vector2f getScreen() {
		float currentX = getX() - get().gameViewportPos.x;
		currentX = (currentX / get().gameViewportSize.x) * 1920.0f;
		
		float currentY = (getY() - get().gameViewportPos.y);
		currentY = (1.0f - (currentY / get().gameViewportSize.y)) * 1080.0f;
		
		return new Vector2f(currentX, currentY);
	}

	public static float getWorldX() {
		return getWorld().x; //returns current mouseposition in worldcoords
	}
	
	public static float getWorldY() {
		return getWorld().y; //returns current mouseposition in worldcoords
	}
	
	public static Vector2f getWorld() {
		float currentX = getX() - get().gameViewportPos.x;
		currentX = (2.0f * (currentX / get().gameViewportSize.x)) - 1.0f;
		
		float currentY = (getY() - get().gameViewportPos.y);
		currentY = (2.0f * (1.0f - (currentY / get().gameViewportSize.y))) - 1;
		
		Camera camera = Window.getScene().camera();
		Vector4f tmp = new Vector4f(currentX, currentY, 0, 1);
		Matrix4f inverseView = new Matrix4f(camera.getInverseView());
		Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjection());
		tmp.mul(inverseView.mul(inverseProjection));
		return new Vector2f(tmp.x, tmp.y);
	}
	
	public static void setGameViewportPos(Vector2f gameViewportPos) {
		get().gameViewportPos.set(gameViewportPos);
	}
	
	public static void setGameViewportSize(Vector2f gameViewportSize) {
		get().gameViewportSize.set(gameViewportSize);
	}
	
}
