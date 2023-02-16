package renderer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.FloatBuffer;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
public class Shader {

		private int shaderProgramID;
		private String vertexSource;
		private String fragmentSource;
		private String filepath;
		private boolean beingUsed = false;
		
		
		public Shader(String filepath) {
			this.filepath = filepath;
			try {
				String source = new String(Files.readAllBytes(Paths.get(filepath)));
				String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");
				
				//find first pattern after #type  "pattern"
				int index = source.indexOf("#type") + 6;
				int eol = source.indexOf("\r\n", index);
				String firstPattern = source.substring(index, eol).trim();
				
				//find second pattern after #type  "pattern"
				index = source.indexOf("#type", eol) + 6;
				eol = source.indexOf("\r\n", index);
				String secondPattern = source.substring(index, eol).trim();

				//if there's something wrong with it, throw IOException error
				if (firstPattern.equals("vertex")) {
					vertexSource = splitString[1];
				}else if(firstPattern.equals("fragment")) {
					fragmentSource = splitString[1];
				}else {
					throw new IOException("Unexpected token" + firstPattern);
				}
				
				//
				if (secondPattern.equals("vertex")) {
					vertexSource = splitString[2];
				}else if(secondPattern.equals("fragment")) {
					fragmentSource = splitString[2];
				}else {
					throw new IOException("Unexpected token" + secondPattern);
				}
				
			}catch(IOException e){
				e.printStackTrace();
				assert false: "Error: could not open file for shader:" +filepath;
			}			
		}
		
		public void compile() {
			//TODO:this is where an error originates - error 1282
			int vertexID, fragmentID;
			//compile and link shaders
			//first thing: load and compile the vertex shader
			vertexID = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
			//pass shader source code to gpu
			GL20.glShaderSource(vertexID,  vertexSource);
			GL20.glCompileShader(vertexID);
			//check for errors in compilation process
			int success = GL20.glGetShaderi(vertexID, GL20.GL_COMPILE_STATUS);
			if(success == GL20.GL_FALSE) {
				int len = GL20.glGetShaderi(vertexID, GL20.GL_INFO_LOG_LENGTH);
				System.err.println("ERROR: '"+filepath+"'\n\tVertex shader compilation failed.");
				System.out.println(GL20.glGetShaderInfoLog(vertexID, len));
				assert false : "";
			}
			
			//compile and link shaders
			//first thing: load and compile the fragment shader
			fragmentID = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
			//pass shader source code to gpu
			GL20.glShaderSource(fragmentID,  fragmentSource);
			GL20.glCompileShader(fragmentID);
			//check for errors in compilation process
			if(success == GL20.GL_FALSE) {
				int len = GL20.glGetShaderi(fragmentID, GL20.GL_INFO_LOG_LENGTH);
				System.err.println("ERROR: '"+filepath+"'\n\tFragment shader compilation failed.");
				System.out.println(GL20.glGetShaderInfoLog(fragmentID, len));
				assert false : "";
			}
			
			//link shaders and check for errors
			shaderProgramID = GL20.glCreateProgram();
			GL20.glAttachShader(shaderProgramID, vertexID);
			GL20.glAttachShader(shaderProgramID, fragmentID);
			GL20.glLinkProgram(shaderProgramID);
		
			//check for linking and attaching errors
			success = GL20.glGetProgrami(shaderProgramID,  GL20.GL_LINK_STATUS);
			GL20.glGetShaderInfoLog(shaderProgramID);
			if(success == GL20.GL_FALSE) {
				int len = GL20.glGetProgrami(shaderProgramID, GL20.GL_INFO_LOG_LENGTH);
				System.err.println("ERROR: '"+filepath+"'\n\tLinking shaders failed");
				//System.out.println(GL20.glGetProgramInfoLog(fragmentID, len));
				assert false : "ERROR: '"+filepath+"'\n\tLinking shaders failed";
			}
		}
		
		public void use() {
			if(!beingUsed) {
				//bind shader program
				GL20.glUseProgram(shaderProgramID);
				beingUsed = true;
			}

		}
		
		public void detach() {
			GL20.glUseProgram(0);
			beingUsed = false;
		}
		
		public void uploadMat4f(String varName, Matrix4f mat4) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
			mat4.get(matBuffer);
			GL20.glUniformMatrix4fv(varLocation, false, matBuffer);
		}
		
		public void uploadMat3f(String varName, Matrix3f mat3) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
			mat3.get(matBuffer);
			GL20.glUniformMatrix3fv(varLocation, false, matBuffer);
		}
		
		public void uploadVec4f(String varName, Vector4f vec) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform4f(varLocation, vec.x ,vec.y, vec.z, vec.w);
			
		}
		
		public void uploadFloat(String varName, float val) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform1f(varLocation, val);
		}
		
		public void uploadInt(String varName, int val) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform1i(varLocation, val);
		}
		
		public void uploadVec3f(String varName, Vector3f vec) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform3f(varLocation, vec.x, vec.y, vec.z);
		}
		
		public void uploadVec2f(String varName, Vector2f vec) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform2f(varLocation, vec.x, vec.y);
		}
		
		public void uploadTexture(String varName, int slot) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform1i(varLocation, slot);
		}
		
		public void uploadIntArray(String varName, int[] array) {
			int varLocation = GL20.glGetUniformLocation(shaderProgramID, varName);
			use();
			GL20.glUniform1iv(varLocation, array);
		}

}
