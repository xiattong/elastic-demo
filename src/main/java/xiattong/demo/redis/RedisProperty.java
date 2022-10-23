package xiattong.demo.redis;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author ：xiattong
 * @description：Redis配置属性
 * @version: $
 * @date ：Created in 2022/10/20 1:39
 * @modified By：
 */
@Getter
@Setter
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
}
