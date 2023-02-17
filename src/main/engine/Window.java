package main.engine;


import renderer.*;
import scenes.LevelSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import scenes.levelEditorSceneInitializer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Dimension;
import java.awt.Toolkit;

import static org.lwjgl.glfw.Callbacks.*;

import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.*;
import static org.lwjgl.openal.ALC10.*;

import main.util.AssetPool;
import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import physics2d.Physics2D;


public class Window implements Observer{
	private int width;
	private int height;
	private String title;
	private long glfwWindow;
	private static Window window = null;
	private int cycle = 0;
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	public Vector2f screenSizePixels;
	private long audioContext;
	private long audioDevice;

	private static Scene currentScene;
	private boolean runtimeActive = false;
	private ImGuiLayer imguiLayer;
	
	private Framebuffer framebuffer;
	
	private PickingTexture pickingTexture;
	
	private Window() {
		//width and height are monitor size in pixels
		this.width = (int)screenSize.getWidth();//1920
		this.height = (int)screenSize.getHeight();//1080
		screenSizePixels = new Vector2f((float)screenSize.getWidth(), (float)screenSize.getHeight());
		this.title = "Engine";
		//System.out.println(this.width + "," + this.height);
		EventSystem.addObserver(this);
		//-> this command gets monitor res glfwGetPrimaryMonitor();
		
	}
	
	public static void changeScene(SceneInitializer sceneInitializer) {

		if(currentScene != null) {
			currentScene.destroy();
		}
		
		getImGuiLayer().getPropertiesWindow().setActiveGameObject(null);
		currentScene = new Scene(sceneInitializer);
		currentScene.load();
		currentScene.init();
		currentScene.start();

	}
	
	public static Window get() {
		if(Window.window == null) {
			Window.window = new Window();
		}
		return Window.window;
	}
	
	public static Scene getScene() {
		return get().currentScene;
	}
	
	public void run() {
		System.out.println("Hello LWJGL + " + Version.getVersion() + "!"); //test window creation and get version of LWJGL
		
		init();
		loop();
		//destroy audio context after use
		alcDestroyContext(audioContext);
		alcCloseDevice(audioDevice);
		
		//Free the memory after it exits
		glfwFreeCallbacks(glfwWindow);
		glfwDestroyWindow(glfwWindow);
		//terminate GLFW
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		System.err.println("Closed.");
	}
	public void init() {
		//setup error sequence
		GLFWErrorCallback.createPrint(System.err).set();
		
		//start lwjgl and glfw
		if(!glfwInit()) {
			throw new IllegalStateException("cant initialize glfw");
		}
		//configure glfw
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		
		//TODO:Figure our why this can't be GLFW_TRUE -> window coords don't work correctly if it starts maximized...
		glfwWindowHint(GLFW_MAXIMIZED, GLFW_FALSE);
		
		//create the  window using the window hints above
		glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
		if(glfwWindow == NULL) {
			throw new IllegalStateException("Failed to create the glfw window");
		}
		
		glfwSetCursorPosCallback(glfwWindow, mouseListener::mousePosCallback);
		glfwSetMouseButtonCallback(glfwWindow, mouseListener::mouseButtonCallback);
		glfwSetScrollCallback(glfwWindow, mouseListener::mouseScrollCallback);
		glfwSetKeyCallback(glfwWindow, keyListener::keyCallback);
		
		glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
			Window.setWidth(newWidth);
			Window.setHeight(newHeight);
		});
		
		//configure open GL stuff
		glfwMakeContextCurrent(glfwWindow);
		//enable the v-sync settings
		glfwSwapInterval(1);
		//window should be alive so make it visible
		glfwShowWindow(glfwWindow);
		
		//setup audio context
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		audioDevice = alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		audioContext = alcCreateContext(audioDevice, attributes);
		alcMakeContextCurrent(audioContext);
		//creating al capabilities
		ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
		//checks to make sure audio library is supported
		if(!alCapabilities.OpenAL10) {
			assert false : "Audio library not supported";
		}
		
		
		
		//following line of code is necessary for all LWJGL window programs
		GL.createCapabilities();
		GL20.glEnable(GL_BLEND);
		GL20.glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		
		
		
		this.framebuffer = new Framebuffer(1920, 1080);
		this.pickingTexture = new PickingTexture(1920,1080);
		
		GL20.glViewport(0, 0, 1920, 1080);
		
		this.imguiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
		this.imguiLayer.initImGui();

		Window.changeScene(new levelEditorSceneInitializer());

	}

	public void loop() {
		float beginTime = (float)glfwGetTime();
		float endTime;
		float dt = -1.0f;
		
		Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
		Shader pickingShader = AssetPool.getShader("assets/shaders/pickingShader.glsl");
		Shader backDropShader = AssetPool.getShader("assets/shaders/backDrop.glsl");

		while(!glfwWindowShouldClose(glfwWindow)) {
			//poll events(get keyboard inputs, mouse inputs, etc)
			glfwPollEvents();



			//render to picking texture
			
			glDisable(GL_BLEND);
			pickingTexture.enableWriting();
			
			glViewport(0, 0, 1920, 1080);
			Vector4f clearColor = currentScene.camera().clearColor;
			glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			//render to backdrop
			/**
			if(true){
				Renderer.bindShader(backDropShader);
				currentScene.renderBackDrop();
			}
			 **/

			Renderer.bindShader(backDropShader);
			RenderBatch.renderBackDrop();

			//render main
			Renderer.bindShader(pickingShader);
			currentScene.render();
			

			
			pickingTexture.disableWriting();
			glEnable(GL_BLEND);
			//render game
			
			DebugDraw.beginFrame();
			
			this.framebuffer.bind();
			//glClearColor(1, 1, 1, 1);
			glClear(GL_COLOR_BUFFER_BIT);

			if(dt >= 0) {
				
				Renderer.bindShader(defaultShader);
				if(runtimeActive) {
					currentScene.update(dt);
				}else{
					currentScene.editorUpdate(dt);
				}
				currentScene.render();
				DebugDraw.draw();
			}
			
			this.framebuffer.unbind();
			
			this.imguiLayer.update(dt, currentScene);
			mouseListener.endFrame();
			keyListener.endFrame();
			glfwSwapBuffers(glfwWindow);
			endTime = (float)glfwGetTime();
			dt = endTime - beginTime;
			beginTime = endTime;

			//error handling
			cycle++;
			int err = glGetError();
			if(err != 0){
				System.out.println(err + "," + cycle);
			}
		}
		
	}
	
	public static int getWidth() {
		return 1920; //get().width;
	}
	
	public static int getHeight() {
		return 1080; //get().height;
	}
	
	public static void setWidth(int newWidth) {
		get().width = newWidth;
	}
	
	public static void setHeight(int newHeight) {
		get().height = newHeight;
	}
	
	public static Framebuffer getFramebuffer() {
		return get().framebuffer;
	}
	
	public static float getTargetAspectRatio() {
		return 16.0f / 9.0f;
	}
	
	public static ImGuiLayer getImGuiLayer() {
		return get().imguiLayer;
	}

	@Override
	public void onNotify(GameObject object, Event event) {
		
		switch(event.type) {
			case GameEngineStartPlay:
				this.runtimeActive = true;
				currentScene.save();
				Window.changeScene(new LevelSceneInitializer());
				break;
			case GameEngineStopPlay:
				this.runtimeActive = false;
				Window.changeScene(new levelEditorSceneInitializer());
				break;
			case LoadLevel:
				Window.changeScene(new levelEditorSceneInitializer());
				break;
			case SaveLevel:
				currentScene.save();
				break;
		}
	}
	
	public static Physics2D getPhysics() {
		return currentScene.getPhysics();
	}
	
}
