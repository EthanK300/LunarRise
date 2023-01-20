package main.components;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class Interaction extends Component{

    private static transient GameObject player;
    private static Vector2f pos;
    private float range;
    private int keyCode;
    private int interactionID;
    private static List<Interaction> interactions = new ArrayList<>();

    public Interaction(float range, int keyCode, int interactionID){
        interactions.add(this);
        this.range = range;
        this.keyCode = keyCode;
        this.interactionID = interactionID;
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

        for(Interaction inter : interactions){

            //check if player in range
            if((diffX < inter.range) || (diffY < inter.range)){
                //display lines if in range, execute interaction if key pressed
                if(keyListener.isKeyPressed(inter.keyCode)){
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
                break;
            case 2:
                break;
            default:
                assert false: "Error: interaction not defined";
        }
    }

}
