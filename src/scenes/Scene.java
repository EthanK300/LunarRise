package scenes;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import main.engine.*;
import main.player.PlayerController;
import org.joml.Vector2f;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import main.components.Component;
import physics2d.Physics2D;
import renderer.Renderer;

public class Scene {

	private Renderer renderer;
	private Camera camera;
	private boolean isRunning;
	private List<GameObject> gameObjects;
	private List<GameObject> pendingObjects;
	private List<Item> items;
	private List<Item> pendingItems;
	private Physics2D physics2D;

	private SceneInitializer sceneInitializer;

	public Scene(SceneInitializer sceneInitializer) {
		this.sceneInitializer = sceneInitializer;
		this.physics2D = new Physics2D();
		this.renderer = new Renderer();
		this.gameObjects = new ArrayList<>();
		this.pendingObjects = new ArrayList<>();
		this.isRunning = false;
		this.items = new ArrayList<>();
		this.pendingItems = new ArrayList<>();
	}

	public Physics2D getPhysics() {
		return this.physics2D;
	}

	public void init() {
		this.camera = new Camera(new Vector2f(0,0));
		this.sceneInitializer.loadResources(this);
		this.sceneInitializer.init(this);
	}

	public void start() {
		for(int i=0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.start();
			this.renderer.add(go);
			this.physics2D.add(go);
		}
		for(int i=0; i < items.size(); i++){
			Item it = items.get(i);
			it.start();
			this.renderer.add(it);
		}
		isRunning = true;
	}

	public void addGameObjectToScene(GameObject go) {
		if(!isRunning) {
			gameObjects.add(go);
		}else {
			pendingObjects.add(go);
		}
	}
	public void addItemToScene(Item it) {
		if(!isRunning) {
			items.add(it);
		}else {
			pendingItems.add(it);
		}
	}

	public void destroy() {
		for(GameObject go : gameObjects) {
			go.destroy();
		}
		for(Item it : items) {
			it.destroy();
		}
	}

	public <T extends Component> GameObject getGameObjectWith(Class<T> correctClass) {
		for(GameObject go : gameObjects) {
			if(go.getComponent(correctClass) != null) {
				return go;
			}
		}

		return null;
	}
	public <T extends Component> Item getItemWith(Class<T> correctClass) {
		for(Item it : items) {
			if(it.getComponent(correctClass) != null) {
				return it;
			}
		}

		return null;
	}

	public List<GameObject> getGameObjects(){
		return this.gameObjects;
	}
	public List<Item> getItems(){
		return this.items;
	}

	public GameObject getGameObject(int gameObjectId) {
		Optional<GameObject> result = this.gameObjects.stream()
				.filter(gameObject -> gameObject.getUid() == gameObjectId).findFirst();
		return result.orElse(null);
	}
	public Item getItem(int itemId) {
		Optional<Item> result = this.items.stream()
				.filter(item -> item.getUid() == itemId).findFirst();
		return result.orElse(null);
	}

	public void editorUpdate(float dt) {

		this.camera.adjustProjection();

		for(int i=0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.editorUpdate(dt);
			if(go.isDead()) {
				gameObjects.remove(i);
				this.renderer.destroyGameObject(go);
				this.physics2D.destroyGameObject(go);
				i--;
			}

			for(GameObject obj : pendingObjects) {
				gameObjects.add(obj);
				obj.start();
				this.renderer.add(obj);
				this.physics2D.add(obj);
			}
			pendingObjects.clear();
		}
		for(int i=0; i < items.size(); i++) {
			Item it = items.get(i);
			it.editorUpdate(dt);
			if(it.isDead()) {
				items.remove(i);
				this.renderer.destroyItem(it);
				i--;
			}

			for(Item item : pendingItems) {
				items.add(item);
				item.start();
				this.renderer.add(item);
			}
			pendingItems.clear();
		}
	}

	public void update(float dt) {
		this.camera.adjustProjection();
		this.physics2D.update(dt);
		//System.out.println("fps: "+ 1/dt); //fps monitor
		for(int i=0; i < gameObjects.size(); i++) {
			GameObject go = gameObjects.get(i);
			go.update(dt);

			if(go.isDead()) {
				gameObjects.remove(i);
				this.renderer.destroyGameObject(go);
				this.physics2D.destroyGameObject(go);
				i--;
			}
		}
		for(int i=0; i < items.size(); i++){
			Item it = items.get(i);
			it.update(dt);

			if(it.isDead()){
				items.remove(i);
				this.renderer.destroyItem(it);
				i--;
			}
		}

		for(GameObject obj : pendingObjects) {
			gameObjects.add(obj);
			obj.start();
			this.renderer.add(obj);
			this.physics2D.add(obj);
		}
		for(Item item : pendingItems) {
			items.add(item);
			item.start();
			this.renderer.add(item);
		}
		pendingObjects.clear();
		pendingItems.clear();

	}

	public void render() {
		this.renderer.render();
	}

	public Camera camera() {
		return this.camera;
	}

	public void imgui() {
		this.sceneInitializer.imgui();
	}

	public GameObject createGameObject(String name) {
		GameObject go = new GameObject(name);
		go.addComponent(new Transform());
		go.transform = go.getComponent(Transform.class);
		return go;
	}
	public Item createItem(String name) {
		try {
			Item it = (Item)Class.forName(name).getDeclaredConstructor().newInstance();
			attach(it);
			return it;
			/**
			 * switch(itemIndex){
			 * 				case 0:
			 *
			 * 				case 1:
			 * 					//add cases for
			 * 				default:
			 * 					assert false: "Could not load specific item class";
			 * 					return null;
			 *                        }
			 */

		}catch(Exception e){
			e.printStackTrace();
			assert false: "Error creating item instance object";
			return null;
		}
	}
	private Item attach(Item it){
		it.addComponent(new Transform());
		it.transform = it.getComponent(Transform.class);
		return it;
	}

	
	public void save() {
		Gson gson  = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Component.class, new ComponentDeserializer())
				.registerTypeAdapter(GameObject.class , new GameObjectDeserializer())
				.registerTypeAdapter(Item.class, new ItemDeserializer())
				.enableComplexMapKeySerialization()
				.create();
		
		try {
			FileWriter writer = new FileWriter("level.txt");
			List<GameObject> objsToSerialize = new ArrayList<>();
			List<Item> itemsToSerialize = new ArrayList<>();
			for(GameObject obj : this.gameObjects) {
				if(obj.doSerialization()) {
					objsToSerialize.add(obj);
				}
			}
			for(Item item : this.items){
				if(item.doSerialization()){
					itemsToSerialize.add(item);
				}
			}
			writer.write(gson.toJson(objsToSerialize));
			writer.write(gson.toJson(itemsToSerialize));
			writer.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		Gson gson  = new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(Component.class, new ComponentDeserializer())
				.registerTypeAdapter(GameObject.class , new GameObjectDeserializer())
				.registerTypeAdapter(Item.class, new ItemDeserializer())
				.enableComplexMapKeySerialization()
				.create();
		String inFile = "";
		try {
			inFile = new String(Files.readAllBytes(Paths.get("level.txt")));
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		if(!inFile.equals("")) {
			
			int maxGoId = -1;
			int maxCompId = -1;
			
			GameObject[] objs = gson.fromJson(inFile, GameObject[].class);
			for(int i=0; i < objs.length; i++) {
				addGameObjectToScene(objs[i]);
				
				for(Component c : objs[i].getAllComponents()) {
					if(c.getUid() > maxCompId) {
						maxCompId = c.getUid();
					}
				}
				if(objs[i].getUid() > maxGoId) {
					maxGoId = objs[i].getUid();
				}
			}
			Item[] items = gson.fromJson(inFile, Item[].class);
			for(int i=0; i < items.length; i++){
				addItemToScene(items[i]);

				for(Component c : items[i].getAllComponents()){
					if(c.getUid() > maxCompId){
						maxCompId = c.getUid();
					}
				}
				if(items[i].getUid() > maxGoId) {
					maxGoId = items[i].getUid();
				}
			}
			
			maxGoId++;
			maxCompId++;
			//System.out.println(maxGoId);
			//System.out.println(maxCompId);
			GameObject.init(maxGoId);
			Component.init(maxCompId);
			Item.init(maxCompId);
		}
	}
	
}
