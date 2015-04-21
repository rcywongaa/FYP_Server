import java.util.Arrays;

public class DataSet {
	private final float EPSILON = 0.99f;
	private float[] angles = new float[MainActivity.SENSORCOUNT * 3];
	private boolean[] taps = new boolean[MainActivity.SENSORCOUNT - 1]; // Palm doesn't tap
	private float[] palmPos = new float[2];

	public DataSet() {
		Arrays.fill(angles, 0.0f);
		Arrays.fill(taps, false);
		Arrays.fill(palmPos, 0.0f);
	}

	public void update(float[] data) {
		assert data.length == MainActivity.DATALENGTH : "DataSet() bad input!";
		for (int i = 0; i < MainActivity.SENSORCOUNT; i++) {
			angles[i * 3] = data[i * 3];
			angles[i * 3 + 1] = data[i * 3 + 1];
			angles[i * 3 + 2] = data[i * 3 + 2];
			if (i < MainActivity.SENSORCOUNT - 1)
				taps[i] = data[MainActivity.SENSORCOUNT * 3 + i] > EPSILON;
		}
		if (MainActivity.HASMOUSE) {
			palmPos[0] = data[MainActivity.DATALENGTH - 2];
			palmPos[1] = data[MainActivity.DATALENGTH - 1];
		}
	}
	
	public DataSet copy(){
		DataSet dest = new DataSet();
		System.arraycopy(angles, 0, dest.angles, 0, angles.length);
		System.arraycopy(taps, 0, dest.taps, 0, taps.length);
		System.arraycopy(palmPos, 0, dest.palmPos, 0, palmPos.length);
		return dest;
	}

	public float[] getAngles() {
		return angles;
	}

	public boolean[] getTaps() {
		return taps;
	}

	public float[] getPalmPos() {
		return palmPos;
	}

	public float radToDeg(float rad) {
		return (float) (rad * 180.0f / Math.PI);
	}

	public float degToRad(float deg) {
		return (float) (deg * Math.PI / 180.0f);
	}
}