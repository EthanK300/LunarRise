package main.components;

import main.engine.Window;
import main.engine.keyListener;
import static org.lwjgl.glfw.GLFW.*;

public class GizmoSystem extends Component{
	
	private Spritesheet gizmos;
	private int usingGizmo = 0;
	
	public GizmoSystem(Spritesheet gizmoSprites) {
		gizmos = gizmoSprites;
	}
	
	@Override
	public void start() {
		gameObject.addComponent(new TranslateGizmo(gizmos.getSprite(1), Window.getImGuiLayer().getPropertiesWindow()));
		gameObject.addComponent(new ScaleGizmo(gizmos.getSprite(2), Window.getImGuiLayer().getPropertiesWindow()));
		
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(usingGizmo == 0) {
			gameObject.getComponent(TranslateGizmo.class).setUsing();
			gameObject.getComponent(ScaleGizmo.class).setNotUsing();
		}else if(usingGizmo == 1) {
			gameObject.getComponent(TranslateGizmo.class).setNotUsing();
			gameObject.getComponent(ScaleGizmo.class).setUsing();
		}
		
		if(keyListener.isKeyPressed(GLFW_KEY_KP_ADD)) {
			usingGizmo = 0;
		}else if(keyListener.isKeyPressed(GLFW_KEY_KP_SUBTRACT)) {
			usingGizmo = 1;
		}
	}
}
