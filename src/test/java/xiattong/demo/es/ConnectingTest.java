package xiattong.demo.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.indices.*;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xiattong.demo.es.order.model.OrderDto;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author ：xiattong
 * @description：
 * @version: $
 * @date ：Created in 2022/10/16 22:19
 * @modified By：
 */
@SpringBootTest
public class ConnectingTest {

    @Resource
    private ElasticsearchClient esClient;

    @Test
    public void createIndex() throws IOException {
        //tag::builders
        CreateIndexResponse createResponse = esClient.indices().create(
                new CreateIndexRequest.Builder()
                        .index("orders-000001")
                        .aliases("orders",
                                new Alias.Builder().isWriteIndex(true).build()
                        )
                        .build()
        );
        System.out.printf(createResponse.index());
    }

    @Test
    public void writeIndex() throws IOException {


        for (long id = 2L ; id < 10l ; id ++) {

            OrderDto orderDto = new OrderDto();
            orderDto.setId(id);

            IndexRequest<OrderDto> request = IndexRequest.of(i -> i
                    .index("orders")
                    .id(orderDto.getId().toString())
                    .opType(OpType.Create)
                    .document(orderDto));

            IndexResponse response = esClient.index(request);
            System.out.printf("订单写入ES成功！orderNo:{}, version:{}", orderDto.getOrderNo(), response.version());
        }
    }

    /**
     * 查询一个别名是否存在
     * @throws IOException
     */
    @Test
    public void checkAliasExit() throws IOException {
        GetAliasRequest request = GetAliasRequest.of(i ->i.name("orders"));
        GetAliasResponse response = esClient.indices().getAlias(request);
        System.out.println(response);
    }
}
