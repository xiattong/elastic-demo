package xiattong.demo.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xiattong.demo.es.config.ElasticMessageTypeEnum;
import xiattong.demo.es.model.OrderDto;
import xiattong.demo.redis.RedisClient;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author ：xiattong
 * @description：ES消息处理
 * @version: $
 * @date ：Created in 2022/10/16 21:53
 * @modified By：
 */
@Component
public class ElasticMessageHandler {

    @Resource
    private ElasticsearchClient esClient;

    @Resource
    private RedisClient redisClient;

    /**
     * 超时时间 lock
     */
    public static final int LOCK_TIMEOUT = 300;

    /**
     * 对订单消息进行消费
     * @param message
     * @throws RuntimeException
     */
    public void ordersMessageHandle(String message) throws RuntimeException{
        if (StringUtils.isEmpty(message)) {
            return;
        }
        JSONObject msgObj = JSONObject.parseObject(message);
        // 检查 ddl
        if (Boolean.TRUE.equals(msgObj.get("isDdl"))) {
            return;
        }
        // 检查消息类型
        Object typeObj = msgObj.get("type");
        if (Objects.isNull(typeObj) || ElasticMessageTypeEnum.getType(typeObj.toString()) == null) {
            return;
        }
        // 执行策略
        doHandler(ElasticMessageTypeEnum.getType(typeObj.toString()), msgObj);
     }

    /**
     *
     * @param type
     * @param msgObj
     */
    private void doHandler(ElasticMessageTypeEnum type, JSONObject msgObj) throws RuntimeException{
        // 解析出订单数据
        List<OrderDto> orderList = msgObj.getObject("data", new TypeReference<List<OrderDto>>(){});
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        switch (type) {
            case INSERT:
                insertProcess(orderList);
                break;
            case UPDATE:
                updateProcess(orderList);
                break;
            case DELETE:
                deleteProcess(orderList);
                break;
        }
    }

    /**
     * 新增数据
     * @param orderList
     */
    private void insertProcess(List<OrderDto> orderList) {
        // 新增数据不需要做版本校验，直接写入es
        for (OrderDto orderDto : orderList) {
            Optional<String> lock = Optional.empty();
            try {
                // 给订单加锁
                lock = redisClient.tryLock(orderDto.getOrderNo(), LOCK_TIMEOUT);
                if (lock.isPresent()) {
                    IndexRequest<OrderDto> request = IndexRequest.of(i -> i
                            .index("orders")
                            .requireAlias(Boolean.TRUE)
                            .id(orderDto.getId().toString())
                            .opType(OpType.Create)
                            .document(orderDto));

                    IndexResponse response = esClient.index(request);
                    System.out.printf("订单写入ES成功！orderNo:{}, version:{}", orderDto.getOrderNo(), response.version());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 抛出异常后，消息会重发
                throw new RuntimeException(e);
            } finally {
                if (lock.isPresent()) {
                    redisClient.unlock(orderDto.getOrderNo(), lock.get());
                }
            }
        }
    }

    /**
     * 更新数据
     * @param orderList
     */
    private void updateProcess(List<OrderDto> orderList) {
        for (OrderDto orderDto : orderList) {
            Optional<String> lock = Optional.empty();
            try {
                // 给订单加锁
                lock = redisClient.tryLock(orderDto.getOrderNo(), LOCK_TIMEOUT);
                if (lock.isPresent()) {
                    // 更新需要先去ES中查询数据并比对版本号
                    SearchResponse<OrderDto> searchResponse = esClient.search(s -> s
                                    .index("orders")
                                    .query(q -> q.match(t -> t.field("orderNo").query(orderDto.getOrderNo()))),
                            OrderDto.class
                    );
                    List<Hit<OrderDto>> hits = searchResponse.hits().hits();
                    if (CollectionUtils.isNotEmpty(hits)) {
                        for (Hit<OrderDto> hit : hits) {
                            OrderDto oldOrder = hit.source();
                            // 检查版本，新数据版本低时直接丢弃
                            if (oldOrder.getVersion() >= orderDto.getVersion()) {
                                continue;
                            }
                        }
                    }

                    // 先删除
                    DeleteByQueryRequest deleteRequest = DeleteByQueryRequest.of(i -> i
                            .index("orders")
                            .query(q -> q.match(t -> t.field("orderNo").query(orderDto.getOrderNo()))));
                    esClient.deleteByQuery(deleteRequest);

                    // 再写入
                    IndexRequest<OrderDto> indexRequest = IndexRequest.of(i -> i
                            .index("orders")
                            .requireAlias(Boolean.TRUE)
                            .id(orderDto.getId().toString())
                            .opType(OpType.Create)
                            .document(orderDto));

                    IndexResponse indexResponse = esClient.index(indexRequest);
                    System.out.printf("更新ES成功！orderNo:{}, version:{}", orderDto.getOrderNo(), indexResponse.version());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 抛出异常后，消息会重发
                throw new RuntimeException(e);
            } finally {
                if (lock.isPresent()) {
                    redisClient.unlock(orderDto.getOrderNo(), lock.get());
                }
            }
        }
    }

    /**
     * 删除数据
     * @param orderList
     */
    private void deleteProcess(List<OrderDto> orderList) {
        for (OrderDto orderDto : orderList) {
            Optional<String> lock = Optional.empty();
            try {
                // 给订单加锁
                lock = redisClient.tryLock(orderDto.getOrderNo(), LOCK_TIMEOUT);
                if (lock.isPresent()) {
                    DeleteRequest request = DeleteRequest.of(i -> i.index("orders")
                            .id(orderDto.getId().toString()));
                    DeleteResponse response = esClient.delete(request);
                    System.out.printf("删除ES中订单成功！orderNo:{}, version:{}", orderDto.getOrderNo(), response.version());
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 抛出异常后，消息会重发
                throw new RuntimeException(e);
            } finally {
                if (lock.isPresent()) {
                    redisClient.unlock(orderDto.getOrderNo(), lock.get());
                }
            }
        }
    }
}
