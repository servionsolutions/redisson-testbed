#!/bin/sh

TOPIC="{dev:ib02:cachedmap}:topic"

NODES=($(redis-cli --no-auth-warning --tls -h ${ELASTICACHE_DNS_NAME} --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} CLUSTER NODES | awk -F' ' '{print $2 "@" $3}' ))
NODES_SORTED=($(for l in ${NODES[@]}; do echo $l; done | sort))
while true
do
        for NODE in "${NODES_SORTED[@]}"
        do
                HOST=$(echo $NODE | awk -F':' '{print $1}')
                PORT=$(echo $NODE | awk -F':' '{print $2}' | cut -f1 -d"@")
                ROLE=$(echo $NODE | awk -F'@' '{print $3}')
                CHANNELS=$(timeout 5 redis-cli --csv --no-auth-warning --tls -h $HOST -p $PORT --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} PUBSUB CHANNELS | tr -cd , | wc -c)
                printf "%-12s\t%-15s\t%-10s\tchannels: %-3s\t%s\n"  $(date +"%T.%3N") $ROLE  $HOST $CHANNELS $(timeout 5 redis-cli --csv --no-auth-warning --tls -h $HOST -p $PORT --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} PUBSUB NUMSUB $TOPIC)
        done
        printf "\n"
        sleep 5s
done
