package main.engine;

import java.lang.reflect.Type;

import com.google.gson.*;

import main.components.Component;

public class ItemDeserializer implements JsonSerializer<Item>, JsonDeserializer<Item>{

    @Override
    public Item deserialize(JsonElement json, Type TypeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        JsonElement element= jsonObject.get("properties");
        JsonArray components = jsonObject.getAsJsonArray("components");
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Item.class, Item.class.getClasses());
        try {
            Item it = (Item)(Class.forName(type)).getDeclaredConstructor().newInstance();
            
            for (JsonElement e : components) {
                Component c = context.deserialize(e, Component.class);
                it.addComponent(c);
            }
            it.transform = it.getComponent(Transform.class);
            return it;
        }catch(Exception e){
            throw new JsonParseException("Unknown element type: " + type, e);
        }
        /**
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
        */

    }

    @Override
    public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();

        result.add("type", new JsonPrimitive(src.getClass().getCanonicalName()));
        result.add("properties", context.serialize(src, src.getClass()));
        return result;
    }
}
