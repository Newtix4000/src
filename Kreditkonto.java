import java.math.BigDecimal;

class Kreditkonto extends Account {
    private final BigDecimal creditLimit;
    private final BigDecimal yearlyRate;
    public Kreditkonto(String owner, String blz, String number, BigDecimal monthlyFee, BigDecimal creditLimit, BigDecimal yearlyRate) {
        super(owner, blz, number, "Kreditkonto", monthlyFee);
        this.creditLimit = creditLimit != null ? creditLimit : BigDecimal.ZERO;
        this.yearlyRate = yearlyRate != null ? yearlyRate : BigDecimal.ZERO;
        this.overdraft = this.creditLimit;
    }
    @Override public void onMonthlyCycle() {
        super.onMonthlyCycle();
        if (getBalance().compareTo(BigDecimal.ZERO) < 0 && yearlyRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthly = yearlyRate.divide(new BigDecimal("12"), 10, java.math.RoundingMode.HALF_UP);
            BigDecimal interest = getBalance().abs().multiply(monthly).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
            this.balance = this.balance.subtract(interest);
            log("Sollzinsen -" + interest + " (" + yearlyRate + "% p.a.), neuer Stand=" + this.balance);
        }
    }
}