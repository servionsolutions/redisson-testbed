package org.example.redissonfailover;

import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class LockFailoverFailApp {
    private static final Logger LOG = LoggerFactory.getLogger(LockFailoverFailApp.class);

    RedissonConnection redissonConnection = new RedissonConnection();

    LockFailoverFailApp() {
        LOG.info("Starting up test...");
        RLock distributedLock = redissonConnection.getRedisson().getLock("distributed_lock");

        LOG.info("starting main loop in " + this.getClass().getName());

        while (true) {
            try {
                LOG.info("My node ID: {}\tgetting lock, is currently locked: {}", redissonConnection.getRedisson().getId(), distributedLock.isLocked());
                if (!distributedLock.tryLock(30, TimeUnit.SECONDS)) {
                    LOG.info("unable to get lock within 30 sec, will try again");
                    continue;
                }

                LOG.info("\"My node ID: {}\tobtained lock, beginning sleep to emulate work", redissonConnection.getRedisson().getId());
                Thread.sleep(5000);
                LOG.info("My node ID: {}\treleasing lock", redissonConnection.getRedisson().getId());
                distributedLock.unlock();
                LOG.info("released lock");
            } catch (Exception ex) {
                LOG.error("Caught exception", ex);
            }
        }
    }

    public static void main(String[] args) {
        LockFailoverFailApp app = new LockFailoverFailApp();
    }
}
