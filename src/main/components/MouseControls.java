package main.components;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.mouseListener;
import main.engine.keyListener;
import main.util.settings;
import renderer.DebugDraw;
import renderer.PickingTexture;
import scenes.Scene;

import static org.lwjgl.glfw.GLFW.*;

import java.util.HashSet;
import java.util.Set;

import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector4f;

import editor.PropertiesWindow;

public class MouseControls extends Component{
	
	GameObject holdingObject = null;
	private float debounceTime = 0.2f;
	private float debounce = debounceTime;
	private boolean boxSelectSet = false;
	private Vector2f boxSelectStart = new Vector2f();
	private Vector2f boxSelectEnd = new Vector2f();
	
	public void pickupObject(GameObject go) {
		if(this.holdingObject != null) {
			this.holdingObject.destroy();
		}
		this.holdingObject = go;
		this.holdingObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(0.8f, 0.8f, 0.8f, 0.5f));
		this.holdingObject.addComponent(new NonPickable());
		Window.getScene().addGameObjectToScene(go);
		
	}
	
	public void place() {
		GameObject newObj = holdingObject.copy();
		if(newObj.getComponent(StateMachine.class)!= null) {
			newObj.getComponent(StateMachine.class).refreshTextures();
		}
		newObj.getComponent(SpriteRenderer.class).setColor(new Vector4f(1, 1, 1, 1));
		
		newObj.removeComponent(NonPickable.class);
		
		Window.getScene().addGameObjectToScene(newObj);
	}
	
	public void editorUpdate(float dt) {
		debounce -= dt;
		PickingTexture pickingTexture = Window.getImGuiLayer().getPropertiesWindow().getPickingTexture();
		Scene currentScene = Window.getScene();
		
		if(holdingObject != null) {
			float x = mouseListener.getWorldX();
            float y = mouseListener.getWorldY();
            holdingObject.transform.position.x = ((int)Math.floor(x / settings.GRID_WIDTH) * settings.GRID_WIDTH) + settings.GRID_WIDTH / 2.0f;
            holdingObject.transform.position.y = ((int)Math.floor(y / settings.GRID_HEIGHT) * settings.GRID_HEIGHT) + settings.GRID_HEIGHT / 2.0f;
			
			if(mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
				float halfWidth = settings.GRID_WIDTH / 2.0f;
				float halfHeight = settings.GRID_HEIGHT / 2.0f;
				if(mouseListener.isDragging() && !blockInSquare(holdingObject.transform.position.x - halfWidth,
						holdingObject.transform.position.y - halfHeight)) {
					place();
				}else if(!mouseListener.isDragging() && debounce < 0) {
					place();
					debounce = debounceTime;
				}
			}
			
			if(keyListener.isKeyPressed(GLFW_KEY_ESCAPE)) {
				holdingObject.destroy();
				holdingObject = null;
			}
		}else if(!mouseListener.isDragging() && mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && debounce < 0) {
			int x = (int)mouseListener.getScreenX();
			int y = (int)mouseListener.getScreenY();
			int gameObjectId = pickingTexture.readPixel(x, y);
			
			GameObject pickedObj = currentScene.getGameObject(gameObjectId);
			if(pickedObj != null && pickedObj.getComponent(NonPickable.class)== null) {
				Window.getImGuiLayer().getPropertiesWindow().setActiveGameObject(pickedObj);
			}else if(pickedObj == null & !mouseListener.isDragging()) {
				Window.getImGuiLayer().getPropertiesWindow().clearSelected();
			}
			this.debounce = 0.2f;
		}else if(mouseListener.isDragging() && mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
			//dragging and pressing left mouse button
			if(!boxSelectSet) {
				Window.getImGuiLayer().getPropertiesWindow().clearSelected();
				boxSelectStart = mouseListener.getScreen();
				boxSelectSet = true;
			}
			boxSelectEnd = mouseListener.getScreen();
			Vector2f boxSelectStartWorld = mouseListener.screenToWorld(boxSelectStart);
			Vector2f boxSelectEndWorld = mouseListener.screenToWorld(boxSelectEnd);
			Vector2f halfSize = (new Vector2f(boxSelectEndWorld).sub(boxSelectStartWorld)).mul(0.5f);
			DebugDraw.addBox2D((new Vector2f(boxSelectStartWorld)).add(halfSize), new Vector2f(halfSize).mul(2.0f), 0.0f);
		}else if(boxSelectSet) {
			boxSelectSet = false;
			int screenStartX = (int)boxSelectStart.x;
			int screenStartY = (int)boxSelectStart.y;
			int screenEndX = (int)boxSelectEnd.x;
			int screenEndY = (int)boxSelectEnd.y;
			boxSelectStart.zero();
			boxSelectEnd.zero();
			
			if(screenEndX < screenStartX) {
				int tmp = screenStartX;
				screenStartX = screenEndX;
				screenEndX = tmp;
			}
			if(screenEndY < screenStartY) {
				int tmp = screenStartY;
				screenStartY = screenEndY;
				screenEndY = tmp;
			}
			
			float[] gameObjectIds = pickingTexture.readPixels(new Vector2i(screenStartX, screenStartY), new Vector2i(screenEndX, screenEndY));
			
			Set<Integer> uniqueGameObjectIds = new HashSet<>();
			for(float objId : gameObjectIds) {
				uniqueGameObjectIds.add((int)objId);
			}
			for(Integer gameObjectId : uniqueGameObjectIds) {
				GameObject pickedObj = Window.getScene().getGameObject(gameObjectId);
				if(pickedObj != null && pickedObj.getComponent(NonPickable.class) == null) {
					Window.getImGuiLayer().getPropertiesWindow().addActiveGameObject(pickedObj);
				}
			}
		}
	}
	
	private boolean blockInSquare(float x, float y) {
		PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
		Vector2f start = new Vector2f(x, y);
		Vector2f end = new Vector2f(start).add(new Vector2f(settings.GRID_WIDTH, settings.GRID_HEIGHT));
		Vector2f startScreenf = mouseListener.worldToScreen(start);
		Vector2f endScreenf = mouseListener.worldToScreen(end);
		Vector2i startScreen = new Vector2i((int)startScreenf.x + 2, (int)startScreenf.y + 2);
		Vector2i endScreen = new Vector2i((int)endScreenf.x - 2, (int)endScreenf.y - 2);
		float[] gameObjectIds = propertiesWindow.getPickingTexture().readPixels(startScreen, endScreen);
		for(int i=0; i < gameObjectIds.length; i++) {
			if(gameObjectIds[i] >= 0) {
				GameObject pickedObj = Window.getScene().getGameObject((int)gameObjectIds[i]);
				if(pickedObj != null) {//TODO: fix possible null pointer when attempting to drag place blocks
					if(pickedObj.getComponent(NonPickable.class) == null) {
						return true;
					}
				}
			}
		}
		return false;
		
	}
}
