package xiattong.demo.redis;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ：xiattong
 * @description：Redis配置属性
 * @version: $
 * @date ：Created in 2022/10/20 1:39
 * @modified By：
 */
@Component
@ConfigurationProperties(prefix = "redis")
public class RedisProperty {

    /**
     * 主机
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 密码
     */
    private String password;

    /**
     * db
     */
    private Integer db;

    /**
     * 超时时间
     */
    private Integer timeout;

    /**
     * 最小空闲连接数
     */
    private Integer minIdle;

    /**
     * 最大空闲连接
     */
    private Integer maxIdle;

    /**
     * 最大连接数
     */
    private Integer maxTotal;

    /**
     * 最大等待时间
     */
    private Long maxWaitMills;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getDb() {
        return db;
    }

    public void setDb(Integer db) {
        this.db = db;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(Integer maxIdle) {
        this.maxIdle = maxIdle;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Long getMaxWaitMills() {
        return maxWaitMills;
    }

    public void setMaxWaitMills(Long maxWaitMills) {
        this.maxWaitMills = maxWaitMills;
    }
}
