package main.player;

import main.components.Component;
import main.components.StateMachine;
import main.components.Terrain;
import main.items.Item;
import org.joml.Vector2f;
import org.joml.Vector3f;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import main.util.settings;
import physics2d.RaycastInfo;
import physics2d.components.RigidBody2D;
import renderer.DebugDraw;

import static org.lwjgl.glfw.GLFW.*;

import org.jbox2d.dynamics.contacts.Contact;

import java.util.ArrayList;
import java.util.List;

public class PlayerController extends Component {

	private enum PlayerState{
		Small,
		Big,
		Invulnerable,
		Fire
	}

	public float walkSpeed = settings.walkSpeed;
	public float jumpForce = settings.jumpForce;
	public float jumpImpulse = settings.jumpForce;
	public float slowDownForce = settings.slowDownForce;
	public Vector2f terminalVelocity = settings.terminalVelocitry;

	private PlayerState playerState = PlayerState.Small;
	public transient boolean onGround = false;
	private transient float groundDebounce = 0.0f;
	private transient float groundDebounceTime = 0.1f;
	private transient RigidBody2D rb;
	private transient StateMachine stateMachine;
	private transient float megaJumpBoostFactor = settings.megaJumpBoostFactor;
	private transient float playerWidth = 0.25f;
	private transient float playerHeight = 0.25f;
	private transient int jumpTime = 0;
	private transient Vector2f acceleration = new Vector2f();
	private transient Vector2f velocity = new Vector2f();
	private transient boolean isDead = false;
	private transient int enemyBounce = 0;
	private boolean controlsActive;
	private int MAX_SIZE = settings.inventorySize;
	private Vector2f pos;
	private GameObject player;
	private Vector2f acquireItemRange = settings.acquireItemRange;
	private List<Item> items;

	@Override
	public void start() {
		//attach everything to game object
		this.rb = gameObject.getComponent(RigidBody2D.class);
		this.stateMachine = gameObject.getComponent(StateMachine.class);
		this.rb.setGravityScale(0.0f);
		this.controlsActive = true;
		this.pos = this.gameObject.transform.position;
		this.items = new ArrayList<>();
	}

	public boolean hasWon() {
		return false;
	}

	public boolean getControlsActive() {
		return this.controlsActive;
	}

	public Vector2f getVelocity() {
		return this.velocity;
	}

	@Override
	public void update(float dt) {
		//code player control bindings
		/**
		 * format:
		 * if(key is pressed(key){
		 * 	modify player position/size/scale whatever is needed
		 * 	change animation using statemachine.trigger(trigger);
		 * }
		 *
		 */

		if(!controlsActive) {
			return;
		}

		if(keyListener.isKeyPressed(GLFW_KEY_RIGHT) || keyListener.isKeyPressed(GLFW_KEY_D)) {
			this.gameObject.transform.scale.x = playerWidth;
			this.acceleration.x = walkSpeed;
			if(this.velocity.x < 0) {
				//this.stateMachine.trigger("switchDirection");//TODO: create this animation state
				this.velocity.x += slowDownForce;
			}else {
				this.stateMachine.trigger("startRunning");
			}
		}else if(keyListener.isKeyPressed(GLFW_KEY_LEFT) || keyListener.isKeyPressed(GLFW_KEY_A)) {
			this.gameObject.transform.scale.x = -playerWidth;
			this.acceleration.x = -walkSpeed;
			if(this.velocity.x > 0) {
				//this.stateMachine.trigger("switchDirection");//TODO: create this animation state
				this.velocity.x -= slowDownForce;
			}else {
				//this.stateMachine.trigger("startRunning");//TODO: create this animation state
			}
		}else {
			this.acceleration.x = 0;
			if(this.velocity.x > 0) {
				this.velocity.x = Math.max(0,  this.velocity.x - slowDownForce);
			}else if(this.velocity.x < 0) {
				this.velocity.x = Math.min(0,  this.velocity.x + slowDownForce);
			}
		}
		if(this.velocity.x == 0) {
			//this.stateMachine.trigger("stopRunning");//TODO: create this animation state
		}
		//check if on ground
		checkOnGround();
		//checkOnLeft();
		if((keyListener.isKeyPressed(GLFW_KEY_SPACE) || keyListener.isKeyPressed(GLFW_KEY_UP)) && (jumpTime > 0 || onGround || groundDebounce > 0)){
			//jumped
			//System.out.println(System.currentTimeMillis() + "," + jumpTime);
			if((onGround || groundDebounce > 0) && jumpTime == 0){
				//AssetPool.getSound("assets/sounds/jump-small.ogg").play();//TODO: add asset to game
				jumpTime = 28;//max variable length jump
				this.velocity.y = jumpImpulse;

			}else if(jumpTime > 0) {
				jumpTime--;
				this.velocity.y = ((jumpTime / 2.2f) * jumpForce);
			}else {
				this.velocity.y = 0;
			}
			groundDebounce = 0;
		}else if(!onGround) {
			if(this.jumpTime > 0) {
				this.velocity.y *= 0.35f;//TODO:tune this for if you release space key early, how fast do you fall?
				this.jumpTime = 0;
			}
			groundDebounce -= dt;
			this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;//TODO: tune gravity
		}else {
			this.velocity.y = 0;
			this.acceleration.y = 0;
			groundDebounce = groundDebounceTime;
		}

		//System.out.println(System.currentTimeMillis() + "," + jumpTime);
		//update movement
		this.velocity.x += this.acceleration.x * dt;
		this.velocity.y += this.acceleration.y * dt;
		this.velocity.x = Math.max(Math.min(this.velocity.x,  this.terminalVelocity.x), -this.terminalVelocity.x);
		this.velocity.y = Math.max(Math.min(this.velocity.y,  this.terminalVelocity.y), -this.terminalVelocity.y);
		//System.out.println(this.velocity);
		//update box2d physics about new movement
		this.rb.setVelocity(this.velocity);
		this.rb.setAngularVelocity(0);

		if(!onGround) {
			//stateMachine.trigger("jump");//TODO:create animation
		}else {
			//stateMachine.trigger("stopJumping");//TODO:create animation
		}
	}

	public void checkOnGround() {
		Vector2f raycastBegin = new Vector2f(this.gameObject.transform.position);
		float innerPlayerWidth =  this.playerWidth * 0.9f; //TODO: scale this 0.6f to a width that is small enough to just barely reach the left and right side of the player
		raycastBegin.sub(innerPlayerWidth / 2.0f, 0.0f);
		float yVal = playerState == PlayerState.Small ? -0.14f : -0.24f;//0.14f and 0.24f are values for big and small character due to powerups(for raycasting)
		Vector2f raycastEnd = new Vector2f(raycastBegin).add(0.0f, yVal);
		RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);

		Vector2f raycast2Begin = new Vector2f(raycastBegin).add(innerPlayerWidth, 0.0f);
		Vector2f raycast2End = new Vector2f(raycastEnd).add(innerPlayerWidth, 0.0f);
		RaycastInfo info2 = Window.getPhysics().raycast(gameObject, raycast2Begin, raycast2End);

		onGround = (info.hit && info.hitObject != null && info.hitObject.getComponent(Terrain.class) != null) ||
				(info2.hit && info2.hitObject != null && info2.hitObject.getComponent(Terrain.class) != null);

		DebugDraw.addLine2D(raycastBegin, raycastEnd, new Vector3f(1, 0, 0));
		DebugDraw.addLine2D(raycast2Begin, raycast2End, new Vector3f(1, 0, 0));

	}
	/**
	 public void checkOnLeft() {
	 Vector2f raycastBegin = new Vector2f(this.gameObject.transform.position);
	 float innerPlayerHeight =  this.playerHeight * 0.9f; //TODO: scale this 0.6f to a width that is small enough to just barely reach the left and right side of the player
	 raycastBegin.sub(innerPlayerHeight / 2.0f, 0.0f);
	 float yVal = playerState == PlayerState.Small ? -0.14f : -0.24f;//0.14f and 0.24f are values for big and small character due to powerups(for raycasting)
	 Vector2f raycastEnd = new Vector2f(raycastBegin).add(0.0f, yVal);
	 RaycastInfo info = Window.getPhysics().raycast(gameObject, raycastBegin, raycastEnd);

	 Vector2f raycast2Begin = new Vector2f(raycastBegin).add(0.0f, innerPlayerHeight);
	 Vector2f raycast2End = new Vector2f(raycastEnd).add(0.0f, innerPlayerHeight);
	 RaycastInfo info2 = Window.getPhysics().raycast(gameObject, raycast2Begin, raycast2End);

	 onGround = (info.hit && info.hitObject != null && info.hitObject.getComponent(Terrain.class) != null) ||
	 (info2.hit && info2.hitObject != null && info2.hitObject.getComponent(Terrain.class) != null);

	 DebugDraw.addLine2D(raycastBegin, raycastEnd, new Vector3f(1, 0, 0));
	 DebugDraw.addLine2D(raycast2Begin, raycast2End, new Vector3f(1, 0, 0));
	 }
	 **/
	@Override
	public void beginCollision(GameObject collidingObject, Contact contact, Vector2f contactNormal) {
		if(isDead) {
			return;
		}
		//if colliding with ground
		if(collidingObject.getComponent(Terrain.class)!= null) {
			//if the block hit is actually terrain
			if(Math.abs(contactNormal.x)> 0.99f ) {
				this.velocity.x = 0;
			}else if(contactNormal.y > 0.99f) {
				this.velocity.y = 0;
				this.acceleration.y = 0;
				this.jumpTime = 0;
			}
		}
	}
	public boolean addItemInv(Item item){
		if(items.size() >= MAX_SIZE){
			items.add(item);
			return true;
		}else{
			return false;
		}
	}
	public boolean removeItemInv(Item item){
		if(items.contains(item)){
			items.remove(item);
			return true;
		}else{
			//inventory does not contain item
			return false;
		}
	}
	public boolean itemInInv(Item item){
		return items.contains(item);
	}

}