package xiattong.demo.rocketmq;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.annotation.SelectorType;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import xiattong.demo.es.ElasticMessageHandler;

import javax.annotation.Resource;

/**
 * @author ：xiattong
 * @description：消息消费者
 * @version: $
 * @date ：Created in 2022/10/15 23:53
 * @modified By：
 */
@Component
@RocketMQMessageListener(consumerGroup = "orders-db-consumer-group", topic = "orders_to_es",
        selectorType = SelectorType.TAG, selectorExpression = "orders")
public class MqConsumer implements RocketMQListener<String> {

    @Resource
    private ElasticMessageHandler elasticMessageHandler;

    @Override
    public void onMessage(String message) {
        try{
            System.out.println(Thread.currentThread().getName()+ "message:" + message);
            elasticMessageHandler.ordersMessageHandle(message);
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("消息消费失败了！:"+ ex);
            //抛出异常后，ConsumeConcurrentlyStatus.RECONSUME_LATER
            throw new RuntimeException(ex);
        }

    }
}
