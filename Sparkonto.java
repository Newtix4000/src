import java.math.BigDecimal;

class Sparkonto extends Account {
    private final BigDecimal yearlyRate;
    public Sparkonto(String owner, String blz, String number, BigDecimal monthlyFee, BigDecimal startBalance, BigDecimal yearlyRate) {
        super(owner, blz, number, "Sparkonto", monthlyFee);
        if (startBalance != null && startBalance.compareTo(BigDecimal.ZERO) > 0) { this.balance = startBalance; }
        this.yearlyRate = yearlyRate != null ? yearlyRate : BigDecimal.ZERO;
    }
    @Override public void onMonthlyCycle() {
        super.onMonthlyCycle();
        if (yearlyRate.compareTo(BigDecimal.ZERO) > 0 && getBalance().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthly = yearlyRate.divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
            BigDecimal interest = getBalance().multiply(monthly).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            this.balance = this.balance.add(interest);
            log("Zinsen +" + interest + " (" + yearlyRate + "% p.a.), neuer Stand=" + this.balance);
        }
    }
    @Override protected boolean canWithdraw(BigDecimal amount) {
        return amount.compareTo(getBalance()) <= 0;
    }
}