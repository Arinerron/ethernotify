# Pseudocode
```

repeat (60 sec) {
    update list of prices for cryptocurrencies on watchlist;

    for each (watchlist, cryptocurrency) {
        # could just make these if statements a function like check(yearly_array, 1 year) or something
        if (current time - time when yearly price was taken last >= 1 year) {
            yearly price -> add (current time, current price);
        }
        if (current time - time when monthly price was taken last >= 1 month) {
            monthly price -> add (current time, current price);
        }
        if (current time - time when weekly price was taken last >= 1 week) {
            weekly price -> add (current time, current price);
        }
        if (current time - time when daily price was taken last >= 1 day) {
            daily price -> add (current time, current price);
        }
        if (current time - time when hourly price was taken last >= 1 hour) {
            hourly price -> add (current time, current price);
        }

        analyze (cryptocurrency);
    }
}

analyze (cryptocurrency) {
    # IDEA: use database of prices; update every minute. For hourly, simply fetch row (current minute - 60)

    last price yearly = last price (yearly);
    last price monthly = last price (monthly);
    last price weekly = last price (weekly);
    last price daily = last price (daily);
    last price hourly = last price (hourly);
    last price minutely = last price (minutely);

    yearly percent change = get percent change (last price yearly, current price);
    monthly percent change = get percent change (last price monthly, current price);
    weekly percent change = get percent change (last price weekly, current price);
    daily percent change = get percent change (last price daily, current price);
    hourly percent change = get percent change (last price hourly, current price);
    minutely percent change = get percent change (last price minutely, current price);

    if (minutely percent change < -95) { # it flash crashed crazy!
        suggest(buy, MOST IMPORTANT);
    } else if (hourly percent change < -95) { # it flash crashed
        suggest(buy, IMPORTANT);
    } else if ()
}

price at (time) {
    return (item in array where (minute = time));
}

last price (time_array) {
    return (last item in time array);
}

get percent change (from, to) {
    return (100 * (to - from) / from);
}

```
