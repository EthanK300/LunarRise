package main.engine;

import main.components.AnimationState;
import main.player.PlayerController;
import main.components.Sprite;
import main.components.SpriteRenderer;
import main.components.Spritesheet;
import main.components.StateMachine;
import main.util.AssetPool;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import physics2d.enums.BodyType;

public class PreFabs {
	
	public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
		GameObject block = Window.getScene().createGameObject("Sprite_Object_Gen");
		block.transform.scale.x = sizeX;
		block.transform.scale.y = sizeY;
		//new Transform(new Vector2f(), new Vector2f(sizeX, sizeY))
		SpriteRenderer renderer = new SpriteRenderer();
		renderer.setSprite(sprite);
		block.addComponent(renderer);
		
		return block;
		
	}


	
	public static GameObject generateCharacter() {
		//requesting the player spritesheet from the asset pool
		Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/playerSprites.png");
		//create game object
		GameObject character = generateSpriteObject(playerSprites.getSprite(0), 0.25f, 0.25f);
		//sprite based animations
		
		//basic framework for animation
		/**
		 * define animation
		 * 
		 * give animation a title
		 * give animation a speed
		 * add each frame IN ORDER for animation
		 * define if its looping or not
		 * add to state machine for animation computing
		 * 
		**/
		//this doesn't work cuz no spritesheet yet
		AnimationState run = new AnimationState();
		run.title = "Run";
		float defaultFrameTime = 0.23f;//animation speed
		run.addFrame(playerSprites.getSprite(0), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(2), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(3), defaultFrameTime);
		run.addFrame(playerSprites.getSprite(2), defaultFrameTime);
		run.setLoop(true);
		
		StateMachine stateMachine = new StateMachine();
		stateMachine.addState(run);
		stateMachine.setDefaultState(run.title);
		//setting a default state(just in case no triggers occur)
		
		//add bindings for animations when triggers happen
		/**
		 * format:
		 * stateMachine.addState(from animation, to animation, trigger name);
		 * 
		 */
		
		
		//add all animations as a component to character
		character.addComponent(stateMachine);
		//add player collider
		PillboxCollider pb = new PillboxCollider();
		//Box2DCollider bc = new Box2DCollider();
		//bc.setHalfSize(new Vector2f(0.24f,0.24f));
		//bc.setOffset(new Vector2f(0,0));
		//creating the pb collider
        pb.width = 0.39f;
        pb.height = 0.31f;
        RigidBody2D rb = new RigidBody2D();
        rb.setBodyType(BodyType.Dynamic);
        rb.setContinuousCollision(false);
        rb.setFixedRotation(true);//can't fall over if character hits wall
        rb.setMass(25.0f);
		
        character.addComponent(rb);
		//character.addComponent(bc);
        character.addComponent(pb);
        character.addComponent(new PlayerController());

		return character;
		
	}
	
}
