package API;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class FormData {
    public CompletableFuture<CloseableHttpResponse> post(JSONObject formDataJson, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpClient client = HttpClients.createDefault();
                HttpPost post = new HttpPost(url);
                MultipartEntityBuilder builder = MultipartEntityBuilder.create();

                Iterator<String> keys = formDataJson.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    builder.addTextBody(key, formDataJson.getString(key));
                }
                post.setEntity(builder.build());
                return client.execute(post);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }
}
