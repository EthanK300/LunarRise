package main.components;

import org.joml.Vector2f;
import org.joml.Vector3f;

import main.engine.Camera;
import main.engine.Window;
import main.util.settings;
import renderer.DebugDraw;

public class GridLines extends Component{
	
	@Override
	public void editorUpdate(float dt) {
		
		Camera camera = Window.getScene().camera();
		
		Vector2f cameraPos = camera.position;
		Vector2f projectionSize = camera.getProjectionSize();
		
		float firstX = ((int)Math.floor(cameraPos.x / settings.GRID_WIDTH) -1) * settings.GRID_WIDTH;
		float firstY = ((int)Math.floor(cameraPos.y / settings.GRID_HEIGHT) -1) * settings.GRID_HEIGHT;
		
		int numVtLines = (int)(projectionSize.x * camera.getZoom()/ settings.GRID_WIDTH) + 2;
		int numHzLines = (int)(projectionSize.y * camera.getZoom()/ settings.GRID_HEIGHT) + 2;

		float width = (int)(projectionSize.x * camera.getZoom())+ settings.GRID_WIDTH * 2;
		float height = (int)(projectionSize.y * camera.getZoom()) + settings.GRID_HEIGHT * 2;
		
		int maxLines = Math.max(numVtLines, numHzLines);
		//System.out.println(maxLines);
		Vector3f color = new Vector3f(0.2f, 0.2f, 0.2f);
		//int v=0;
		//int h=0;
		for(int i = 0; i < maxLines; i++) {
			float x = firstX + (settings.GRID_WIDTH * i);
			float y = firstY + (settings.GRID_HEIGHT * i);
			
			if(i < numVtLines) {
				DebugDraw.addLine2D(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color);
				//v++;
			}
			
			if(i < numHzLines) {
				DebugDraw.addLine2D(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color);
				//h++;
			}
			//System.out.println(v + "," + h);
		}
	}
}
