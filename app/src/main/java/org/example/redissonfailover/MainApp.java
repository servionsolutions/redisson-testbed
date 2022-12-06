package org.example.redissonfailover;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MainApp {
    private static final Logger LOG = LoggerFactory.getLogger(MainApp.class);

    RedissonConnection redissonConnection = new RedissonConnection();

    MainApp() {
        LOG.info("Starting up test...");
        RLock rCacheInitializationLock = redissonConnection.getRedisson().getLock("cachedmap_lock");

        LOG.info("starting main loop in " + this.getClass().getName());

        while (true) {
            try {
                LOG.info("getting lock, is currently locked: {}", rCacheInitializationLock.isLocked());
//                rCacheInitializationLock.lock();

                if (!rCacheInitializationLock.tryLock(30, TimeUnit.SECONDS)) {
                    LOG.info("unable to get lock within 30 sec, will try again");
                    continue;
                }

                LOG.info("obtained lock, beginning sleep");
                Thread.sleep(5000);
                LOG.info("releasing lock");
                rCacheInitializationLock.unlock();
                LOG.info("released lock");
            } catch (Exception ex) {
                LOG.error("Caught exception", ex);
            }
        }
    }

    public static void main(String[] args) {
        MainApp app = new MainApp();
    }
}
