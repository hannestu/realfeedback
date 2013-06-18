package wrapper;

public class ProjectInvalidException extends Exception {
	private static final long serialVersionUID = 7526472295622776147L;
	
	public ProjectInvalidException() { super(); }
	  public ProjectInvalidException(String message) { super(message); }
	  public ProjectInvalidException(String message, Throwable cause) { super(message, cause); }
	  public ProjectInvalidException(Throwable cause) { super(cause); }

}
