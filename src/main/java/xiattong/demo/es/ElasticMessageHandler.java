package xiattong.demo.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.*;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import xiattong.demo.es.order.model.OrderDto;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

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

    /**
     * 对订单消息进行消费
     * @param message
     * @throws RuntimeException
     */
    public void ordersMessageHandle(String message) throws IOException{
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
    private void doHandler(ElasticMessageTypeEnum type, JSONObject msgObj) throws IOException{
        // 解析出订单数据
        List<OrderDto> orderList = msgObj.getObject("data", new TypeReference<List<OrderDto>>(){});
        if (CollectionUtils.isEmpty(orderList)) {
            return;
        }
        switch (type) {
            case INSERT:
                insertProcess(orderList);
            case UPDATE:
                updateProcess(orderList);
            case DELETE:
                deleteProcess(orderList);
        }
    }

    /**
     * 新增数据
     * @param orderList
     */
    private void insertProcess(List<OrderDto> orderList) throws IOException {
        // 新增数据不需要做版本校验，直接写入es
        for (OrderDto orderDto : orderList) {
            write(orderDto);
        }
    }

    /**
     * 更新数据
     * @param orderList
     */
    private void updateProcess(List<OrderDto> orderList) throws IOException {
        // 更新需要先去ES中查询数据并比对版本号
        for (OrderDto orderDto : orderList) {
            GetResponse<OrderDto> response = esClient.get(g -> g.index("orders-" + orderDto.getSellerId()).id(orderDto.getId().toString()), OrderDto.class);
            if (response.found()) {
                OrderDto oldOrder = response.source();
                // 检查版本，新数据版本低时直接丢弃
                if (oldOrder.getVersion() >= orderDto.getVersion()) {
                    continue;
                }
            }
            write(orderDto);
        }
    }

    /**
     * 删除数据
     * @param orderList
     */
    private void deleteProcess(List<OrderDto> orderList) throws IOException{
        for (OrderDto orderDto : orderList) {
            DeleteRequest request = DeleteRequest.of(i -> i.index("orders-" + orderDto.getSellerId())
                            .id(orderDto.getId().toString()));
            DeleteResponse response = esClient.delete(request);
            System.out.printf("删除ES中订单成功！orderNo:{}, version:{}", orderDto.getOrderNo(), response.version());
        }
    }

    /**
     * 写入es
     * @param orderDto
     * @throws IOException
     */
    private void write(OrderDto orderDto) throws IOException {
        IndexRequest<OrderDto> request = IndexRequest.of(i -> i
                .index("orders-" + orderDto.getSellerId())
                .id(orderDto.getId().toString())
                .opType(OpType.Create)
                .document(orderDto));

        IndexResponse response = esClient.index(request);
        System.out.printf("订单写入ES成功！orderNo:{}, version:{}", orderDto.getOrderNo(), response.version());
    }
}
