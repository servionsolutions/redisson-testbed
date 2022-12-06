package org.example.redissonfailover;

import org.redisson.Redisson;
import org.redisson.api.NameMapper;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SubscriptionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedissonConnection {
    private static final Logger LOG = LoggerFactory.getLogger(RedissonConnection.class);

    private final String LOCAL_CONNECTION_URI = "localhost:7000";
    private final String KEY_PREFIX_COLON = System.getenv("ELASTICACHE_PREFIX") + ":";

    private final RedissonClient redisson;

    public RedissonConnection() {
        LOG.info("Beginning redisson configuration...");
        Config config = new Config();
        ClusterServersConfig clusterServers = config.useClusterServers()
                .setRetryInterval(3000)
                .setTimeout(30000)
//                .setRetryAttempts(300)
                .setSubscriptionMode(SubscriptionMode.SLAVE)
                .setSubscriptionsPerConnection(10)
                .setNameMapper(new NameMapper() {
                    @Override
                    public String map(String name) {
                        return KEY_PREFIX_COLON + name;
                    }

                    @Override
                    public String unmap(String name) {
                        return name.replace(KEY_PREFIX_COLON, "");
                    }
                });

        //Uncomment to test with local redis cluster
//        // Local testing with docker compose redis cluster
//        clusterServers.addNodeAddress("redis://" + LOCAL_CONNECTION_URI);

//        Uncomment to test with AWS elasticache redis cluster
        // AWS testing with SSL, same env vars as issuebook
        clusterServers.addNodeAddress("rediss://" +
                        System.getenv("ELASTICACHE_DNS_NAME") + ":" +
                        Integer.parseInt(System.getenv("ELASTICACHE_PORT")))
                .setUsername(System.getenv("ELASTICACHE_USERNAME"))
                .setPassword(System.getenv("ELASTICACHE_PASSWORD"));

        redisson = Redisson.create(config);
    }

//        config.setUseThreadClassLoader(false);
//        config.setCodec(new FstCodec());

    public RedissonClient getRedisson() {
        return redisson;
    }
}
