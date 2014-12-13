package ee.ut.algorithmics.collage.maker.exceptions;

public class ClusteringNotRunException extends Exception{

	private static final long serialVersionUID = 1L;

	public ClusteringNotRunException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

	public ClusteringNotRunException(String arg0) {
		super(arg0);
	}
	
	

}
