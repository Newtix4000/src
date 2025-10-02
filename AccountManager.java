import java.math.BigDecimal;
import java.util.*;

class AccountManager {
    private final Map<String, Account> accountsByKey = new LinkedHashMap<>();
    public void add(Account a) { accountsByKey.put(key(a.getBlz(), a.getNumber()), a); }
    public void close(String blz, String number) throws BankException {
        Account a = accountsByKey.remove(key(blz, number));
        if (a == null) throw new BankException("Konto nicht gefunden.");
    }
    public Account find(String blz, String number) throws BankException {
        Account a = accountsByKey.get(key(blz, number));
        if (a == null) throw new BankException("Konto nicht gefunden.");
        return a;
    }
    public Collection<Account> list() { return accountsByKey.values(); }
    public void transfer(Account from, Account to, BigDecimal amount) throws BankException {
        Account.requirePositive(amount);
        if (!from.canWithdraw(amount))
            throw new BankException("Quelle deckt Betrag nicht. Verfügbar: " + from.getBalance().add(from.overdraft));
        from.withdraw(amount);
        to.deposit(amount);
    }
    public void simulateMonthly() { for (Account a : accountsByKey.values()) a.onMonthlyCycle(); }
    public String nextAccountNumber(String blz) {
        int count = (int) accountsByKey.keySet().stream().filter(k -> k.startsWith(blz + "#")).count() + 1;
        return String.format("%05d", count);
    }
    private static String key(String blz, String nr) { return blz + "#" + nr; }
}