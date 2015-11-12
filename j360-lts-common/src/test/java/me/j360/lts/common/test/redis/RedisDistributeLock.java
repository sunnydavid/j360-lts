package me.j360.lts.common.test.redis;

import me.j360.lts.common.logger.Logger;
import me.j360.lts.common.logger.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Robert HG (254963746@qq.com) on 9/9/15.
 */
public class RedisDistributeLock {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisDistributeLock.class);

    private static JedisPool pool;
    private JedisLock        jedisLock;
    private String           lockKey;
    private Jedis jedis;
    private int              timeoutMsecs;
    private int              expireMsecs;

    public RedisDistributeLock(String lockKey) {
        this(lockKey, 3000, 300000);
    }

    public RedisDistributeLock(String lockKey, int timeoutMsecs, int expireMsecs) {
        this.lockKey = lockKey;
        this.jedis = pool.getResource();
        this.timeoutMsecs = timeoutMsecs;
        this.expireMsecs = expireMsecs;
        this.jedisLock = new JedisLock(jedis, lockKey.intern(), timeoutMsecs, expireMsecs);
    }

    public void wrap(Runnable runnable) {
        long begin = System.currentTimeMillis();
        try {
            // timeout��ʱ���ȴ�������ʱ�䣬����Ϊ3�룻expiration���ڣ������ڵ�ʱ������Ϊ5����
            LOGGER.info("begin logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
            if (jedisLock.acquire()) { // ������
                runnable.run();
            } else {
                LOGGER.info("The time wait for lock more than [{}] ms ", timeoutMsecs);
            }
        } catch (Throwable t) {
            // �ֲ�ʽ���쳣
            LOGGER.warn(t.getMessage(), t);
        } finally {
            this.lockRelease(jedisLock, jedis);
        }
        LOGGER.info("[{}]cost={}", lockKey, System.currentTimeMillis() - begin);
    }

    /**
     * �ͷ���,�����������߼�����ͷ�����װ
     */
    private void lockRelease(JedisLock lock,
                             Jedis jedis) {
        if (lock != null) {
            try {
                lock.release();// �����
            } catch (Exception e) {
            }
        }
        if (jedis != null) {
            jedis.close();
        }
        LOGGER.info("release logck,lockKey={},timeoutMsecs={},expireMsecs={}", lockKey, timeoutMsecs, expireMsecs);
    }

    public static JedisPool getPool() {
        return pool;
    }

    public static synchronized void setPool(JedisPool pool) {
        RedisDistributeLock.pool = pool;
    }

}
