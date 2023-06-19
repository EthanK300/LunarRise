package scenes;

import java.io.File;
import java.util.Collection;

import main.items.testItem;
import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImVec2;
import main.components.*;
import main.engine.*;
import main.util.AssetPool;
import org.joml.Vector3f;
import physics2d.components.Box2DCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;
import renderer.Texture;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glGetError;


public class levelEditorSceneInitializer extends SceneInitializer{

	private Spritesheet sprites;
	//SpriteRenderer obj1Sprite;
	private GameObject levelEditorStuff;

	public levelEditorSceneInitializer() {

	}

	@Override
	public void init(Scene scene) {


		sprites = AssetPool.getSpritesheet("assets/images/decorationsAndBlocks.png");

		Spritesheet gizmos = AssetPool.getSpritesheet("assets/images/gizmos.png");

		levelEditorStuff = scene.createGameObject("LevelEditor");
		levelEditorStuff.setNoSerialize();
		levelEditorStuff.addComponent(new MouseControls());
		levelEditorStuff.addComponent(new KeyControls());
		levelEditorStuff.addComponent(new GridLines());
		levelEditorStuff.addComponent(new EditorCamera(scene.camera()));
		levelEditorStuff.addComponent(new GizmoSystem(gizmos));
		scene.addGameObjectToScene(levelEditorStuff);
		//TODO: TIMESTAMP: 8:34:41, create a new sprite sheet for map textures
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
		//TODO:add all sounds
		AssetPool.addSound("assets/sounds/crabrave.ogg", true);
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
		/**
		 System.out.println("x: " + mouseListener.getScreenX());
		 System.out.println("y: " + mouseListener.getScreenY());
		 **/
		ImGui.begin("Level Editor Stuff");
		levelEditorStuff.imGui();
		ImGui.end();
		ImGui.begin("Objects");

		if(ImGui.beginTabBar("WindowTabBar")) {
			//tab for blocks
			if(ImGui.beginTabItem("Blocks")) {
				ImVec2 windowPos = new ImVec2();
				ImGui.getWindowPos(windowPos);
				ImVec2 windowSize = new ImVec2();
				ImGui.getWindowSize(windowSize);
				ImVec2 itemSpacing = new ImVec2();
				ImGui.getStyle().getItemSpacing(itemSpacing);

				float windowX2 = windowPos.x + windowSize.x;
				for (int i = 0; i < sprites.size(); i++) {

					//add sprites from the appropriate spritesheet that you want to not have box colliders
					/**
					 * format:
					 * if(i == texture's index) continue;
					 */

					Sprite sprite = sprites.getSprite(i);
					float spriteWidth = sprite.getWidth() * 4;
					float spriteHeight = sprite.getHeight() * 4;
					int id = sprite.getTexId();
					Vector2f[] texCoords = sprite.getTexCoords();

					ImGui.pushID(i);
					if(ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
						addBlock2D(sprite, i);
					}
					ImGui.popID();

					ImVec2 lastButtonPos = new ImVec2();
					ImGui.getItemRectMax(lastButtonPos);
					float lastButtonX2 = lastButtonPos.x;
					float nextButtonX2 = lastButtonX2 + itemSpacing.x + spriteWidth;
					if(i + 1 < sprites.size() && nextButtonX2 < windowX2) {
						ImGui.sameLine();
					}

				}
				ImGui.endTabItem();
			}

			if(ImGui.beginTabItem("Prefabs")) {
				Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/playerSprites.png");
				Sprite sprite = playerSprites.getSprite(0);
				float spriteWidth = sprite.getWidth() * 4;
				float spriteHeight = sprite.getHeight() * 4;
				int id = sprite.getTexId();
				Vector2f[] texCoords = sprite.getTexCoords();
				if(ImGui.imageButton(id, spriteWidth, spriteHeight, texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
					GameObject object = PreFabs.generateCharacter();//generated sprite size
					levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);

				}

				ImGui.sameLine();
				ImGui.endTabItem();
			}

			//tab for sounds
			if(ImGui.beginTabItem("Sounds")) {
				Collection<Sound> sounds = AssetPool.getAllSounds();
				for(Sound sound : sounds) {
					File tmp = new File(sound.getFilepath());
					if(ImGui.button(tmp.getName())) {
						if(!sound.isPlaying()) {
							sound.play();
						}else {
							sound.stop();
						}
					}

					if(ImGui.getContentRegionAvailX() > 100) {
						ImGui.sameLine();
					}
				}
				ImGui.endTabItem();
			}

			ImGui.endTabBar();

		}



		ImGui.end();
	}

	public void addBlock2D(Sprite sprite, int Index){
		GameObject object = PreFabs.generateSpriteObject(sprite, 0.25f, 0.25f);//generated sprite size
		RigidBody2D rb = new RigidBody2D();
		//if the shift key is pressed it changes to a non-static object
		if(keyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT) || keyListener.isKeyPressed(GLFW_KEY_RIGHT_SHIFT)) {
			rb.setBodyType(BodyType.Dynamic);
			//System.out.println("pressed");
		}else{
			rb.setBodyType(BodyType.Static);
		}
		//rb.setBodyType(BodyType.Static);
		object.addComponent(rb);
		Box2DCollider b2d = new Box2DCollider();
		b2d.setHalfSize(new Vector2f(0.25f, 0.25f)); //TODO: change the 0.25f to the grid size
		object.addComponent(b2d);
		object.addComponent(new Terrain());
		//add sprites from the appropriate spritesheet that you want to be breakable objects
		/**
		 * format:
		 * if(i = indexOfTexture){
		 * 	object.addComponent(new BreakableBlock());
		 * }
		 */
		//TODO: add more objects with interactions
		if(Index == 15){
			object.addComponent(new detector(new Vector2f(1,1), new Vector2f(1,1), GLFW_KEY_F, 1, new Vector3f(0.0f, 0.0f, 1.0f)));
			//System.out.println("15");
			//Interaction inty = new Interaction(new Vector2f(1,1), new Vector2f(1,1), GLFW_KEY_F, 1, new Vector3f(0.0f, 0.0f, 1.0f));
			//object.addComponent(inty);
			//manager.add(inty);
		}
		if(Index == 14){
			object.addComponent(new destroyer(new Vector2f(1,1), new Vector2f(1,1), GLFW_KEY_C, 0, new Vector3f(0.0f, 0.0f, 1.0f)));
			//System.out.println("15");
			//Interaction inty = new Interaction(new Vector2f(1,1), new Vector2f(1,1), GLFW_KEY_C, 0, new Vector3f(0.0f, 0.0f, 1.0f));
			//object.addComponent(inty);
			//manager.add(inty);
		}
		if(Index == 13){
			object.addComponent(new testItem("testitem1", new Vector2f(1, 1), new Vector3f(1.0f, 0.0f, 0.0f), true));
		}

		levelEditorStuff.getComponent(MouseControls.class).pickupObject(object);

	}

}