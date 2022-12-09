package org.example.redissonfailover;

import org.redisson.Redisson;
import org.redisson.api.NameMapper;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.connection.ConnectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class RedissonConnection {
    private static final Logger LOG = LoggerFactory.getLogger(RedissonConnection.class);

    private final String LOCAL_CONNECTION_URI = "localhost:7000";
    private final String KEY_PREFIX = System.getenv("ELASTICACHE_PREFIX");

    private final RedissonClient redisson;

    public RedissonConnection() {
        LOG.info("Beginning redisson configuration...");
        Config config = new Config();
        ClusterServersConfig clusterServers = config.useClusterServers()
                .setRetryInterval(3000)
                .setTimeout(30000)
//                .setRetryAttempts(300)
//                .setSubscriptionMode(SubscriptionMode.SLAVE)
                .setSubscriptionsPerConnection(10);
        if(KEY_PREFIX != null && KEY_PREFIX.length() > 0) {
            clusterServers.setNameMapper(new NameMapper() {
                @Override
                public String map(String name) {
                    return KEY_PREFIX + ":" + name;
                }

                @Override
                public String unmap(String name) {
                    return name.replace(KEY_PREFIX + ":", "");
                }
            });
        }
        // Setting this to FALSE eliminates the lock bug, so it seems that the unexpected exception causes the bug.
//        config.setCheckLockSyncedSlaves(false);

        config.setConnectionListener(new ConnectionListener() {
            @Override
            public void onConnect(InetSocketAddress addr) {
                LOG.info("ConnectionListener: onConnect() called: {}", addr);
            }

            @Override
            public void onDisconnect(InetSocketAddress addr) {
                LOG.info("ConnectionListener: onDisconnect() called: {}", addr);
            }
        });

        // Uncomment to test with local redis cluster
        // Local testing with docker compose redis cluster
        clusterServers.addNodeAddress("redis://" + LOCAL_CONNECTION_URI);

//        Uncomment to test with AWS elasticache redis cluster
//        // AWS testing with SSL, same env vars as issuebook
//        clusterServers.addNodeAddress("rediss://" +
//                        System.getenv("ELASTICACHE_DNS_NAME") + ":" +
//                        Integer.parseInt(System.getenv("ELASTICACHE_PORT")))
//                .setUsername(System.getenv("ELASTICACHE_USERNAME"))
//                .setPassword(System.getenv("ELASTICACHE_PASSWORD"));

        redisson = Redisson.create(config);
    }

//        config.setUseThreadClassLoader(false);
//        config.setCodec(new FstCodec());

    public RedissonClient getRedisson() {
        return redisson;
    }
}
