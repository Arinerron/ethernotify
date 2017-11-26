import org.json.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class Main {

    public static final String CURRENCY = "usd";
    public static final String CRYPTOCURRENCY = "eth";

    /*
     * API Docs:
     * History -- https://etherchain.org/documentation/api/#api-Statistics-GetStatisticsPrice
     * Current -- https://api.coinbase.com/v2/prices/BTC-USD/spot
     */


    public static void main(String[] args) {
        Exchange exchange = new ExchangeSimulator();

        System.out.println("INITIAL: " + exchange.getCoins() + " coins and $" + exchange.getFiat());
        exchange.buy(100);
        System.out.println("BUY 100: " + exchange.getCoins() + " coins and $" + exchange.getFiat());
        exchange.sell(0.5);
        System.out.println("SELL .5: " + exchange.getCoins() + " coins and $" + exchange.getFiat());

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {public void run() {
            check("btc");
            check("eth");
            check("ltc");
        }}, 0, 30000);
    }

    public static void check(String currency) {
        double currentPrice = getPrice(currency, CURRENCY);

        double hourPrice = getPrice(currency, CURRENCY, getDate("hour", -2));
        double dayPrice = getPrice(currency, CURRENCY, getDate("day", -1));
        double weekPrice = getPrice(currency, CURRENCY, getDate("week", -1));

        double hourPercentage = getPercent(currentPrice, hourPrice);
        double dayPercentage = getPercent(currentPrice, dayPrice);
        double weekPercentage = getPercent(currentPrice, weekPrice);

        double buyorsell = 0;

        buyorsell -= hourPercentage;
        buyorsell -= dayPercentage / 2;
        buyorsell -= weekPercentage / 4;

        double range = 5;

        boolean buy = buyorsell > range;
        boolean sell = buyorsell < -range;

        System.out.println((int)buyorsell + ": " + (buy ? "BUY" : (sell ? "SELL" : "NO ACTION")) + ": " + currency.toUpperCase());

        //System.out.println(hourPercentage + "\n" + dayPercentage + "\n" + weekPercentage);
    }

    public static double getPercent(double from, double to) {
        return (double) (100 * (from - to) / from);
    }

    public static String getDate(String unit, int amount) {
        SimpleDateFormat sdf = new SimpleDateFormat("y-M-d k:m:s");
        sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        Calendar calendar = Calendar.getInstance();

        switch(unit.toLowerCase().trim()) {
            case "minute":
                calendar.add(Calendar.MINUTE, amount);
                break;
            case "hour":
                calendar.add(Calendar.HOUR_OF_DAY, amount);
                break;
            case "day":
                calendar.add(Calendar.DAY_OF_MONTH, amount);
                break;
            case "week":
                calendar.add(Calendar.WEEK_OF_MONTH, amount);
                break;
            case "month":
                calendar.add(Calendar.MONTH, amount);
                break;
            case "year":
                calendar.add(Calendar.YEAR, amount);
                break;
        }

        return sdf.format(calendar.getTime());
    }

    public static double getPrice(String cryptocurrency, String currency) {
        try {
            String json = doGET("https://api.coinbase.com/v2/prices/" + cryptocurrency.toUpperCase() + "-" + currency.toUpperCase() + "/spot");
            JSONObject object = new JSONObject(json);
            return object.getJSONObject("data").getDouble("amount");
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static double getPrice(String cryptocurrency, String currency, String date) {
        try {
            String json = doGET("https://api.coinbase.com/v2/prices/" + cryptocurrency.toUpperCase() + "-" + currency.toUpperCase() + "/spot?date=" + URLEncoder.encode(date));
            JSONObject object = new JSONObject(json);
            return object.getJSONObject("data").getDouble("amount");
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
        conn.setRequestProperty("CB-VERSION", "2017-01-01");
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
