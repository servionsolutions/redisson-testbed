#!/bin/sh

TOPIC="\
{dev:ib02:map0}:topic \
{dev:ib02:map1}:topic \
{dev:ib02:map2}:topic \
{dev:ib02:map3}:topic \
{dev:ib02:map4}:topic \
{dev:ib02:map5}:topic \
{dev:ib02:map6}:topic \
{dev:ib02:map7}:topic \
{dev:ib02:map8}:topic \
{dev:ib02:map9}:topic \
{dev:ib02:map10}:topic \
{dev:ib02:map11}:topic \
{dev:ib02:map12}:topic \
{dev:ib02:map13}:topic \
{dev:ib02:map14}:topic \
{dev:ib02:map15}:topic \
{dev:ib02:map16}:topic \
{dev:ib02:map17}:topic \
{dev:ib02:map18}:topic \
{dev:ib02:map19}:topic \
{dev:ib02:map20}:topic \
{dev:ib02:map21}:topic \
{dev:ib02:map22}:topic \
{dev:ib02:map23}:topic \
{dev:ib02:map24}:topic \
{dev:ib02:map25}:topic \
{dev:ib02:map26}:topic \
{dev:ib02:map27}:topic \
{dev:ib02:map28}:topic \
{dev:ib02:map29}:topic \
{dev:ib02:map30}:topic \
{dev:ib02:map31}:topic \
{dev:ib02:map32}:topic \
{dev:ib02:map33}:topic \
{dev:ib02:map34}:topic \
{dev:ib02:map35}:topic \
{dev:ib02:map36}:topic \
{dev:ib02:map37}:topic \
{dev:ib02:map38}:topic \
{dev:ib02:map39}:topic \
{dev:ib02:map40}:topic \
{dev:ib02:map41}:topic \
{dev:ib02:map42}:topic \
{dev:ib02:map43}:topic \
{dev:ib02:map44}:topic \
{dev:ib02:map45}:topic \
{dev:ib02:map46}:topic \
{dev:ib02:map47}:topic \
{dev:ib02:map48}:topic \
{dev:ib02:map49}:topic \
{dev:ib02:map50}:topic \
{dev:ib02:map51}:topic \
{dev:ib02:map52}:topic \
{dev:ib02:map53}:topic \
{dev:ib02:map54}:topic \
{dev:ib02:map55}:topic \
{dev:ib02:map56}:topic \
{dev:ib02:map57}:topic \
{dev:ib02:map58}:topic \
{dev:ib02:map59}:topic \
{dev:ib02:map60}:topic \
{dev:ib02:map61}:topic \
{dev:ib02:map62}:topic \
{dev:ib02:map63}:topic \
{dev:ib02:map64}:topic \
{dev:ib02:map65}:topic \
{dev:ib02:map66}:topic \
{dev:ib02:map67}:topic \
{dev:ib02:map68}:topic \
{dev:ib02:map69}:topic \
{dev:ib02:map70}:topic \
{dev:ib02:map71}:topic \
{dev:ib02:map72}:topic \
{dev:ib02:map73}:topic \
{dev:ib02:map74}:topic \
{dev:ib02:map75}:topic \
{dev:ib02:map76}:topic \
{dev:ib02:map77}:topic \
{dev:ib02:map78}:topic \
{dev:ib02:map79}:topic \
{dev:ib02:map80}:topic \
{dev:ib02:map81}:topic \
{dev:ib02:map82}:topic \
{dev:ib02:map83}:topic \
{dev:ib02:map84}:topic \
{dev:ib02:map85}:topic \
{dev:ib02:map86}:topic \
{dev:ib02:map87}:topic \
{dev:ib02:map88}:topic \
{dev:ib02:map89}:topic \
{dev:ib02:map90}:topic \
{dev:ib02:map91}:topic \
{dev:ib02:map92}:topic \
{dev:ib02:map93}:topic \
{dev:ib02:map94}:topic \
{dev:ib02:map95}:topic \
{dev:ib02:map96}:topic \
{dev:ib02:map97}:topic \
{dev:ib02:map98}:topic \
{dev:ib02:map99}:topic \
"

NODES=($(redis-cli --no-auth-warning --tls -h ${ELASTICACHE_DNS_NAME} --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} CLUSTER NODES | awk -F' ' '{print $2 "@" $3}' ))
NODES_SORTED=($(for l in ${NODES[@]}; do echo $l; done | sort))
while true
do
        for NODE in "${NODES_SORTED[@]}"
        do
                HOST=$(echo $NODE | awk -F':' '{print $1}')
                PORT=$(echo $NODE | awk -F':' '{print $2}' | cut -f1 -d"@")
                ROLE=$(echo $NODE | awk -F'@' '{print $3}')
                CHANNELS=$(timeout 5 redis-cli --csv --no-auth-warning --tls -h $HOST -p $PORT --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} PUBSUB CHANNELS | awk -F',' '{print NF}')
                printf "%-12s\t%-15s\t%-10s\tchannels: %-3s\t%s\n"  $(date +"%T.%3N") $ROLE  $HOST $CHANNELS $(timeout 5 redis-cli --csv --no-auth-warning --tls -h $HOST -p $PORT --user ${ELASTICACHE_USERNAME} --pass ${ELASTICACHE_PASSWORD} PUBSUB NUMSUB $TOPIC)
        done
        printf "\n"
        sleep 5s
done
