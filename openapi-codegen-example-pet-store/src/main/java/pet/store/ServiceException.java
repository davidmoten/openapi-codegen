package pet.store;

public final class ServiceException extends Exception {

    private static final long serialVersionUID = -4115693210890378905L;
    
    private final int statusCode;

    public ServiceException(int statusCode, String message) {
        super("statusCode=" + statusCode + ", " + message);
        this.statusCode = statusCode;
    }
    
    public ServiceException(int statusCode, Throwable e) {
        super(e);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
    
}
