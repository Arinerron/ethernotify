public class ExchangeSimulator extends Exchange {
    // sell x in coins
    @Override
    public boolean sell(double amount) {
        if(amount <= 0 || amount > this.getCoins())
            return false;

        this.setCoins(this.getCoins() - amount);
        this.setFiat(this.getFiat() + (amount * this.getFiatPerCoin()));

        return true;
    }

    // buy x in fiat
    @Override
    public boolean buy(double amount) {
        if(amount <= 0 || amount > this.getFiat())
            return false;

        this.setFiat(this.getFiat() - amount);
        this.setCoins(this.getCoins() + (amount / this.getFiatPerCoin()));

        return true;
    }
}
