/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter9;

/**
 * @author jverson
 *
 */
public interface Instrument2 {
    default void play(){
    	System.out.println("Instrument2.play()");
    }
    //接口的默认方法
	default String what() {
		return "instrument";
	}
	//使用内部类进行测试
	static class Test implements Instrument2{
		static void tune(Instrument2 i){
			i.play();
		}
		static void tuneAll(Instrument2[] e){
			for (Instrument2 instrument : e) {
				tune(instrument);
			}
		}
		public static void main(String[] args) {
			tuneAll(new Instrument2[]{
					new Wind2(),
					new Percussion2()
			});
		}
		
		@Override
		public void play() {}
	}
}

class Wind2 implements Instrument2{
	@Override
	public void play() {
		System.out.println("Wind2.play()");
	}
}

class Percussion2 implements Instrument2{
	@Override
	public String what() {
		return "percussion2";
	}
}