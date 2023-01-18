package physics2d.components;

import org.joml.Vector2f;

import main.components.Component;
import renderer.DebugDraw;

public class CircleCollider extends Component{
	private float radius = 1f;
	protected Vector2f offset = new Vector2f();
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}
	public Vector2f getOffset() {
		return this.offset;
	}
	
	public void setOffset(Vector2f newOffset) {
		this.offset.set(newOffset);
	}
	
	@Override
	public void editorUpdate(float dt) {
		Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.offset);
		DebugDraw.addCircle(center, this.radius);
	}
}
