package crtpapi.com.test.crptapp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class CrptApi {
    private final String CREATE_DOC_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Object lock = new Object();
    private LocalDateTime localDateTime;
    private int countRequests;


    public CrptApi(TimeUnit timeUnit, int requestLimit) throws MalformedURLException {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.countRequests = 0;
        this.localDateTime = LocalDateTime.now();
    }


    private void checkAndResetTimePeriod() {
        // Если прошло времени больше чем timeUnit, то сбрасываем начало периода localDateTime в now() и обнуляем кол-во запросов countRequests
        if ((Duration.between(localDateTime, LocalDateTime.now()).toNanos() - timeUnit.toNanos(1)) > 0) {
            try {
                synchronized (lock) {
                    localDateTime = LocalDateTime.now();
                    countRequests = 0;
                    lock.notifyAll();
                }
            } catch (Exception e) {
            }
        }
    }
    @Scheduled(fixedRate = 1000)
    public void scheduledResetTimePeriod() {
        checkAndResetTimePeriod();
    }

    public void createDoc(RootProductPojo rootProductPojo) throws IOException {
        checkAndResetTimePeriod();
        try {
            boolean ok = true;
            synchronized (lock) {
                ok = countRequests++ < requestLimit; // true - количество запросов в период времени не превышено
            }
            if (!ok) {
                lock.wait(); // кол-во запросов превышено, ожидаем следующий период
            }
            doRequest(rootProductPojo);
        } catch (Exception e) {
        }
    }

    /**
     *
     * @param rootProductPojo - Pojo объект с описанием продукта, который преобразуется в JSON в теле запроса
     * @throws IOException
     */
    private void doRequest(RootProductPojo rootProductPojo) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(CREATE_DOC_URL);

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonProduct = objectMapper.writeValueAsString(rootProductPojo);

        final List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("body", jsonProduct));
        httpPost.setEntity(new UrlEncodedFormEntity(params));

        try (
                CloseableHttpResponse response2 = httpClient.execute(httpPost)
        ){
            final HttpEntity entity2 = response2.getEntity();
            System.out.println(EntityUtils.toString(entity2));
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        httpClient.close();
    }

    // вспомогательный метод, подготавливающий вариант Pojo для JSON
    public RootProductPojo prepareProductJson() {
        Description description = new Description();
        description.setParticipantInn("string");

        Product product = new Product();
        product.setCertificate_document("string");
        product.setCertificate_document_date("2020-01-23");
        product.setCertificate_document_number("string");
        product.setOwner_inn("string");
        product.setProducer_inn("string");
        product.setProduction_date("2020-01-23");
        product.setTnved_code("string");
        product.setUit_code("string");
        product.setUitu_code("string");

        RootProductPojo rootProductPojo = new RootProductPojo();
        rootProductPojo.setDescription(description);
        rootProductPojo.setProducts(new ArrayList<>(List.of(product)));
        rootProductPojo.setDoc_id("string");
        rootProductPojo.setDoc_status("string");
        rootProductPojo.setDoc_type("LP_INTRODUCE_GOODS");
        rootProductPojo.setImportRequest(true);
        rootProductPojo.setOwner_inn("string");
        rootProductPojo.setParticipant_inn("string");
        rootProductPojo.setProducer_inn("string");
        rootProductPojo.setProduction_date("2020-01-23");
        rootProductPojo.setProduction_type("string");
        rootProductPojo.setReg_date("2020-01-23");
        rootProductPojo.setReg_number("string");

        return rootProductPojo;
    }

    // -------Классы, описывающие продукт (будущий JSON) -----------------------
    @Data
    public class Description{
        public String participantInn;
    }

    @Data
    public class Product{
        public String certificate_document;
        public String certificate_document_date;
        public String certificate_document_number;
        public String owner_inn;
        public String producer_inn;
        public String production_date;
        public String tnved_code;
        public String uit_code;
        public String uitu_code;
    }

    @Data
    public class RootProductPojo{
        public Description description;
        public String doc_id;
        public String doc_status;
        public String doc_type;
        public boolean importRequest;
        public String owner_inn;
        public String participant_inn;
        public String producer_inn;
        public String production_date;
        public String production_type;
        public ArrayList<Product> products;
        public String reg_date;
        public String reg_number;
    }

}
