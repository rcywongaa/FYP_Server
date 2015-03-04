import java.util.ArrayList;

/*
 * TODO: Consider using hardware to detect tap
 */
public class TapDetector {
	private final int DURATION = 15; //ms
	private int sampleSize = 0;
	private ArrayList<float[]> samples = new ArrayList<float[]>();
	
	public TapDetector(int rate){
		sampleSize = DURATION / rate;
	}
	
	public boolean check(float[] data){
		assert data.length == 3 : "check() bad input!";
		if (samples.size() < sampleSize){
			samples.add(data);
			return false;
		}
		return true;
	}
}
