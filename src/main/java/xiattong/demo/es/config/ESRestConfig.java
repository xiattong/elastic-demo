package xiattong.demo.es.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

/**
 * @author ：xiattong
 * @description：
 * @version: $
 * @date ：Created in 2022/10/17 0:25
 * @modified By：
 */
@Configuration
public class ESRestConfig {

    @Value("${es.connectionTimeout}")
    private int connectTimeOut;
    @Value("${es.socketTimeout}")
    private int socketTimeOut;

    @Value("${es.host}")
    private String esHost;

    @Value("${es.port}")
    private Integer esPort;

    @Value("${es.ssl}")
    private Boolean isSSL;

    @Value("${es.certPath}")
    private String certPath;

    @Value("${es.username}")
    private String username;

    @Value("${es.password}")
    private String password;

    private int maxConnectNum = 100;
    private int maxConnectPerRoute = 100;
    private int connectionRequestTimeOut = 5000;

    private void setRequestTimeOutConfig(RestClientBuilder builder) {
        builder.setRequestConfigCallback(builder1 -> {
            builder1.setConnectTimeout(connectTimeOut);
            builder1.setSocketTimeout(socketTimeOut);
            builder1.setConnectionRequestTimeout(connectionRequestTimeOut);
            return builder1;
        });
    }

    /**
     * 权限设置
     * @param builder
     */
    private void setCredentialConfig(RestClientBuilder builder) {
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            if (isSSL) {
                try {
                    Path caCertificatePath = Paths.get(certPath);
                    CertificateFactory factory = CertificateFactory.getInstance("X.509");
                    Certificate trustedCa;
                    try (InputStream is = Files.newInputStream(caCertificatePath)) {
                        trustedCa = factory.generateCertificate(is);
                    }

                    KeyStore trustStore = KeyStore.getInstance("pkcs12");
                    trustStore.load(null, null);
                    trustStore.setCertificateEntry("ca", trustedCa);
                    SSLContextBuilder sslContextBuilder = SSLContexts.custom().loadTrustMaterial(trustStore, null);
                    final SSLContext sslContext = sslContextBuilder.build();

                    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                    credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    httpClientBuilder.setSSLContext(sslContext);
                    httpClientBuilder.setMaxConnTotal(maxConnectNum);
                    httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
                } catch (Exception e) {
                    System.out.printf("配置ES加密集群连接数错误！%s", e);
                    e.printStackTrace();
                }
            } else {
                httpClientBuilder.setMaxConnTotal(maxConnectNum);
                httpClientBuilder.setMaxConnPerRoute(maxConnectPerRoute);
            }
            return httpClientBuilder;
        });
    }


    @Bean
    public ElasticsearchClient esClient() {
        RestClientBuilder builder = RestClient.builder( new HttpHost(esHost, esPort, isSSL ? "https" : "http"));
        // 超时设置
        setRequestTimeOutConfig(builder);
        // 权限配置
        setCredentialConfig(builder);
        ElasticsearchTransport transport = new RestClientTransport(builder.build(), new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}
