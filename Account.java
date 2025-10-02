import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

abstract class Account {
    private final String owner;
    private final String blz;
    private final String number;
    private final String type;
    private final BigDecimal monthlyFee;
    protected BigDecimal balance = BigDecimal.ZERO;
    protected BigDecimal overdraft = BigDecimal.ZERO;
    private final List<String> log = new ArrayList<>();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected Account(String owner, String blz, String number, String type, BigDecimal monthlyFee) {
        this.owner = owner; this.blz = blz; this.number = number; this.type = type; this.monthlyFee = monthlyFee;
        log("Konto eröffnet: " + type + ", Gebühr/Monat=" + monthlyFee);
    }
    public String getOwner() { return owner; }
    public String getBlz() { return blz; }
    public String getNumber() { return number; }
    public String getType() { return type; }
    public BigDecimal getBalance() { return balance; }
    public BigDecimal getMonthlyFee() { return monthlyFee; }

    public void deposit(BigDecimal amount) throws BankException {
        requirePositive(amount);
        balance = balance.add(amount);
        log("Einzahlung +" + amount + ", neuer Stand=" + balance);
    }
    public void withdraw(BigDecimal amount) throws BankException {
        requirePositive(amount);
        if (!canWithdraw(amount)) throw new BankException("Limit überschritten. Verfügbar: " + availableAmount());
        balance = balance.subtract(amount);
        log("Abhebung -" + amount + ", neuer Stand=" + balance);
    }

    protected boolean canWithdraw(BigDecimal amount) {
        BigDecimal limit = balance.add(overdraft);
        return amount.compareTo(limit) <= 0;
    }
    protected BigDecimal availableAmount() { return balance.add(overdraft); }
    protected void log(String entry) { log.add(LocalDateTime.now().format(fmt) + " | " + entry); }

    public String printStatement() {
        StringBuilder sb = new StringBuilder();
        sb.append("Kontoauszug ").append(type).append(" (BLZ ").append(blz).append(", Nr ").append(number).append(")\n");
        sb.append("Inhaber: ").append(owner).append("\n");
        sb.append("Stand: ").append(balance).append(" EUR\n");
        sb.append("Überziehungsrahmen: ").append(overdraft).append(" EUR\n");
        sb.append("Monatliche Gebühr: ").append(monthlyFee).append(" EUR\n");
        sb.append("--- Buchungen/Notizen ---\n");
        for (String e : log) sb.append(e).append('\n');
        return sb.toString();
    }

    public void onMonthlyCycle() {
        if (monthlyFee.compareTo(BigDecimal.ZERO) > 0) {
            balance = balance.subtract(monthlyFee);
            log("Kontoführungsgebühr -" + monthlyFee + ", neuer Stand=" + balance);
        }
    }

    protected static void requirePositive(BigDecimal amount) throws BankException {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new BankException("Betrag muss > 0 sein.");
    }

    @Override public String toString() {
        return String.format("%s | BLZ %s | Nr %s | Inhaber %s | Stand %s EUR",
                type, blz, number, owner, balance);
    }
}