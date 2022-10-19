package xiattong.demo.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author ：xiattong
 * @description：Redis配置类
 * @version: $
 * @date ：Created in 2022/10/20 1:39
 * @modified By：
 */
public class RedisConfig {

    @Bean
    @Primary
    public JedisPool jedisPool(@Autowired RedisProperty redisProperty) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        jedisPoolConfig.setMaxIdle(redisProperty.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(redisProperty.getMaxWaitMills());
        jedisPoolConfig.setMaxTotal(redisProperty.getMaxTotal());
        jedisPoolConfig.setMinIdle(redisProperty.getMinIdle());

        JedisPool jedisPool;
        if (StringUtils.isEmpty(redisProperty.getPassword())) {
            jedisPool = new JedisPool(jedisPoolConfig, redisProperty.getHost(), redisProperty.getPort(), redisProperty.getTimeout());
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, redisProperty.getHost(), redisProperty.getPort(), redisProperty.getTimeout(), redisProperty.getPassword());
        }
        return jedisPool;
    }

    @Bean
    public RedisClient RedisClient(@Qualifier("jedisPool") JedisPool jedisPool) {
        return new RedisClient(jedisPool);
    }
}
