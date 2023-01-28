package main.components;

import main.engine.GameObject;
import main.engine.Window;
import main.engine.keyListener;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

public class destroyer extends Interaction {
    private static GameObject player;
    private static Vector2f pos;
    private Vector2f range;
    private Vector2f dimensions;
    private Vector3f color;
    private int keyCode;
    private int interactionID;
    private float debounce;

    @Override
    public void selfStart() {
        this.player = Window.getScene().getGameObjectWith(PlayerController.class);
    }

    public destroyer(Vector2f range, Vector2f dimensions, int keyCode, int interactionID, Vector3f color) {
        this.range = range;
        this.keyCode = keyCode;
        this.interactionID = interactionID;
        this.dimensions = dimensions;
        this.color = color;

    }

    @Override
    public void selfUpdate(float dt) {
        debounce -= 0.08f;

        //calculate how close the player is
        pos = this.gameObject.transform.position;
        float diffX = Math.abs(pos.x - player.transform.position.x);
        float diffY = Math.abs(pos.y - player.transform.position.y);
        //System.out.println(diffX + "," + diffY);


        //System.out.println(interactions);
        //check if player in range
        if ((diffX < this.range.x) && (diffY < this.range.y)) {
            //System.out.println(diffX + "," + diffY);
            DebugDraw.addBox2D(pos, new Vector2f(dimensions.x / 4, dimensions.y / 4), 0.0f, color);
            //display lines if in range, execute interaction if key pressed
            if (keyListener.isKeyPressed(this.keyCode) && debounce <= 0) {
                //System.out.println("inside inter");
                //trigger appropriate interaction
                triggerInteraction(this.interactionID);
                debounce = 1.0f;
            }
        }
    }
    private void triggerInteraction(int interactionID) {
        switch (interactionID) {
            case 0:
                this.gameObject.destroy();
                System.out.println("activated 2");
                break;
            case 1:
                System.out.println("activated 1");
                break;
            case 2:
                break;
            default:
                assert false : "Error: interaction not defined";
        }
    }
}