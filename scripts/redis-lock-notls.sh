#!/bin/sh

LOCK_NAME="distributed_lock"
while true
do
        printf "%-12s\tTTL: %-3s\tLocked by:\t%s\n"  $(date +"%T.%3N") $(timeout 5 redis-cli --csv --no-auth-warning  -c -h localhost -p 7000 TTL ${LOCK_NAME}) $(timeout 5 redis-cli --csv  -c -h localhost -p 7000 HGETALL ${LOCK_NAME})
        sleep 5s
done

