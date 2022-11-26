package API;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class JavaFormData {
    private static final String CRLF = "\r\n";
    private static final String CHARSET = "UTF-8";

    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 10000;

    private final HttpURLConnection connection;
    private final OutputStream outputStream;
    private final PrintWriter writer;
    private final String boundary;

    private final URL url;
    private String getBasicAuth() {
        Dotenv dotenv = Dotenv.load();
        String basicAuthUsername = dotenv.get("BASIC_AUTH_USERNAME");
        String basicAuthPassword = dotenv.get("BASIC_AUTH_PASSWORD");
        String basicAuth = basicAuthUsername + ":" + basicAuthPassword;
        byte[] encodedBytes = Base64.encodeBase64(basicAuth.getBytes());
        return "Basic " + new String(encodedBytes);
    }

    public JavaFormData(final URL url) throws IOException {
        this.url = url;

        boundary = "---------------------------" + System.currentTimeMillis();

        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", getBasicAuth());
        connection.setRequestProperty("Accept-Charset", CHARSET);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),
                true);
    }

    public JavaFormData(final URL url, String method) throws IOException {
        this.url = url;

        boundary = "---------------------------" + System.currentTimeMillis();

        connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", getBasicAuth());
        connection.setRequestProperty("Accept-Charset", CHARSET);
        connection.setRequestProperty("Content-Type",
                "multipart/form-data; boundary=" + boundary);
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        outputStream = connection.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, CHARSET),
                true);
    }

    public void addFormField(final String name, final String value) {
        writer.append("--").append(boundary).append(CRLF)
                .append("Content-Disposition: form-data; name=\"").append(name)
                .append("\"").append(CRLF)
                .append("Content-Type: text/plain; charset=").append(CHARSET)
                .append(CRLF).append(CRLF).append(value).append(CRLF);
    }

    public String finish() throws IOException {
        writer.append(CRLF).append("--").append(boundary).append("--")
                .append(CRLF);
        writer.close();

        try  {
            final InputStream is = connection.getInputStream();

            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            StringBuffer response = new StringBuffer();
            String inputLine = in.readLine();
            while (inputLine != null) {
                response.append(inputLine);
                inputLine = in.readLine();
                if (inputLine != null)
                    response.append('\r');
            }
            in.close();
            isr.close();
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
}