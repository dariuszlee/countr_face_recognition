package countr.common;

import java.io.Serializable;

public class ServerResult implements Serializable {
    final boolean success;
    final String message;

    public ServerResult(boolean success) {
        this.success = success;
        this.message = "";
    }

    public ServerResult(boolean success, String message){
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "ServerResult [message=" + message + ", success=" + success + "]";
    }

    public String getMessage() {
        return message;
    }    
}
