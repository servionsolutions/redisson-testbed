#!/bin/sh

LOCK_NAME="dev:ib02:cachedmap_lock"
while true
do
        printf "%-12s\tTTL: %-3s\tLocked by:\t%s\n"  $(date +"%T.%3N") $(timeout 5 redis-cli --csv --no-auth-warning --tls -c -h ${ELASTICACHE_DNS_NAME} -p 6379 --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} TTL ${LOCK_NAME}) $(timeout 5 redis-cli --csv --no-auth-warning --tls -c -h ${ELASTICACHE_DNS_NAME} -p 6379 --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} HGETALL ${LOCK_NAME})
        sleep 5s
done

