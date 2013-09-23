package sim;

public class ProgressBar {

	private int size;
	private double taskLength;
	private char covered='+';
	private int currentBarProgress=0;
	private double taskProgress=0.0;
	private boolean first = true;
	
	public ProgressBar(int size, double taskLength) {
		this.size = size;
		this.taskLength = taskLength;
	}
		
	synchronized public void setProgress(double progress){
		taskProgress += taskProgress + progress;
	}
	
	synchronized public void addOneUnitOfProgress(){
		taskProgress++;
	}
	
	public void display() {	
		int barProgress = (int) Math.floor(size*taskProgress/(1.0*taskLength));
		if (barProgress > currentBarProgress || first) {
			StringBuilder bar = new StringBuilder(size+2);
			bar.append('[');
			for (int i=0; i<=size; i++){
				if (i<=barProgress){
					bar.append(covered);
				} else {
					bar.append(' ');
				}
			}
			bar.append(']');
			System.out.println(bar);
			currentBarProgress = barProgress;
			first = false;
		}
	}
}
