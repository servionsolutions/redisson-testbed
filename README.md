# redisson-testbed

Tooling to test and replicate bugs related to handling elasticache redis cluster failover issues in redis cluster library redisson.

This is based upon a vanilla aws elasticache redis 7 cluster (tested with 3 shards, 1 read replica per shard, multi-az enabled, cluster mode enabled).
This is able to verify the problem 100% of the time.  When triggering aws to failover one master node shard to a slave,
one redisson is never again is able to obtain a redisson lock without restarting redisson.

## Getting started

This tooling assumed you have ec2 host with necessary access to elasticache, hosted in a private subnet.
This setup can differ, however the test code is very simple and 100% repeatable.

To setup the testbed to connect to elasticache redis cluster mode enabled on an amazonlinux2 bastion host within a
private subnet, some variant of the following steps must be taken:

1. build this project shadow distribution: `gradlew shadowDistTar`
2. copy tar from local to s3: `aws s3 cp redisson-testbed/build/distributions/app-shadow.tar s3://<your bucket here>/`
3. On Bastion: [build and install redis-cli](https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/in-transit-encryption.html)
    ```
    cd /tmp
    sudo yum -y install openssl-devel gcc
    wget http://download.redis.io/redis-stable.tar.gz
    tar xvzf redis-stable.tar.gz
    cd redis-stable
    make distclean
    make redis-cli BUILD_TLS=yes
    sudo install -m 755 src/redis-cli /usr/local/bin/
    ```
4. On Bastion: install JRE:
   ```
   sudo amazon-linux-extras enable corretto8
   sudo yum -y install java-1.8.0-amazon-corretto
   ```
5. On Bastion: copy from s3 to jumphost: `aws s3 cp s3://<your bucket here>/app-shadow.tar /tmp/` 
6. On bastion: extract `tar xvf app-shadow.tar`
7. If desired, create/upload the 2 scripts from this repo in `scripts` directory on bastion:
   * redis-subs.sh
   * redis-lock.sh
8. Update both of the above to populate with REDIS ACL username and password
9. Run test:
   ```
   # Export variables used in scripts... instert values as appropriate
   # export ELASTICACHE_PREFIX=
   # export ELASTICACHE_DNS_NAME=
   # export ELASTICACHE_PORT=
   # export ELASTICACHE_USERNAME=
   # export ELASTICACHE_PASSWORD=

   # Run 2 identical copies of the app to test the lock or subscriptions
   /tmp/app-shadow/bin/app &> /tmp/app01_test11.log & 
   /tmp/app-shadow/bin/app &> /tmp/app02_test11.log &
   
   # if desired, monitor subscriptions with redis-cli - this will poll to see how many subscriptions are connected to a given topic
   /tmp/redis-subs.sh &> /tmp/subs_test11.log &

   # Monitor lock with redis-cli - this will poll to see the contents of the lock within redis directly
   /tmp/redis-lock.sh &>> /tmp/lock_test11.log &

   # Trigger a failover 
   aws elasticache test-failover --replication-group-id <cluster id here> --node-group-id 0001 --region eu-west-1
   
   # Example to tail the logs as you see fit 
   tail -f appNN_testXX.log | grep --text -v TRACE | grep --text -v REPLICATION | grep -e MainApp -e INFO -e WARN -e ERROR --text
   ```
