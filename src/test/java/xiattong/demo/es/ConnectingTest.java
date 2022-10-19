package xiattong.demo.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.OpType;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xiattong.demo.es.order.model.OrderDto;

import javax.annotation.Resource;

/**
 * @author ：xiattong
 * @description：
 * @version: $
 * @date ：Created in 2022/10/16 22:19
 * @modified By：
 */
@SpringBootTest
public class ConnectingTest {

    @Resource()
    private RestClient restClient;

    @Test
    public void searchTest() throws Exception {

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        SearchResponse<OrderDto> search = client.search(s -> s
                        .index("orders")
                        ,
                OrderDto.class);

        for (Hit<OrderDto> hit: search.hits().hits()) {
            System.out.println((hit.source()));
        }
    }

    @Test
    public void putTest() throws Exception{
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient client = new ElasticsearchClient(transport);

        OrderDto orderDto = new OrderDto();
        orderDto.setId(1L);
        orderDto.setOrderAt("2022-10-18 01:24:00");

        IndexRequest<OrderDto> request = IndexRequest.of(i -> i
                .index("orders-2401")
                .id(orderDto.getId().toString())
                .opType(OpType.Create)
                .document(orderDto));

        IndexResponse response = client.index(request);

        System.out.println("Indexed with version " + response.version());

    }
}
