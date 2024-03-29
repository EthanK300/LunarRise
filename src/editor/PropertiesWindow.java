package editor;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import imgui.ImGui;
import main.components.NonPickable;
import main.components.RigidBody;
import main.components.SpriteRenderer;
import main.engine.GameObject;
import main.engine.mouseListener;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import renderer.PickingTexture;
import scenes.Scene;

public class PropertiesWindow {
	private GameObject activeGameObject = null;
	private PickingTexture pickingTexture;
	private List<Vector4f> activeGameObjectsOgColor;
	private List<GameObject> activeGameObjects;
	
	public PropertiesWindow(PickingTexture pickingTexture){
		this.activeGameObjects = new ArrayList<>();
		this.pickingTexture = pickingTexture;
		this.activeGameObjectsOgColor = new ArrayList<>();
	}
	
	public void imgui() {
		if(activeGameObjects.size() == 1 && activeGameObjects.get(0) != null) {
			activeGameObject = activeGameObjects.get(0);
			ImGui.begin("Properties");
			
			if(ImGui.beginPopupContextWindow("ComponentAdder")) {
				if(ImGui.menuItem("Add RigidBody")) {
					if(activeGameObject.getComponent(RigidBody2D.class) == null) {
						activeGameObject.addComponent(new RigidBody2D());
					}
				}
				
				if(ImGui.menuItem("Add Box Collider")) {
					if(activeGameObject.getComponent(Box2DCollider.class) == null && activeGameObject.getComponent(CircleCollider.class) == null) {
						activeGameObject.addComponent(new Box2DCollider());
					}
				}
				
				if(ImGui.menuItem("Add Circle Collider")) {
					if(activeGameObject.getComponent(CircleCollider.class) == null && activeGameObject.getComponent(Box2DCollider.class) == null) {
						activeGameObject.addComponent(new CircleCollider());
					}
				}
				
				ImGui.endPopup();
				
			}
			
			activeGameObject.imGui();
			ImGui.end();
		}
	}
	
	public GameObject getActiveGameObject() {
		return activeGameObjects.size() == 1 ? this.activeGameObjects.get(0) : null;
	}
	
	public List<GameObject> getActiveGameObjects(){
		return this.activeGameObjects;
	}
	
	public void clearSelected() {
		
		if(activeGameObjectsOgColor.size() > 0) {
			int i = 0;
			for(GameObject go : activeGameObjects) {
				SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
				if(spr != null) {
					spr.setColor(activeGameObjectsOgColor.get(i));
				}
				i++;
			}
		}
		this.activeGameObjects.clear();
		this.activeGameObjectsOgColor.clear();
	}
	
	public void setActiveGameObject(GameObject go) {
		if(go != null) {
			clearSelected();
			this.activeGameObjects.add(go);
		}
	}
	
	public void addActiveGameObject(GameObject go) {
		SpriteRenderer spr = go.getComponent(SpriteRenderer.class);
		if(spr != null) {
			this.activeGameObjectsOgColor.add(new Vector4f(spr.getColor()));
			spr.setColor(new Vector4f(0.5f, 0.0f, 0.5f, 0.0f));
		}else {
			this.activeGameObjectsOgColor.add(new Vector4f());
		}
		this.activeGameObjects.add(go);
	}
	
    
    public PickingTexture getPickingTexture() {
    	return this.pickingTexture;
    }
}
