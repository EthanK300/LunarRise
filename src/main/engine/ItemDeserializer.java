package main.engine;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import main.components.Component;

public class ItemDeserializer implements JsonDeserializer<Item>{

    @Override
    public Item deserialize(JsonElement json, Type TypeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        Item item = jsonObject.get("Item").getAsString();
        JsonArray components = jsonObject.getAsJsonArray("components");

        Item it = new Item(name, item);
        for(JsonElement e : components) {
            Component c = context.deserialize(e, Component.class);
            it.addComponent(c);
        }
        it.transform = it.getComponent(Transform.class);
        return it;
    }

}
