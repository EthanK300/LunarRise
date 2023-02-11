package main.items;

import main.engine.Window;
import main.engine.keyListener;
import main.player.PlayerController;
import org.joml.Vector2f;
import org.joml.Vector3f;
import renderer.DebugDraw;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;

public class testItem extends Item{
    public testItem(String name, Vector2f dimensions, Vector3f color, boolean startInScene){
        this.name = name;
        this.dimensions = dimensions;
        this.BoxColor = color;
        this.inScene = startInScene;
    }

    @Override
    public void selfUpdate(float dt) {
        //update

        debounce -= 0.08f;
        //calculate how close the player is
        pos = this.gameObject.transform.position;
        float diffX = Math.abs(pos.x - player.transform.position.x);
        float diffY = Math.abs(pos.y - player.transform.position.y);
        if(!inScene){
            this.gameObject.transform.position = new Vector2f((float)Double.POSITIVE_INFINITY, (float)Double.POSITIVE_INFINITY);
            return;
        }
        //check if player in range
        if ((diffX < this.range.x) && (diffY < this.range.y) && inScene) {
            DebugDraw.addBox2D(pos, new Vector2f(dimensions.x / 4, dimensions.y / 4), 0.0f, BoxColor);
            //display lines if in range, execute interaction if key pressed
            if (keyListener.isKeyPressed(GLFW_KEY_F) && debounce <= 0) {
                debounce = 1.0f;
                if(PC.addItemInv(this)){
                    //success!
                    this.gameObject.transform.position = new Vector2f((float)Double.POSITIVE_INFINITY, (float)Double.POSITIVE_INFINITY);
                    System.out.println("added item");
                }
                System.out.println("pressed f");
                //trigger appropriate interaction
            }
        }
    }

    @Override
    public void selfStart() {
        if(!inScene){
            this.gameObject.transform.position = new Vector2f((float)Double.POSITIVE_INFINITY, (float)Double.POSITIVE_INFINITY);
        }

    }
}
