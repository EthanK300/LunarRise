package main.components;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class RigidBody extends Component{
	@SuppressWarnings("unused")
	private int colliderType = 0;
	@SuppressWarnings("unused")
	private float friction = 0.8f;
	public Vector3f velocity = new Vector3f(0, 0.5f, 0);
	public transient Vector4f tmp = new Vector4f(0,0,0,0);
}
