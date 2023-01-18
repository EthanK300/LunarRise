package main.components;


import org.joml.Vector4f;
import main.engine.Camera;
import main.engine.GameObject;
import main.engine.Window;
import main.util.settings;


public class GameCamera extends Component{
	private transient GameObject player;
	private transient Camera gameCamera;
	private transient float highestX = Float.MIN_VALUE;
	private transient float highestY = Float.MIN_VALUE;
	private transient float undergroundYLevel = 0.0f;
	private transient float cameraBuffer = 1.5f;
	
	private transient Vector4f cameraThreshold = settings.cameraThreshold;
	//private transient float playerBuffer = 0.25f;
	//private static Vector2f terminalVelocity = settings.terminalVelocitry;
	private Vector4f backGroundColor = new Vector4f(54.0f / 255.0f, 117.0f / 255.0f, 136.0f / 255.0f, 1.0f);
	//light blueish color
	private Vector4f undergroundColor = new Vector4f(0, 0, 0, 1);
	
	//camera panning thresholds assignment
	private float rightTh = cameraThreshold.x;
	private float leftTh = cameraThreshold.y;
	private float upTh = cameraThreshold.z;
	private float downTh = cameraThreshold.w;
	
	public GameCamera(Camera gameCamera) {
		this.gameCamera = gameCamera;
		
	}
	
	@Override
	public void start() {
		this.player = Window.getScene().getGameObjectWith(PlayerController.class);
		this.gameCamera.clearColor.set(backGroundColor);
		this.undergroundYLevel = this.gameCamera.position.y - this.gameCamera.getProjectionSize().y - this.cameraBuffer;
		
	}
	
	@Override
	public void update(float dt) {
		if(player != null && !player.getComponent(PlayerController.class).hasWon()) {
			
			float diffX = player.transform.position.x - gameCamera.position.x;
			float diffY = player.transform.position.y - gameCamera.position.y;
			float diffX2 = 0f;
			float diffY2 = 0f;
			
			if(diffX > rightTh) {
				diffX2 = diffX - rightTh;
				gameCamera.position.x = gameCamera.position.x + diffX2;
				diffX2 = 0f;
			}else if(diffX < leftTh) {
				diffX2 = diffX - leftTh;
				gameCamera.position.x = gameCamera.position.x + diffX2;
				diffX2 = 0f;
			}
			
			if(diffY > upTh) {
				diffY2 = diffY - upTh;
				gameCamera.position.y = gameCamera.position.y + diffY2;
				diffY2 = 0f;
			}else if(diffY < downTh) {
				diffY2 = diffY - downTh;
				gameCamera.position.y = gameCamera.position.y + diffY2;
				diffY2 = 0f;
			}
		}
	}
}
