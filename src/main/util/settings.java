package main.util;

import org.joml.Vector2f;
import org.joml.Vector4f;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;

public class settings {
	//editor settings
	public static float GRID_WIDTH = 0.25f;
	public static float GRID_HEIGHT = 0.25f;

	//player settings
	public static float walkSpeed = 1.0f;
	public static float jumpForce = 1.0f;
	public static float jumpImpulse = 3.0f;
	public static float slowDownForce = 0.05f;
	public static float megaJumpBoostFactor = 1.0f;
	public static Vector2f terminalVelocitry = new Vector2f(2.1f, 3.1f);
	public static int inventorySize = 23;
	public static int acquireItem = GLFW_KEY_F;
	public static Vector2f acquireItemRange = new Vector2f(1.0f,1.0f);

	//camera settings
	public static Vector4f cameraThreshold = new Vector4f(3.0f, 2.0f, 2.0f, 1.0f);

}