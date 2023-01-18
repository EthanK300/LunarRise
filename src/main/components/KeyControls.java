package main.components;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;

import java.util.ArrayList;
import java.util.List;

import editor.PropertiesWindow;
import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import main.util.settings;

public class KeyControls extends Component{
	
	PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
	GameObject activeGameObject = propertiesWindow.getActiveGameObject();
	List<GameObject> activeGameObjects = propertiesWindow.getActiveGameObjects();
	
	public void editorUpdate(float dt) {
		if(keyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && keyListener.keyBeginPress(GLFW_KEY_D) && activeGameObject != null) {
			GameObject newObj = activeGameObject.copy();
			Window.getScene().addGameObjectToScene(newObj);
			newObj.transform.position.add(settings.GRID_WIDTH, 0.0f);
			propertiesWindow.setActiveGameObject(newObj);
			if(newObj.getComponent(StateMachine.class) != null) {
				newObj.getComponent(StateMachine.class).refreshTextures();
			}
		}else if(keyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && keyListener.keyBeginPress(GLFW_KEY_D)) {
			List<GameObject> gameObjects = new ArrayList<>(activeGameObjects);
			propertiesWindow.clearSelected();
			for(GameObject go : gameObjects) {
				GameObject copy = go.copy();
				Window.getScene().addGameObjectToScene(copy);
				propertiesWindow.addActiveGameObject(copy);
				if(copy.getComponent(StateMachine.class) != null) {
					copy.getComponent(StateMachine.class).refreshTextures();
				}
			}
			
			
		}else if(keyListener.keyBeginPress(GLFW_KEY_DELETE)) {
			for(GameObject go : activeGameObjects) {
				go.destroy();
			}
			propertiesWindow.clearSelected();
		}
	}
	
	
}
