package countr.common;

import java.io.Serializable;

public class ServerResult implements Serializable {
    final boolean success;

    public ServerResult(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "ServerResult [success=" + success + "]";
    }    
}
