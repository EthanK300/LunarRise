package main.components;

import editor.PropertiesWindow;
import main.engine.mouseListener;

public class ScaleGizmo extends Gizmo{
	public ScaleGizmo(Sprite scaleSprite, PropertiesWindow propertiesWindow) {
		super(scaleSprite, propertiesWindow);
	}
	@Override
	public void editorUpdate(float dt) {
		if(activeGameObject != null) {
			if(xAxisActive && !yAxisActive) {
				activeGameObject.transform.scale.x -= mouseListener.getWorldX();
			}else if(yAxisActive) {
				activeGameObject.transform.scale.y -= mouseListener.getWorldY();
			}
		}
		
		super.editorUpdate(dt);
	}
}
