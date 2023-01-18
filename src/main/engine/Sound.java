package main.engine;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.system.libc.LibCStdlib.free;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Sound {
	
	private int bufferId;
	private int sourceId;
	private String filepath;
	
	private boolean isPlaying = false;
	
	public Sound(String filepath, boolean loops) {
		this.filepath = filepath;
		//allocate space to store return info
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);
		
		//use stb to load stuff
		ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(filepath, channelsBuffer, sampleRateBuffer);
		//check if the audio was successfully decoded/working
		if(rawAudioBuffer == null) {
			System.out.println("Couldn't load sound :" + filepath);
			stackPop();
			stackPop();
			return;
		}
		
		//retrieve extra information that was stored in buffers
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		//free memory
		stackPop();
		stackPop();
		
		//find correct openal format(mono or stereo)similar to RGB or RGBA with textures
		int format = -1;
		if(channels == 1) {
			format = AL_FORMAT_MONO16;
		}else if(channels == 2) {
			format = AL_FORMAT_STEREO16;
		}
		//create space to hold audio sample with openal
		bufferId = alGenBuffers();
		alBufferData(bufferId, format, rawAudioBuffer, sampleRate);
		
		//generate the sound source
		sourceId = alGenSources();
		alSourcei(sourceId, AL_BUFFER, bufferId);
		alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
		alSourcei(sourceId, AL_POSITION, 0);
		//3rd float parameter -------V   is sort of like volume
		alSourcef(sourceId, AL_GAIN, 0.3f);
		
		//free raw audio buffer / reset
		free(rawAudioBuffer);
		
	}
	//delete all sounds (like if there are completely new sounds for a different level or sm)
	public void delete() {
		alDeleteSources(sourceId);
		alDeleteBuffers(bufferId);
	}
	
	public void play() {
		//get status(playing ,stopped, etc)
		int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying = false;
			alSourcei(sourceId, AL_POSITION, 0);
		}
		if(!isPlaying) {
			alSourcePlay(sourceId);
			isPlaying = true;
		}
		
	}
	
	public void stop() {
		if(isPlaying) {
			alSourceStop(sourceId);
			isPlaying = false;
		}
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	public boolean isPlaying() {
		int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
		if(state == AL_STOPPED) {
			isPlaying = false;
					
		}
		return isPlaying;
	}
	
}
