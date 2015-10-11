/** This file is part of MakamBox.

    MakamBox is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    MakamBox is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with MakamBox.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * MakamBox is an implementation of MakamToolBox Turkish Makam music analysis tool which is developed by Baris Bozkurt
 * This is the project of music tuition system that is a tool that helps users to improve their skills to perform music. 
 * This thesis aims at developing a computer based interactive tuition system specifically for Turkish music. 
 * 
 * Designed and implemented by @author Bilge Mirac Atici
 * Supervised by @supervisor Baris Bozkurt
 * Bahcesehir University, 2014-2015
 */

package backEnd;
/**
 * The main f0 Pitch track estimation class. 
 * It can be used with wave file object
 * It uses the YIN implementation of TarsosDSP by Joren Six
 * 
 * Due to implementation of YIN, we need to split float array to chunk with buffer size. 
 * This buffer size is calculated from sample rate (40 msec window size)
 * It also create pitch file folder in project folder and write pitch result array to text file
 * 
 * Created @author mirac
 * Bahcesehir University, 2014
 * 
 */

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;

import utilities.AudioUtilities;


public class PitchDetection {
	private Wavefile audio;
	private Yin yin;
	private String name;
	private int bufferSize;
	private float sampleRate;
	private float[] floatData,pitchResult;
	
	public PitchDetection(Wavefile afs) throws Exception{
		audio = afs ;
		name = audio.getName();
		sampleRate = audio.getSampleRate();
		bufferSize = (int) Math.round(sampleRate*0.04);
		floatData = audio.getFloatData().clone();
		estimate();
	}
	
	public PitchDetection(float[] audioFloatData,float samplerate) {
		audio = null;
		name = null;
		sampleRate = samplerate;
		bufferSize = (int) Math.round(sampleRate*0.04);
		floatData = audioFloatData;
		estimate();
	}
	
	public void estimate(){
		float[][] chunked = AudioUtilities.chunkArray(floatData,bufferSize);
		yin = new Yin(sampleRate,bufferSize,0.1);
		pitchResult = new float[chunked.length];
		for (int i=0; i<pitchResult.length;++i){
			float r = yin.getPitch(chunked[i]).getPitch();
			if(r==-1){
				pitchResult[i] = 0;
			}
			else{
				pitchResult[i] = r;
			}
		}
	}
	public PitchDetection(File f) throws Exception{
		this(new Wavefile(f));
	}
	public float[] getPitchResult() {
		return pitchResult;
	}
	public Wavefile getAudio() {
		return audio;
	}
	public int getBufferSize() {
		return bufferSize;
	}
	public String getName() {
		return name;
	}
	public float[][] chunkPitchTrack(float[] pitchTrack){
		
		float bottom_limit = 0.7f; float upper_limit = 1.3f;
		ArrayList<ArrayList<Float>> tempChunk = new ArrayList<ArrayList<Float>>();
		int index = 0;
		for (int i = 0; i < pitchTrack.length; i++) {
			if (pitchTrack[i] !=0 && pitchTrack[i+1]!=0){
				float interval = pitchTrack[i+1]/pitchTrack[i];
				if (bottom_limit<interval && interval<upper_limit){
					tempChunk.get(index).add(pitchTrack[i]);
				} else {
					index++;
					tempChunk.get(index).add(pitchTrack[i]);
				}
			}
		}
		
		float[][] chunkedPitch = new float[tempChunk.size()][];
		for (int i = 0; i < chunkedPitch.length; i++) {
			chunkedPitch[i] = ArrayUtils.toPrimitive(tempChunk.get(i).toArray(new Float[tempChunk.get(i).size()]));
		}
		return chunkedPitch; 			
	}

}
