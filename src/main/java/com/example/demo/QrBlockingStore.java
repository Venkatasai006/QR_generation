package com.example.demo;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
public class QrBlockingStore {

    private final Map<String, CountDownLatch> latchMap = new ConcurrentHashMap<>();
    private final Map<String, String> accessTokenMap = new ConcurrentHashMap<>();

    public String createToken() {
        String token = UUID.randomUUID().toString();
        latchMap.put(token, new CountDownLatch(1));
        return token;
    }

    public void approve(String token, String accessToken) {
        accessTokenMap.put(token, accessToken);

        CountDownLatch latch = latchMap.get(token);
        if (latch != null) {
            latch.countDown();
        }
    }

    public String waitForAccessToken(String token) throws InterruptedException {

        CountDownLatch latch = latchMap.get(token);
        if (latch == null) return null;

        boolean success = latch.await(120, TimeUnit.SECONDS);
        if (!success) {
            latchMap.remove(token);
            return null;
        }
        String accessToken = accessTokenMap.get(token);
        latchMap.remove(token);
        accessTokenMap.remove(token);
        return accessToken;
    }
}