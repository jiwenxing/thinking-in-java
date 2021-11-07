/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter9;

/**
 * @author jverson
 *
 */
public class Music {
	static void tune(Instrument i){
		i.play();
	}
	
	static void tuneAll(Instrument[] e){
		for (Instrument instrument : e) {
			tune(instrument);
		}
	}
	public static void main(String[] args) {
		tuneAll(new Instrument[]{
				new Wind(),
				new Percussion()
		});
	}
}

abstract class Instrument{
	public abstract void play(); 
	public String waht() {
		return "instrument";
	}	
}

class Wind extends Instrument{
	@Override
	public void play() {
		System.out.println("Wind.play()");
	}
	@Override
	public String waht() {
		return "wind";
	}
}

class Percussion extends Instrument{
	@Override
	public void play() {
		System.out.println("Percussion.play()");
	}
	@Override
	public String waht() {
		return "percussion";
	}
}