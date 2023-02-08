package main.items;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class testItem extends Item{
    public testItem(Vector2f position, String name, Vector2f dimensions, Vector3f color, boolean startInScene){
        this.name = name;
        this.pos = position;
        this.dimensions = dimensions;
        this.BoxColor = color;
        this.inScene = startInScene;
    }

    @Override
    public void selfUpdate(float dt) {
        //update
    }

    @Override
    public void selfStart() {
        //start
    }
}
