### 代码

关键数据结构：ZSet
说明：有序列表，score相同时根据字典序排序
关键方法：
# 添加元素
zadd key score name

# 获取元素列表
zrange key head tail # 按score从小到大返回

# 获取集合大小
zcard key

代码：

```
# 速率
local rate = redis.call('hget', KEYS[1], 'rate');
# 时间间隔
local interval = redis.call('hget', KEYS[1], 'interval');
# 限流器类型，0为集群限流，1为单实例限流
local type = redis.call('hget', KEYS[1], 'type');
assert(rate ~= false and interval ~= false and type ~= false, 'RateLimiter is not initialized')

# {name}:value 记录当前令牌桶中的令牌数
local valueName = KEYS[2];

# {name}:permits zset，记录了请求的令牌数量，score则为请求的时间戳
local permitsName = KEYS[4];
if type == '1' then
    # {name}:value:id
    valueName = KEYS[3];
    permitsName = KEYS[5];
end;

# 请求的令牌数不能大于桶中最大令牌数
assert(tonumber(rate) >= tonumber(ARGV[1]), 'Requested permits amount could not exceed defined rate');

# 当前令牌数
local currentValue = redis.call('get', valueName);
local res;
if currentValue ~= false then
    # 从第一次设的zset中取数据，范围是0 ~ (当前时间戳 - 令牌生产的时间)
    # 可以看到，如果第二次请求时间距离第一次请求时间很短(小于令牌产生的时间)，那么这个差值将小于上一次请求的时间，取出来的将会是空列表。反之，能取出之前的请求信息,见图1
    # 这里作者将这个取出来的数据命名为expiredValues，可认为指的是过期的数据
    local expiredValues = redis.call('zrangebyscore', permitsName, 0, tonumber(ARGV[2]) - interval);
    local released = 0;
    for i, v in ipairs(expiredValues) do
        local random, permits = struct.unpack('Bc0I', v);
        released = released + permits;
    end;

    if released > 0 then
        # 先删除过期的请求值
        redis.call('zremrangebyscore', permitsName, 0, tonumber(ARGV[2]) - interval);
        # 如果归还的令牌数+当前令牌数 > 设置的总令牌数
        if tonumber(currentValue) + released > tonumber(rate) then
            # 当前令牌数 = 总令牌数 - interval之间的请求个数
            currentValue = tonumber(rate) - redis.call('zcard', permitsName);
        else
            currentValue = tonumber(currentValue) + released;
        end;
        redis.call('set', valueName, currentValue);
    end;
    # 以上代码解释。统计在当前请求中，之前的已经过期的请求，并将他们记录释放掉。然后归还其拿的令牌数

    # 如果当前令牌数 < 请求的令牌数.见图2
    if tonumber(currentValue) < tonumber(ARGV[1]) then
        # 获取距离当前时间最久的一次请求。因为score是时间戳所以得分最低的一定是距离最久的请求
        local firstValue = redis.call('zrange', permitsName, 0, 0, 'withscores');
        # 返回到下一个令牌生产还需要多少时间，单位是毫秒。猜测这里+3是做一个缓冲，等于向后延一小段时间
        res = 3 + interval - (tonumber(ARGV[2]) - tonumber(firstValue[2]));
    else
        # 反之记录下本次请求，并且减少桶中令牌数量
        redis.call('zadd', permitsName, ARGV[2], struct.pack('Bc0I', string.len(ARGV[3]), ARGV[3], ARGV[1]));
        redis.call('decrby', valueName, ARGV[1]);
        res = nil;
    end;
else
    # 第一次进来，设置当前桶中的令牌数就是速率
    redis.call('set', valueName, rate);
    # 设置一个zset，score是当前时间戳，member记录的是本次请求的令牌数与一个随机串的结构体
    redis.call('zadd', permitsName, ARGV[2], struct.pack('Bc0I', string.len(ARGV[3]), ARGV[3], ARGV[1]));
    # 当前桶中令牌数-本次请求的令牌数
    redis.call('decrby', valueName, ARGV[1]);
    res = nil;
end;

# 将与限流有关的key统一续期
local ttl = redis.call('pttl', KEYS[1]);
if ttl > 0 then
    redis.call('pexpire', valueName, ttl);
    redis.call('pexpire', permitsName, ttl);
end;
return res;


# 带有超时时间的获取令牌
private void tryAcquireAsync(long permits, RPromise<Boolean> promise, long timeoutInMillis) {
    long s = System.currentTimeMillis();
    RFuture<Long> future = tryAcquireAsync(RedisCommands.EVAL_LONG, permits);
    future.onComplete((delay, e) -> {
        if (e != null) {
            promise.tryFailure(e);
            return;
        }

        if (delay == null) {
            //delay就是lua返回的 还需要多久才会有令牌
            promise.trySuccess(true);
            return;
        }

        //没有手动设置超时时间的逻辑
        if (timeoutInMillis == -1) {
            //延迟delay时间后重新执行一次拿令牌的动作
            commandExecutor.getConnectionManager().getGroup().schedule(() -> {
                tryAcquireAsync(permits, promise, timeoutInMillis);
            }, delay, TimeUnit.MILLISECONDS);
            return;
        }

        //el 请求redis拿令牌的耗时
        long el = System.currentTimeMillis() - s;
        //如果设置了超时时间，那么应该减去拿令牌的耗时
        long remains = timeoutInMillis - el;
        if (remains <= 0) {
            //如果那令牌的时间比设置的超时时间还要大的话直接就false了
            promise.trySuccess(false);
            return;
        }
        //比如设置的的超时时间为1s，delay为1500ms，那么1s后告知失败
        if (remains < delay) {
            commandExecutor.getConnectionManager().getGroup().schedule(() -> {
                promise.trySuccess(false);
            }, remains, TimeUnit.MILLISECONDS);
        } else {
            long start = System.currentTimeMillis();
            commandExecutor.getConnectionManager().getGroup().schedule(() -> {
                //因为这里是异步的，所以真正再次拿令牌之前再检查一下过去了多久时间。如果过去的时间比设置的超时时间大的话，直接false
                long elapsed = System.currentTimeMillis() - start;
                if (remains <= elapsed) {
                    promise.trySuccess(false);
                    return;
                }
                //再次拿令牌
                tryAcquireAsync(permits, promise, remains - elapsed);
            }, delay, TimeUnit.MILLISECONDS);
        }
    });
}
```