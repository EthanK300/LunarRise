package main.items;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import main.player.PlayerController;
import main.util.settings;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

//michael.mackrory@nike.com
public abstract class Item{
    private static final GameObject player = Window.getScene().getGameObjectWith(PlayerController.class);
    public Vector2f pos;
    public String name;
    public static final Vector2f range = settings.acquireItemRange;
    public Vector2f dimensions;
    public boolean inScene;

    public Vector3f color;
    public Item(Vector2f position, String name, Vector2f dimensions, Vector3f color, boolean startInScene){
        this.name = name;
        this.pos = position;
        this.dimensions = dimensions;
        this.color = color;
        this.inScene = startInScene;
    }
    public abstract void selfUpdate(float dt);
    public abstract void start();
    public void update(float dt){
        float diffX = Math.abs(pos.x - player.transform.position.x);
        float diffY = Math.abs(pos.y - player.transform.position.y);
        //check if player in range
        if ((diffX < range.x) && (diffY < range.y)) {
            DebugDraw.addBox2D(pos, new Vector2f(dimensions.x / 4, dimensions.y / 4), 0.0f, color);
            //in range for player to pick up if they have space in inventory
            if(keyListener.isKeyPressed(settings.acquireItem)){
                //attempted pickup
                if(player.getComponent(PlayerController.class).addItemInv(this)){
                    this.inScene = false;
                    System.out.println("added " + this.name + " to inventory");
                    //added item to inventory
                }else{
                    DebugDraw.addBox2D(pos, new Vector2f(dimensions.x / 4, dimensions.y / 4), 0.0f, new Vector3f(1.0f,0.0f,0.0f), 1);
                    //item can't be added to inventory
                }
            }
        }
    }
}