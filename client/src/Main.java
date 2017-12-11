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

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {public void run() {
            Object[] btc = check("ltc");

            if(btc == null) {
                System.out.println("Warning: Failed to fetch prices.");
            } else 
            //Object[] eth = check("eth");
            //Object[] ltc = check("ltc");

            spend(exchange, btc); // can't do multiple currencies cause shared `last` variable
        }}, 0, 10 * 60000);
    }

    protected static double last = 0;

    public static void spend(Exchange exchange, Object[] data) {
        double difference = 2;
        double range = 5;
        double risk = 5;

        double multiplier = 0;

        boolean buy = (boolean) data[0];
        boolean sell = (boolean) data[1];
        boolean trading = false;
        double confidence = (double) data[2];

        if(buy) {
            if(last < -range || last == 0 || Math.abs(confidence - last) >= difference) {
                last = confidence;
                multiplier = Math.min(1, confidence / 100 * risk);
                trading = true;
            }
        } else if(sell) {
            if(last > range || last == 0 || Math.abs(confidence - last) >= difference) {
                last = confidence;
                multiplier = Math.min(1, confidence / -100 * risk);
                trading = true;
            }
        }

        if(trading) {
            double tradingcoins = exchange.getCoins() * multiplier;

            if(multiplier > 0 && multiplier < 1) {
                boolean transacted = false;

                if(sell) {
                    if(tradingcoins < exchange.getCoins()) {
                        System.out.println("    Selling " + tradingcoins + " coins for +$" + exchange.toFiat(tradingcoins) + " USD.");
                        exchange.sell(tradingcoins);
                        transacted = true;
                    }
                } else if(buy) {
                    if(exchange.toFiat(tradingcoins) < exchange.getFiat()) {
                        System.out.println("    Buying " + tradingcoins + " coins for -$" + exchange.toFiat(tradingcoins) + " USD.");
                        exchange.buy(tradingcoins);
                        transacted = true;
                    }
                } else {
                    System.out.println("Warning: Invalid trade attempt detected. Cannot use buy or sell.");
                }

                if(transacted) {
                    System.out.println("    Transaction complete. Current balance: [ " + exchange.getCoins() + " coins, $" + exchange.getFiat() + "USD ].");
                }

                //System.out.println(confidence + ": " + multiplier + " * " + exchange.getCoins() + " = " + sellcoins;
            } else {
                System.out.println("Warning: Invalid trade attempt detected. Debug info to follow...\n    " + exchange.getCoins() + " coins * " + multiplier + " = null. Multiplier invalid.");
            }
        }
    }

    public static Object[] check(String currency) {
        double currentPrice = getPrice(currency, CURRENCY);

        double hourPrice = getPrice(currency, CURRENCY, getDate("hour", -2));
        try {Thread.sleep(1000);}catch(Exception e){}
        double dayPrice = getPrice(currency, CURRENCY, getDate("day", -1));
        try {Thread.sleep(1000);}catch(Exception e){}
        double weekPrice = getPrice(currency, CURRENCY, getDate("week", -1));
System.out.println(currentPrice + " and " + hourPrice + " and " + dayPrice);
        if(currentPrice == -1 || hourPrice == -1 || dayPrice == -1 || weekPrice == -1)
            return null;

        double hourPercentage = getPercent(currentPrice, hourPrice);
        double dayPercentage = getPercent(currentPrice, dayPrice);
        double weekPercentage = getPercent(currentPrice, weekPrice);

        double buyorsell = 0;

        buyorsell -= hourPercentage + 1;
        buyorsell -= dayPercentage / 2;
        buyorsell -= weekPercentage / 4;

        double range = 5;

        boolean buy = buyorsell > range;
        boolean sell = buyorsell < -range;

        System.out.print("\r" + (int)buyorsell + ": " + (buy ? "BUY" : (sell ? "SELL" : "NO ACTION")) + ": " + currency.toUpperCase() + "        ");

        return new Object[] {buy, sell, buyorsell};
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
