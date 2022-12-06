				if (redis.call('exists', KEYS[1]) == 0) then
					redis.call('hincrby', KEYS[1], ARGV[2], 1);
					redis.call('pexpire', KEYS[1], ARGV[1]); return nil;
				end;
				if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then
					redis.call('hincrby', KEYS[1], ARGV[2], 1);
					redis.call('pexpire', KEYS[1], ARGV[1]); return nil;
				end;


*6 <- args_prefix, len of 6
	$4 <- bye_prefix, len command ("eval") = 4
arg1:	EVAL
		$339 <- byte prefix, len subname command == 339
arg2: if (redis.call('exists', KEYS[1]) == 0) then redis.call('hincrby', KEYS[1], ARGV[2], 1); redis.call('pexpire', KEYS[1], ARGV[1]); return nil; end; if (redis.call('hexists', KEYS[1], ARGV[2]) == 1) then redis.call('hincrby', KEYS[1], ARGV[2], 1); redis.call('pexpire', KEYS[1], ARGV[1]); return nil; end; return redis.call('pttl', KEYS[1]);
$1
arg3: 1 <- number of lua script keys to be provided
$23
arg4: dev:ib02:cachedmap_lock <- the KEY[1]
$5
arg5: 30000 <- the ARGV[1]
$38
arg6: 48203354-bbba-4f31-89f7-e840f01a0d35:1 <- ARGV[2]


*3 <- 3 args
$4
arg1: WAIT
$1
arg2: 1 <- 1 replica
$4
arg4: 1000 <- WAIT timeout
