public class Exchange {
    private double coins = 10;
    private double fiat = 1000;
    private double fpc = 500;

    public boolean sell(double amount) {return false;}
    public boolean buy(double amount) {return false;}


    public double getCoins() {
        return this.coins;
    }

    protected void setCoins(double coins) {
        this.coins = coins;
    }

    public double getFiat() {
        return this.fiat;
    }

    protected void setFiat(double fiat) {
        this.fiat = fiat;
    }

    public double getFiatPerCoin() {
        return this.fpc;
    }

    protected void setFiatPerCoin(double fpc) {
        this.fpc = fpc;
    }
}
