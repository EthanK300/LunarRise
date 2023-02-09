package main.items;

import main.components.Component;
import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import main.player.PlayerController;
import main.util.settings;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

//michael.mackrory@nike.com
public abstract class Item extends Component {
    public static GameObject player;
    public Vector2f pos;
    public String name;
    public static final Vector2f range = settings.acquireItemRange;
    public Vector2f dimensions;
    public boolean inScene;
    public Vector3f BoxColor;
    public float debounce;
    public static PlayerController PC;

    public abstract void selfUpdate(float dt);
    public abstract void selfStart();
    @Override
    public void update(float dt){
        player = Window.getScene().getGameObjectWith(PlayerController.class);
        PC = player.getPlayerController();
        selfUpdate(dt);
    }
    @Override
    public void start(){
        selfStart();
    }
}