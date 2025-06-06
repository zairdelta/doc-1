package com.woow.cache.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WoowCacheServiceImpl {
    private final Map<String, String> sessionIdToUser = new ConcurrentHashMap<>();


}
