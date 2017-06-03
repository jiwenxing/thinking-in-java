/**
 * @author jverson
 *
 */
package chapter5;

class Book{
	boolean checkout = false;
	
	Book(boolean checkout) {
		this.checkout = checkout;
	}
	
	void checkin(){
		this.checkout = false;
	}
	
    @Override
	protected void finalize() throws Throwable{
		if (checkout) {
			System.out.println("error: this book has been checked out!");
			super.finalize();
		}
	}
}