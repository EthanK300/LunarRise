package main.engine;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import imgui.ImGui;
import main.components.Component;
import main.components.SpriteRenderer;
import main.util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public abstract class Item{
    private static int ID_COUNTER = 0;
    private int uid = -1;
    private boolean doSerialization = true;
    public String name;
    private List<Component> components;
    public transient Transform transform;
    private boolean isDead = false;
    public Item(String name){
        this.name = name;
        this.components = new ArrayList<>();
        this.uid = ID_COUNTER++;
    }
    public <T extends Component> T getComponent(Class<T> componentClass){
        for(Component c : components){
            if(componentClass.isAssignableFrom(c.getClass())){
                try{
                    return componentClass.cast(c);
                }catch(ClassCastException e){
                    e.printStackTrace();
                    assert false: "Error casting item component";
                }
            }
        }
        return null;
    }

    public <T extends Component> void removeComponent(Class<T> componentClass){
        for(int i=0; i < components.size(); i++){
            Component c = components.get(i);
            if(componentClass.isAssignableFrom(c.getClass())){
                components.remove(i);
                return;
            }
        }
    }

    public void addComponent(Component c){
        c.generateId();
        this.components.add(c);
        c.item = this;
    }

    public void update(float dt){
        for(int i=0; i < components.size(); i++){
            components.get(i).update(dt);
        }
    }

    public void editorUpdate(float dt){
        for(int i=0; i < components.size(); i++){
            components.get(i).editorUpdate(dt);
        }
    }

    public void imGui(){
        for(Component c : components){
            if(ImGui.collapsingHeader(c.getClass().getSimpleName())){
                c.imgui();
            }
        }
    }
    public void start(){
        for(int i=0; i < components.size(); i++){
            components.get(i).start();
        }
    }
    public void destroy(){
        this.isDead = true;
        for(int i=0; i < components.size(); i++){
            components.get(i).destroy();
        }
    }
    public Item copy() {
        Gson gson  = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(Item.class , new ItemDeserializer())
                .enableComplexMapKeySerialization()
                .create();
        String objAsJson = gson.toJson(this);

        Item it = gson.fromJson(objAsJson, Item.class);
        it.generateUid();
        for(Component c : it.getAllComponents()) {
            c.generateId();
        }

        SpriteRenderer sprite = it.getComponent(SpriteRenderer.class);
        if(sprite != null && sprite.getTexture() != null) {
            sprite.setTexture(AssetPool.getTexture(sprite.getTexture().getFilepath()));
        }

        return it;
    }
    public static void init(int maxId) {
        ID_COUNTER = maxId;
    }

    public int getUid() {
        return this.uid;
    }

    public List<Component> getAllComponents(){
        return this.components;
    }

    public void setNoSerialize() {
        this.doSerialization = false;
    }

    public void generateUid() {
        this.uid = ID_COUNTER++;
    }

    public boolean doSerialization() {
        return this.doSerialization;
    }

    public boolean isDead() {
        return this.isDead;
    }
    
}
