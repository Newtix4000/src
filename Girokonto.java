import java.math.BigDecimal;

class Girokonto extends Account {
    public Girokonto(String owner, String blz, String number, BigDecimal monthlyFee, BigDecimal overdraft) {
        super(owner, blz, number, "Girokonto", monthlyFee);
        this.overdraft = overdraft != null ? overdraft : BigDecimal.ZERO;
    }
}