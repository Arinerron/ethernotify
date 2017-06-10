import org.json.*;
import java.io.*;
import java.net.*;

public class Main {

    public static final String CURRENCY = "usd"; // must be lowercase
    public static final String CRYPTOCURRENCY = "eth";

    /*
     * API Docs:
     * History -- https://etherchain.org/documentation/api/#api-Statistics-GetStatisticsPrice
     * Current -- https://coinmarketcap-nexuist.rhcloud.com/api/eth
     */


    public static void main(String[] args) {
        System.out.println("Current " + CRYPTOCURRENCY.toUpperCase() + " Price: " + getPrice(CRYPTOCURRENCY, CURRENCY) + " " + CURRENCY.toUpperCase());
    }

    public static double getPrice(String cryptocurrency, String currency) {
        try {
            String json = doGET("https://coinmarketcap-nexuist.rhcloud.com/api/" + cryptocurrency);
            JSONObject object = new JSONObject(json);
            return object.getJSONObject("price").getDouble(currency);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // performs a GET request
    private static String doGET(String url2) throws Exception {
        URL url = new URL(url2);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", "EtherNotify/1.0");
        conn.setRequestProperty("Charset", "utf-8");
        conn.setUseCaches(false);
        conn.setDoOutput(true);

        BufferedReader in = new BufferedReader(
		        new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		in.close();

        return response.toString();
    }
}
