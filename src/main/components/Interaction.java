package main.components;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import renderer.DebugDraw;

import java.util.ArrayList;
import java.util.List;

public class Interaction extends Component{

    private static transient GameObject player;
    private static Vector2f pos;
    private Vector2f range;
    private Vector2f dimensions;
    private Vector3f color = new Vector3f(0.3f, 0.5f, 0.0f);
    private int keyCode;
    private int interactionID;
    private float debounce;
    private static ArrayList<Interaction> interactions = new ArrayList<>();

    public Interaction(Vector2f range,Vector2f dimensions, int keyCode, int interactionID){

        this.range = range;
        this.keyCode = keyCode;
        this.interactionID = interactionID;
        this.dimensions = dimensions;
        interactions.add(this);
    }

    @Override
    public void start(){
        this.player = Window.getScene().getGameObjectWith(PlayerController.class);

    }

    @Override
    public void update(float dt){
        debounce -= 0.08f;
        //calculate how close the player is
        pos = this.gameObject.transform.position;
        float diffX = Math.abs(pos.x - player.transform.position.x);
        float diffY = Math.abs(pos.y - player.transform.position.y);
        //System.out.println(diffX + "," + diffY);

        for(Interaction inter : interactions){
            //System.out.println(interactions);
            //check if player in range
            if((diffX < inter.range.x) && (diffY < inter.range.y)){
                //System.out.println(diffX + "," + diffY);
                DebugDraw.addBox2D(pos, new Vector2f(dimensions.x / 4, dimensions.y / 4), 0.0f, color);
                //display lines if in range, execute interaction if key pressed
                if(keyListener.isKeyPressed(inter.keyCode) && debounce <= 0){
                    //System.out.println("inside inter");
                    //trigger appropriate interaction
                    triggerInteraction(inter.interactionID);
                    debounce = 1.0f;
                }
            }
        }
    }

    private void triggerInteraction(int interactionID){
        switch(interactionID){
            case 0:
                break;
            case 1:
                System.out.println("pressed f");
                break;
            case 2:
                break;
            default:
                assert false: "Error: interaction not defined";
        }
    }

}
