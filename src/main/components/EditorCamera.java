package main.components;

import org.joml.Vector2f;
import static org.lwjgl.glfw.GLFW.*;
import main.engine.Camera;
import main.engine.keyListener;
import main.engine.mouseListener;

public class EditorCamera extends Component{
	
	private float dragDebounce = 0.032f;
	private float dragSensitivity = 30.0f;
	private float scrollSensitivity = 0.1f;
	private Camera levelEditorCamera;
	private Vector2f clickOrigin;
	private boolean reset = false;
	private float lerpTime = 0.0f;
	private float autoSnap = 5.0f;
	
	public EditorCamera(Camera levelEditorCamera2) {
		this.levelEditorCamera = levelEditorCamera2;
		this.clickOrigin = new Vector2f();
	}
	
	@Override
	public void editorUpdate(float dt) {

		if(mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE) && dragDebounce > 0) {
			this.clickOrigin = new Vector2f(mouseListener.getWorldX(), mouseListener.getWorldY());
			dragDebounce -= dt;
			return;
		}else if(mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
			Vector2f mousePos = new Vector2f(mouseListener.getWorldX(), mouseListener.getWorldY());
			Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);
			levelEditorCamera.position.sub(delta.mul(dt).mul(dragSensitivity));
			this.clickOrigin.lerp(mousePos, dt);
		}
		
		if(dragDebounce <= 0.0f && !mouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
			dragDebounce = 0.1f;
		}
		
		if(mouseListener.getScrollY() != 0.0f) {
			float addValue = (float)Math.pow(Math.abs(mouseListener.getScrollY() * scrollSensitivity), 1 / levelEditorCamera.getZoom());
			addValue *= Math.signum(mouseListener.getScrollY());
			levelEditorCamera.addZoom(addValue);
		}
		
		if(keyListener.isKeyPressed(GLFW_KEY_KP_ENTER)) {
			reset = true;
		}
		
		if(reset) {
			//System.out.println("resetted view");
			levelEditorCamera.position.lerp(new Vector2f(), lerpTime);
			levelEditorCamera.setZoom(this.levelEditorCamera.getZoom() + ((1.0f - levelEditorCamera.getZoom()) * lerpTime));
			this.lerpTime += 0.1f * dt;
			if(Math.abs(levelEditorCamera.position.x) <= autoSnap && Math.abs(levelEditorCamera.position.y) <= autoSnap) {
				this.lerpTime = 0.0f;
				levelEditorCamera.position.set(0f,0f);
				this.levelEditorCamera.setZoom(1.0f);
				reset = false;
			}
			
		}
		
	}
	
}
