package sim;

public class ProgressBar {

	private int size;
	private double simLength;
	private char covered='+';
	private int currentProgress;
	private boolean first = true;
	
	public ProgressBar(int size, double simLength) {
		this.size = size;
		this.simLength = simLength;
	}
	
	public void display(double currentTime) {	
		int progress = (int) Math.floor(size*currentTime/(1.0*simLength));
		if (progress > currentProgress || first) {
			StringBuilder bar = new StringBuilder(size+2);
			bar.append('[');
			for (int i=0; i<=size; i++){
				if (i<=progress){
					bar.append(covered);
				} else {
					bar.append(' ');
				}
			}
			bar.append(']');
			System.out.println(bar);
			currentProgress = progress;
			first = false;
		}
	}
}
