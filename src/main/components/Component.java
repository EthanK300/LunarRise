package main.components;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import editor.eImGui;
import imgui.ImGui;
import imgui.type.ImInt;
import main.engine.GameObject;

public abstract class Component {
	
	private static int ID_COUNTER = 0;
	private int uid = -1;
	
	public transient GameObject gameObject = null;
	
	public void start() {
		//initialization method
	}
	
	public void update(float dt) {
		//update on each cycle
	}
	
	public void editorUpdate(float dt) {
		//update the editor window each cycle
	}
	
	public void beginCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
		//which object am I colliding with?
		//
		//which direction did we hit it from
	}
	
	public void endCollision(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
		//which object am I colliding with?
		//
		//which direction did we hit it from
	}
	
	public void preSolve(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
		//useful for like jumping up through a block but not falling back through it
		
	}

	public void postSolve(GameObject collidingObject, Contact contact, Vector2f hitNormal) {
		
	}

	public void imgui() {
		try {
			Field[] fields = this.getClass().getDeclaredFields();
			for(Field field : fields) {
				
				boolean isTransient = Modifier.isTransient(field.getModifiers());
				if(isTransient) {
					continue;
				}
				
				boolean isPrivate = Modifier.isPrivate(field.getModifiers());
				if(isPrivate) {
					field.setAccessible(true);
				}
				
				Class type = field.getType();
				Object value = field.get(this);
				String name = field.getName();
				
				if(type == int.class) {
					int val = (int)value;
					field.set(this,  eImGui.dragInt(name,  val));
				}else if(type == float.class) {
					float val = (float)value;
					field.set(this,  eImGui.dragFloat(name,  val));
				}else if(type == boolean.class) {
					boolean val = (boolean)value;
					if(ImGui.checkbox(name + ";", val)) {
						field.set(this, !val);
					}
				}else if(type == Vector2f.class) {
					Vector2f val = (Vector2f)value;
					eImGui.drawVec2Control(name, val);
				}else if(type == Vector3f.class) {
					Vector3f val = (Vector3f)value;
					float[] imVec = {val.x, val.y, val.z};
					if(ImGui.dragFloat3(name + ";", imVec)) {
						val.set(imVec[0], imVec[1], imVec[2]);
					}
				}else if(type == Vector4f.class) {
					Vector4f val = (Vector4f)value;
					float[] imVec = {val.x, val.y, val.z, val.w};
					if(ImGui.dragFloat4(name + ";", imVec)) {
						val.set(imVec[0], imVec[1], imVec[2], imVec[3]);
					}
				}else if(type.isEnum()) {
					String[] enumValues = getEnumValues(type);
					String enumType = ((Enum)value).name();
					ImInt index = new ImInt(indexOf(enumType, enumValues));
					if(ImGui.combo(field.getName(),  index, enumValues, enumValues.length)) {
						field.set(this,  type.getEnumConstants()[index.get()]);
					}
				}
				
				if(isPrivate) {
					field.setAccessible(false);
				}
				
			}
		}catch(IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void generateId() {
		if(this.uid == -1) {
			this.uid = ID_COUNTER++;
		}
	}
	
	private <T extends Enum<T>> String[] getEnumValues(Class<T> enumType) {
		String[] enumValues = new String[enumType.getEnumConstants().length];
		int i=0;
		for(T enumIntegerValue : enumType.getEnumConstants()) {
			enumValues[i] = enumIntegerValue.name();
			i++;
		}
		return enumValues;
	}
	
	private int indexOf(String str, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			if(str.equals(arr[i])) {
				return i;
			}
		}
		
		return -1;
	}
	
	//what to do with the dead game object
	public void destroy() {
		
	}
	
	public int getUid() {
		return this.uid;
	}
	
	public static void init(int maxId) {
		ID_COUNTER = maxId;
	}
	
}
