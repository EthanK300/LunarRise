package main.components;

import org.joml.Vector2f;
import org.joml.Vector4f;

import editor.eImGui;
import main.engine.Transform;
import renderer.Texture;

public class SpriteRenderer extends Component{
	
	private Vector4f color = new Vector4f(1,1,1,1);
	private Sprite sprite = new Sprite();

	private transient Transform lastTransform;
	private transient boolean isDirty = true;
	private transient boolean reAdd = false;
	
	@Override
	public void start() {
		this.lastTransform = gameObject.transform.copy();
	}
	
	@Override
	public void update(float dt) {
		if(!this.lastTransform.equals(this.gameObject.transform)) {
			this.gameObject.transform.copy(this.lastTransform);
			isDirty = true;
		}
	}
	
	@Override
	public void editorUpdate(float dt) {
		if(!this.lastTransform.equals(this.gameObject.transform)) {
			this.gameObject.transform.copy(this.lastTransform);
			isDirty = true;
		}
	}
	
	@Override
	public void imgui() {
		if(eImGui.colorPicker4("Color Picker", this.color)) {
			this.isDirty = true;
		}
	}
	
	public void setDirty() {
		this.isDirty = true;
	}
	
	public Vector4f getColor() {
		return this.color;
	}
	
	public Texture getTexture() {
		return sprite.getTexture();
	}
	
	public Vector2f[] getTexCoords() {
		return sprite.getTexCoords();
	}
	
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
		this.isDirty = true;
	}
	
	public void setColor(Vector4f color1) {
		if(!this.color.equals(color1)) {
			this.isDirty = true;
			this.color.set(color);
		}
	}
	
	public boolean isDirty() {
		return this.isDirty;
	}
	
	public void setClean() {
		this.isDirty = false;
	}
	
	public void setReAdd() {
		this.reAdd = true;
	}
	
	public boolean reAdd() {
		return this.reAdd;
	}
	
	public void setTexture(Texture texture) {
		this.sprite.setTexture(texture);
	}
	
}
