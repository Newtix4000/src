import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.math.BigDecimal;

public class BankAppGUI extends JFrame {

    private final AccountManager manager = new AccountManager();
    private final JTextArea out = new JTextArea(30, 60);

    private final JTextField tfOwner = new JTextField(20);
    private final JTextField tfBLZ = new JTextField(10);
    private final JTextField tfGebuehr = new JTextField(8);
    private final JComboBox<String> cbTyp = new JComboBox<>(new String[]{"Girokonto", "Sparkonto", "Kreditkonto"});

    private final JTextField tfOverdraft = new JTextField(8);
    private final JTextField tfStart = new JTextField(8);
    private final JTextField tfRate = new JTextField(8);
    private final JTextField tfLimit = new JTextField(8);

    private final JTextField tfNr = new JTextField(6);
    private final JTextField tfBetrag = new JTextField(8);
    private final JTextField tfZielBLZ = new JTextField(10);
    private final JTextField tfZielNr = new JTextField(6);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankAppGUI gui = new BankAppGUI();
            gui.setVisible(true);
        });
    }

    public BankAppGUI() {
        super("Kontoverwaltung (GUI)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        seedDemoData();

        out.setEditable(false);
        out.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(out);
        scroll.setBorder(new TitledBorder("Ausgaben"));
        add(scroll, BorderLayout.CENTER);

        JPanel left = new JPanel(new GridBagLayout());
        left.setBorder(new TitledBorder("Aktionen"));
        add(left, BorderLayout.WEST);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0; c.gridy = 0;

        JPanel pCreate = new JPanel(new GridBagLayout());
        pCreate.setBorder(new TitledBorder("Konto anlegen"));
        addLabeled(pCreate, "Inhaber:", tfOwner, 0);
        addLabeled(pCreate, "BLZ:", tfBLZ, 1);
        addLabeled(pCreate, "Gebühr/Monat:", tfGebuehr, 2);
        addLabeled(pCreate, "Kontoart:", cbTyp, 3);

        JPanel pTyp = new JPanel(new GridBagLayout());
        pTyp.setBorder(new TitledBorder("Typ-Details"));
        updateTypePanel(pTyp);
        cbTyp.addActionListener(e -> updateTypePanel(pTyp));

        JButton btAnlegen = new JButton("Konto anlegen");
        btAnlegen.addActionListener(e -> createAccount());

        GridBagConstraints cIn = new GridBagConstraints();
        cIn.insets = new Insets(4,4,4,4);
        cIn.anchor = GridBagConstraints.WEST; cIn.fill = GridBagConstraints.HORIZONTAL;
        cIn.gridx = 0; cIn.gridy = 4; cIn.gridwidth = 2;
        pCreate.add(pTyp, cIn);

        cIn.gridy = 5; cIn.gridwidth = 1;
        pCreate.add(btAnlegen, cIn);

        left.add(pCreate, c);

        c.gridy++;
        JPanel pOps = new JPanel(new GridBagLayout());
        pOps.setBorder(new TitledBorder("Operationen"));

        addLabeled(pOps, "Nr:", tfNr, 0);
        addLabeled(pOps, "Betrag:", tfBetrag, 1);
        addLabeled(pOps, "Ziel-BLZ:", tfZielBLZ, 2);
        addLabeled(pOps, "Ziel-Nr:", tfZielNr, 3);

        JButton btEinzahlen = new JButton("Einzahlen");
        btEinzahlen.addActionListener(e -> deposit());
        JButton btAbheben = new JButton("Abheben");
        btAbheben.addActionListener(e -> withdraw());
        JButton btUeberweisen = new JButton("Überweisen");
        btUeberweisen.addActionListener(e -> transfer());
        JButton btAuszug = new JButton("Kontoauszug");
        btAuszug.addActionListener(e -> statement());
        JButton btSchliessen = new JButton("Konto schließen");
        btSchliessen.addActionListener(e -> closeAccount());
        JButton btAlle = new JButton("Alle Konten");
        btAlle.addActionListener(e -> listAccounts());
        JButton btMonat = new JButton("Monat simulieren");
        btMonat.addActionListener(e -> simulateMonthly());

        GridBagConstraints co = new GridBagConstraints();
        co.insets = new Insets(4,4,4,4);
        co.gridx = 0; co.gridy = 4; co.fill = GridBagConstraints.HORIZONTAL; co.weightx = 1;
        pOps.add(btEinzahlen, co);
        co.gridy++; pOps.add(btAbheben, co);
        co.gridy++; pOps.add(btUeberweisen, co);
        co.gridy++; pOps.add(btAuszug, co);
        co.gridy++; pOps.add(btSchliessen, co);
        co.gridy++; pOps.add(btAlle, co);
        co.gridy++; pOps.add(btMonat, co);

        left.add(pOps, c);

        pack();
        setLocationByPlatform(true);
        appendln("Demodaten erstellt.");
        listAccounts();
    }

    private void addLabeled(JPanel p, String label, JComponent comp, int row) {
        GridBagConstraints cL = new GridBagConstraints();
        cL.insets = new Insets(4,4,4,4);
        cL.gridx = 0; cL.gridy = row; cL.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(label), cL);

        GridBagConstraints cC = new GridBagConstraints();
        cC.insets = new Insets(4,4,4,4);
        cC.gridx = 1; cC.gridy = row; cC.fill = GridBagConstraints.HORIZONTAL; cC.weightx = 1.0;
        p.add(comp, cC);
    }

    private void updateTypePanel(JPanel pTyp) {
        pTyp.removeAll();
        String sel = (String) cbTyp.getSelectedItem();
        int r = 0;
        if ("Girokonto".equals(sel)) {
            addLabeled(pTyp, "Überziehungsrahmen:", tfOverdraft, r++);
        } else if ("Sparkonto".equals(sel)) {
            addLabeled(pTyp, "Startguthaben:", tfStart, r++);
            addLabeled(pTyp, "Zinssatz p.a. (%):", tfRate, r++);
        } else if ("Kreditkonto".equals(sel)) {
            addLabeled(pTyp, "Kreditlimit:", tfLimit, r++);
            addLabeled(pTyp, "Sollzins p.a. (%):", tfRate, r++);
        }
        pTyp.revalidate();
        pTyp.repaint();
        pack();
    }

    private void appendln(String msg) {
        out.append(msg + "\n");
        out.setCaretPosition(out.getDocument().getLength());
    }

    private String nextNr(String blz) {
        return manager.nextAccountNumber(blz);
    }

    private Account selectAccountByInputs(String blz, String nr) throws BankException {
        if (blz == null || blz.isBlank()) throw new BankException("BLZ fehlt.");
        if (nr == null || nr.isBlank()) throw new BankException("Kontonummer fehlt.");
        return manager.find(blz.trim(), nr.trim());
    }

    private BigDecimal parseMoney(String s, String feld) throws BankException {
        if (s == null || s.isBlank()) throw new BankException(feld + " fehlt.");
        try { return new BigDecimal(s.trim()); }
        catch (Exception ex) { throw new BankException("Ungültiger Betrag in Feld: " + feld); }
    }

    private void createAccount() {
        try {
            String owner = tfOwner.getText().trim();
            String blz = tfBLZ.getText().trim();
            if (owner.isBlank()) throw new BankException("Inhaber fehlt.");
            if (blz.isBlank()) throw new BankException("BLZ fehlt.");
            BigDecimal fee = parseMoney(tfGebuehr.getText(), "Gebühr/Monat");
            String type = (String) cbTyp.getSelectedItem();
            Account acc;
            if ("Girokonto".equals(type)) {
                BigDecimal overdraft = parseMoney(tfOverdraft.getText(), "Überziehungsrahmen");
                acc = new Girokonto(owner, blz, nextNr(blz), fee, overdraft);
            } else if ("Sparkonto".equals(type)) {
                BigDecimal start = parseMoney(tfStart.getText(), "Startguthaben");
                BigDecimal rate = parseMoney(tfRate.getText(), "Zinssatz p.a.");
                acc = new Sparkonto(owner, blz, nextNr(blz), fee, start, rate);
            } else {
                BigDecimal limit = parseMoney(tfLimit.getText(), "Kreditlimit");
                BigDecimal creditRate = parseMoney(tfRate.getText(), "Sollzins p.a.");
                acc = new Kreditkonto(owner, blz, nextNr(blz), fee, limit, creditRate);
            }
            manager.add(acc);
            appendln("Angelegt: " + acc);
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void closeAccount() {
        try {
            String blz = tfBLZ.getText();
            String nr = tfNr.getText();
            manager.close(blz.trim(), nr.trim());
            appendln("Geschlossen: " + nr);
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void deposit() {
        try {
            Account a = selectAccountByInputs(tfBLZ.getText(), tfNr.getText());
            BigDecimal betrag = parseMoney(tfBetrag.getText(), "Betrag");
            a.deposit(betrag);
            appendln("Einzahlung OK. Neuer Stand: " + a.getBalance());
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void withdraw() {
        try {
            Account a = selectAccountByInputs(tfBLZ.getText(), tfNr.getText());
            BigDecimal betrag = parseMoney(tfBetrag.getText(), "Betrag");
            a.withdraw(betrag);
            appendln("Abhebung OK. Neuer Stand: " + a.getBalance());
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void transfer() {
        try {
            Account from = selectAccountByInputs(tfBLZ.getText(), tfNr.getText());
            Account to = selectAccountByInputs(tfZielBLZ.getText(), tfZielNr.getText());
            if (from.equals(to)) throw new BankException("Quelle = Ziel");
            BigDecimal betrag = parseMoney(tfBetrag.getText(), "Betrag");
            manager.transfer(from, to, betrag);
            appendln("Überweisung OK. Quelle: " + from.getBalance() + " | Ziel: " + to.getBalance());
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void statement() {
        try {
            Account a = selectAccountByInputs(tfBLZ.getText(), tfNr.getText());
            appendln(a.printStatement());
        } catch (BankException ex) {
            appendln("Fehler: " + ex.getMessage());
        } catch (Exception ex) {
            appendln("Unerwartet: " + ex);
        }
    }

    private void listAccounts() {
        boolean any = false;
        for (Account a : manager.list()) {
            appendln(a.toString());
            any = true;
        }
        if (!any) appendln("(keine Konten)");
    }

    private void simulateMonthly() {
        manager.simulateMonthly();
        appendln("Monat simuliert.");
    }

    private void seedDemoData() {
        Account a = new Girokonto("Jonas Leodolter", "BAWAG", nextNr("BAWAG"),
                new BigDecimal("3.50"), new BigDecimal("500"));
        try { a.deposit(new BigDecimal("1000")); } catch (Exception ignored) {}
        Account b = new Sparkonto("Testkonto", "BAWAG", nextNr("BAWAG"),
                new BigDecimal("0.00"), new BigDecimal("2500"), new BigDecimal("1.25"));
        manager.add(a);
        manager.add(b);
    }
}
