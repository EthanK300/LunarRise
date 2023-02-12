package scenes;

import java.io.File;
import java.util.Collection;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import main.components.*;
import main.engine.*;
import main.util.AssetPool;
import physics2d.components.Box2DCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
import static org.lwjgl.glfw.GLFW.*;

public class LevelSceneInitializer extends SceneInitializer{
	public LevelSceneInitializer() {

	}
	
	@Override
	public void init(Scene scene) {
		
		
		Spritesheet sprites = AssetPool.getSpritesheet("assets/images/decorationsAndBlocks.png");
		
		GameObject cameraObject = scene.createGameObject("GameCamera");
		cameraObject.addComponent(new GameCamera(scene.camera()));
		
		cameraObject.start();
		scene.addGameObjectToScene(cameraObject);
		//TODO: TIMESTAMP: 8:34:41, create a new spritesheet for map textures
		
		
	}
	@Override
	public void loadResources(Scene scene) {
		AssetPool.getShader("assets/shaders/default.glsl");
		
		AssetPool.addSpriteSheet("assets/images/playerSprites.png",
				new Spritesheet(AssetPool.getTexture("assets/images/playerSprites.png"), 16, 16, 81, 0));//change 16,16,81 to respective size and num of sprites
		
		//TODO: add new spritesheet with player character models
		
		AssetPool.addSpriteSheet("assets/images/decorationsAndBlocks.png",
				new Spritesheet(AssetPool.getTexture("assets/images/decorationsAndBlocks.png"), 16, 16, 81, 0));
				//TODO: TIMESTAMP: 8:57:01, substitute "16" for sprite size
		AssetPool.addSpriteSheet("assets/images/gizmos.png", new Spritesheet(AssetPool.getTexture("assets/images/gizmos.png"), 24, 48, 3, 0));
		
		AssetPool.getTexture("assets/images/blendImage2.png");
		AssetPool.getTexture("assets/images/backDrop.png");
		AssetPool.getTexture("assets/images/backDrop2.png");

		//TODO:add all sounds, this one doesn't work yet
		//AssetPool.addSound("assets/sounds/test.ogg", false);
		
		for(GameObject g : scene.getGameObjects()) {
			if(g.getComponent(SpriteRenderer.class) != null) {
				SpriteRenderer spr = g.getComponent(SpriteRenderer.class);
				if(spr.getTexture() != null) {
					spr.setTexture(AssetPool.getTexture(spr.getTexture().getFilepath()));
				}
			}
			
			if(g.getComponent(StateMachine.class) != null) {
				StateMachine stateMachine = g.getComponent(StateMachine.class);
				stateMachine.refreshTextures();
			}
		}
		
	}

	@Override
	public void imgui() {
		//just to resolve errors, nothing here
	}
	
	
	
	
}
