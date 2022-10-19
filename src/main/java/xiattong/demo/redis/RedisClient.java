package xiattong.demo.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Optional;

/**
 * @author ：xiattong
 * @description：RedisClient
 * @version: $
 * @date ：Created in 2022/10/20 1:26
 * @modified By：
 */
public class RedisClient {

    private JedisPool jedisPool;

    public RedisClient(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 分布式锁-获取锁
     * @param key    指定锁
     * @param expire 锁的过期时间，单位秒
     * @return 是否锁定及锁定token
     * @see Optional#isPresent() true表明获取到锁
     * @see Optional#get() 获取到token，用于解锁
     */
    public Optional<String> tryLock(String key, long expire) {
        String token = String.valueOf(System.nanoTime());
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            SetParams setParams = new SetParams();
            setParams.ex((int) expire);
            setParams.nx();
            boolean locked = "OK".equals(jedis.set(key, token, setParams));
            if (locked) {
                return Optional.of(token);
            }

        } catch (Exception e) {
            System.out.printf("error when execute redis command, key=%s, error:%s", key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return Optional.empty();
    }

    private static String UNLOCK_SCRIPT = "local token = redis.call('get',KEYS[1]) if token == ARGV[1] then return redis.call('del',KEYS[1]) elseif token == false then return 1 else return 0 end";

    /**
     * 解除指定的锁定
     *
     * @param key   指定锁
     * @param token 锁定的token
     * @return
     */
    public boolean unlock(String key, String token) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            Long num = (Long) jedis.eval(UNLOCK_SCRIPT, 1, key, token);
            return num > 0;
        } catch (Exception e) {
            System.out.printf("error when execute redis command, key=%s, error:%s", key, e);
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
