package main.engine;
import static org.lwjgl.glfw.GLFW.*;

import java.util.Arrays;
public class keyListener {
	private static keyListener instance;
	private boolean keyPressed[] = new boolean[350];
	private boolean keyBeginPress[] = new boolean[350];
	
	private keyListener() {
		
	}
	
	public static void endFrame() {
		Arrays.fill(get().keyBeginPress, false);
	}
	
	public static keyListener get() {
		if(keyListener.instance == null){
			keyListener.instance = new keyListener();
		}
		return keyListener.instance;
	}
	public static void keyCallback(long window, int key, int scancode, int action, int mods) {
		if(action == GLFW_PRESS) {
			get().keyPressed[key] = true;
			get().keyBeginPress[key] = true;
		}else if(action == GLFW_RELEASE) {
			get().keyPressed[key] = false;
			get().keyBeginPress[key] = false;
		}
	}
	public static boolean isKeyPressed(int keyCode) {
		return get().keyPressed[keyCode];
	}
	
	public static boolean keyBeginPress(int keyCode) {
		return get().keyBeginPress[keyCode];
	}
}
