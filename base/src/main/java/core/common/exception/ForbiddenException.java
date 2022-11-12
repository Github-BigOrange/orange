package core.common.exception;

/**
 * 没有权限异常
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}