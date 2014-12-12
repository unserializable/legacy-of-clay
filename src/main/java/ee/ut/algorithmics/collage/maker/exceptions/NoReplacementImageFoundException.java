package ee.ut.algorithmics.collage.maker.exceptions;

public class NoReplacementImageFoundException extends Exception{

	private static final long serialVersionUID = 1L;

	public NoReplacementImageFoundException(String arg0) {
		super(arg0);
	}
	
	public NoReplacementImageFoundException(){
		super();
	}
}
