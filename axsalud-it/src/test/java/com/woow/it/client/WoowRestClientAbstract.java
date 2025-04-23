package com.woow.it.client;

import com.google.gson.Gson;

public class WoowRestClientAbstract {
    protected static final Gson gson = new Gson();
    protected final int serverPort;
    public WoowRestClientAbstract(final int serverPort) {
        this.serverPort = serverPort;
    }

}
