package API;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.http.Consts;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class FormData {
    private CredentialsProvider getCredentialsProvider() {
        Dotenv dotenv = Dotenv.load();
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(dotenv.get("BASIC_AUTH_USERNAME"), dotenv.get("BASIC_AUTH_PASSWORD").toCharArray());
        AuthScope scope = new AuthScope(null, null, -1, null, null);
        provider.setCredentials(scope, credentials);
        return provider;
    }

    public CompletableFuture<CloseableHttpResponse> get(JSONObject formDataJson, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpClient client = HttpClientBuilder
                        .create()
                        .setDefaultCredentialsProvider(getCredentialsProvider())
                        .build();
                HttpGet get = new HttpGet(url);
                get.setEntity(buildMultipartJson(formDataJson));
                return client.execute(get);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<CloseableHttpResponse> post(JSONObject formDataJson, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpClient client = HttpClientBuilder
                        .create()
                        .setDefaultCredentialsProvider(getCredentialsProvider())
                        .build();
                HttpPost post = new HttpPost(url);
                post.setEntity(buildMultipartJson(formDataJson));
                return client.execute(post);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<CloseableHttpResponse> delete(JSONObject formDataJson, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpClient client = HttpClientBuilder
                        .create()
                        .setDefaultCredentialsProvider(getCredentialsProvider())
                        .build();
                HttpDelete delete = new HttpDelete(url);
                delete.setEntity(buildMultipartJson(formDataJson));
                return client.execute(delete);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public CompletableFuture<CloseableHttpResponse> put(JSONObject formDataJson, String url) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                CloseableHttpClient client = HttpClientBuilder
                        .create()
                        .setDefaultCredentialsProvider(getCredentialsProvider())
                        .build();
                HttpPut put = new HttpPut(url);
                put.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
                put.setEntity(buildUrlMultipartJson(formDataJson));
                return client.execute(put);
            }
            catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private HttpEntity buildMultipartJson(JSONObject body) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.EXTENDED);

        Iterator<String> keys = body.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            builder.addTextBody(key, body.getString(key));
        }
        return builder.build();
    }

    private HttpEntity buildUrlMultipartJson(JSONObject body) {
        ArrayList<NameValuePair> formData = new ArrayList<>();
        Iterator<String> keys = body.keys();
        while(keys.hasNext()) {
            String key = keys.next();
            formData.add(new BasicNameValuePair(key, body.getString(key)));
        }
        return new UrlEncodedFormEntity(formData, Consts.UTF_8);
    }
}
