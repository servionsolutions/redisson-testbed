package org.example.redissonfailover;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscriptionFailApp {
    private static final Logger LOG = LoggerFactory.getLogger(SubscriptionFailApp.class);
    RedissonConnection redissonConnection = new RedissonConnection();

    SubscriptionFailApp() {
        LOG.info("Starting up test...");

        int count = -1;
        List<RLocalCachedMap<Object, Object>> maps = new ArrayList<>();

        RLocalCachedMap<Object, Object> testMap = redissonConnection.getRedisson()
                .getLocalCachedMap("map0", LocalCachedMapOptions.defaults().syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                        .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR));
        maps.add(testMap);

        for (int i = 1; i <= 99; ++i) {
            RLocalCachedMap<Object, Object> idleTestMap = redissonConnection.getRedisson()
                    .getLocalCachedMap("map" + i, LocalCachedMapOptions.defaults().syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                            .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.CLEAR));
            maps.add(testMap);
        }

        LOG.info("Created {} cached maps", maps.size());

        testMap.preloadCache();
        String shortId = redissonConnection.getRedisson().getId().substring(0, 7);

        testMap.put(shortId + "_increasing", ++count);
        testMap.put(shortId + "_timestamp", System.currentTimeMillis());
        Map<Object, Object> localCachedMap = testMap.getCachedMap();

        LOG.info("starting main loop to publish continual data in first map " + this.getClass().getName());
        Thread printingHook = new Thread(testMap::clear);
        Runtime.getRuntime().addShutdownHook(printingHook);

        while (true) {
            try {
                LOG.info("beginning sleep");
                Thread.sleep(5000);
                LOG.info("mynode: {}, current time: {} local cache:", shortId, System.currentTimeMillis());
                localCachedMap.forEach((key, value) -> LOG.info("{}: {}", key, value));

                LOG.info("mynode: {}, current time: {} real cache:", shortId, System.currentTimeMillis());
                testMap.forEach((key, value) -> LOG.info("{}: {}", key, value));

                LOG.info("writing values");
                testMap.put(shortId + "_increasing", ++count);
                testMap.put(shortId + "_timestamp", System.currentTimeMillis());
            } catch (Exception ex) {
                LOG.error("Caught exception", ex);
            }
        }
    }

    public static void main(String[] args) {
        SubscriptionFailApp app = new SubscriptionFailApp();
    }
}
