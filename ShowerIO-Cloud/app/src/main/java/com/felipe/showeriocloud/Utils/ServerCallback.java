package com.felipe.showeriocloud.Utils;

public interface ServerCallback {

    void onServerCallback(Boolean status, String response);

    void onServerCallbackObject(Boolean status, String response, Object object);

}
