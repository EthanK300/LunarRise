package main.components;


import editor.PropertiesWindow;
import main.engine.mouseListener;


public class TranslateGizmo extends Gizmo{
	
	public TranslateGizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
		super(arrowSprite, propertiesWindow);
	}
	@Override
	public void editorUpdate(float dt) {
		if(activeGameObject != null) {
			if(xAxisActive && !yAxisActive) {
				activeGameObject.transform.position.x -= mouseListener.getWorldX();
			}else if(yAxisActive) {
				activeGameObject.transform.position.y -= mouseListener.getWorldY();
			}
		}
		
		super.editorUpdate(dt);
	}
	
}
