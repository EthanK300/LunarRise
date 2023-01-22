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
    private int keyCode;
    private int interactionID;
    private static List<Interaction> interactions = new ArrayList<>();

    public Interaction(Vector2f range,Vector2f dimensions, int keyCode, int interactionID){
        interactions.add(this);
        this.range = range;
        this.keyCode = keyCode;
        this.interactionID = interactionID;
        this.dimensions = dimensions;
    }

    @Override
    public void start(){
        this.player = Window.getScene().getGameObjectWith(PlayerController.class);

    }

    @Override
    public void update(float dt){

        //calculate how close the player is
        pos = this.gameObject.transform.position;
        float diffX = Math.abs(pos.x - player.transform.position.x);
        float diffY = Math.abs(pos.y - player.transform.position.y);
        //System.out.println(diffX + "," + diffY);
        for(Interaction inter : interactions){

            //check if player in range
            if((diffX < inter.range.x) || (diffY < inter.range.y)){
                //System.out.println(diffX + "," + diffY);
                DebugDraw.addBox2D(pos, dimensions, 0.0f, new Vector3f(154, 250, 50));
                //display lines if in range, execute interaction if key pressed
                if(keyListener.isKeyPressed(inter.keyCode)){
                    //System.out.println("inside inter");
                    //trigger appropriate interaction
                    triggerInteraction(inter.interactionID);
                }
            }
        }
    }

    private void triggerInteraction(int interactionID){
        switch(interactionID){
            case 0:
                break;
            case 1:
                //System.out.println("pressed f");
                break;
            case 2:
                break;
            default:
                assert false: "Error: interaction not defined";
        }
    }

}
