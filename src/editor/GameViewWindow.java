package editor;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import main.engine.Window;
import main.engine.mouseListener;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;

public class GameViewWindow {
	
	private float leftX, rightX, topY, bottomY;
	private boolean isPlaying = false;
	private int selfXOffset = 10;
	public void imgui() {
		ImGui.begin("Game Viewport", ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.MenuBar);
		
		ImGui.beginMenuBar();
		if(ImGui.menuItem("Play", "", isPlaying, !isPlaying)) {
			isPlaying = true;
			EventSystem.notify(null, new Event(EventType.GameEngineStartPlay));
		}
		
		if(ImGui.menuItem("Stop", "", !isPlaying, isPlaying)) {
			isPlaying = false;
			EventSystem.notify(null,  new Event(EventType.GameEngineStopPlay));
		}
		
		ImGui.endMenuBar();
		ImGui.setCursorPos(ImGui.getCursorPosX(), ImGui.getCursorPosY());
		ImVec2 windowSize = getLargestSizeForViewport();
		ImVec2 windowPos = getCenteredPositionForViewport(windowSize);
		ImGui.setCursorPos(windowPos.x, windowPos.y);
		
		/**
		ImVec2 topLeft = new ImVec2();
		ImGui.getCursorScreenPos(topLeft);
		
		topLeft.x -= ImGui.getScrollX();
		topLeft.y -= ImGui.getScrollY();
		**/
		leftX = windowPos.x + selfXOffset;
		bottomY = windowPos.y;
		rightX = windowPos.x + windowSize.x + selfXOffset;
		topY = windowPos.y + windowSize.y;
		
		int textureId = Window.getFramebuffer().getTextureId();
		ImGui.image(textureId, windowSize.x, windowSize.y, 0, 1, 1, 0);
		
		mouseListener.setGameViewportPos(new Vector2f(windowPos.x + selfXOffset, windowPos.y));
		mouseListener.setGameViewportSize(new Vector2f(windowSize.x, windowSize.y));
		
		ImGui.end();
		
	}
	
	private ImVec2 getLargestSizeForViewport() {
		ImVec2 windowSize = new ImVec2();
		ImGui.getContentRegionAvail(windowSize);
		/**
		windowSize.x -= ImGui.getScrollX();
		windowSize.y -= ImGui.getScrollY();
		**/
		float aspectWidth = windowSize.x;
		float aspectHeight = aspectWidth / Window.getTargetAspectRatio();
		if(aspectHeight > windowSize.y) {
			//black sidebars needed if the above is true
			aspectHeight = windowSize.y;
			aspectWidth = aspectHeight * Window.getTargetAspectRatio();
		}
		
		return new ImVec2(aspectWidth, aspectHeight);
		
	}
	
	private ImVec2 getCenteredPositionForViewport(ImVec2 aspectSize) {
		
		ImVec2 windowSize = new ImVec2();
		ImGui.getContentRegionAvail(windowSize);
		/**
		windowSize.x -= ImGui.getScrollX();
		windowSize.y -= ImGui.getScrollY();
		**/
		float viewportX = (windowSize.x / 2.0f) - (aspectSize.x / 2.0f);
		float viewportY = (windowSize.y / 2.0f) - (aspectSize.y / 2.0f);
		
		return new ImVec2(viewportX + ImGui.getCursorPosX(), viewportY + ImGui.getCursorPosY());
		
	}
	
	public boolean getWantCaptureMouse() {
		//added a negator to the return statement
		return (mouseListener.getX() >= leftX && mouseListener.getX() <= rightX 
				&& mouseListener.getY() >= bottomY && mouseListener.getY() <= topY);
				
	}
	
}
