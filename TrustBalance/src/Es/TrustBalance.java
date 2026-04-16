package Es;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

// ----------- MODEL -----------
class ContoBancario {
    private double saldo;
    private final ReentrantLock lock = new ReentrantLock();

    public ContoBancario(double saldoIniziale) {
        this.saldo = saldoIniziale;
    }

    public void preleva(double importo) {
        lock.lock();
        try {
            if (saldo >= importo) {
                saldo -= importo;
            }
        } finally {
            lock.unlock();
        }
    }

    public double getSaldo() {
        lock.lock();
        try {
            return saldo;
        } finally {
            lock.unlock();
        }
    }
}

class AstaBOT {
    private double prezzoCorrente = 80.0;
    private int vincitore = -1;
    private final ReentrantLock lock = new ReentrantLock();

    public void faiOfferta(int idUtente, double offerta) {
        lock.lock();
        try {
            if (offerta > prezzoCorrente) {
                prezzoCorrente = offerta;
                vincitore = idUtente;
            }
        } finally {
            lock.unlock();
        }
    }

    public int getVincitore() {
        return vincitore;
    }

    public double getPrezzoFinale() {
        return prezzoCorrente;
    }
}

class Investitore extends Thread {
    private final int id;
    private final AstaBOT asta;
    private final Random random = new Random();

    public Investitore(int id, AstaBOT asta) {
        this.id = id;
        this.asta = asta;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(random.nextInt(50));
        } catch (InterruptedException e) {}

        double offerta = 80 + (20 * random.nextDouble());
        offerta = Math.round(offerta * 100.0) / 100.0;

        asta.faiOfferta(id, offerta);
    }
}

// ----------- DATI UTENTE -----------
class DatiUtente {
    double saldoIniziale;
    double totaleAste = 0;
    int asteVinte = 0;
    double speseMensili;
    double saldoFinale;
}

// ----------- SIMULAZIONE -----------
class Simulazione {
    public static List<DatiUtente> esegui() throws InterruptedException {

        final int NUM_UTENTI = 50;
        final int NUM_ASTE = 5;

        List<ContoBancario> conti = new ArrayList<>();
        List<DatiUtente> dati = new ArrayList<>();
        Random random = new Random();

        // inizializzazione
        for (int i = 0; i < NUM_UTENTI; i++) {
            double saldo = 5000 + random.nextInt(45001);
            conti.add(new ContoBancario(saldo));

            DatiUtente d = new DatiUtente();
            d.saldoIniziale = saldo;
            dati.add(d);
        }

        // aste
        for (int i = 0; i < NUM_ASTE; i++) {
            AstaBOT asta = new AstaBOT();
            List<Thread> threads = new ArrayList<>();

            for (int j = 0; j < NUM_UTENTI; j++) {
                Thread t = new Investitore(j, asta);
                threads.add(t);
                t.start();
            }

            for (Thread t : threads) {
                t.join();
            }

            int vincitore = asta.getVincitore();
            double prezzo = asta.getPrezzoFinale();

            if (vincitore != -1) {
                conti.get(vincitore).preleva(prezzo);
                dati.get(vincitore).totaleAste += prezzo;
                dati.get(vincitore).asteVinte++;
            }
        }

        // spese mensili
        for (int i = 0; i < NUM_UTENTI; i++) {
            int spesa = 1000 + random.nextInt(2001);
            conti.get(i).preleva(spesa);

            dati.get(i).speseMensili = spesa;
            dati.get(i).saldoFinale = conti.get(i).getSaldo();
        }

        return dati;
    }
}

// ----------- GUI -----------
public class TrustBalance {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TrustBalance::login);
    }

    private static void login() {
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        Object[] message = {
                "Username:", userField,
                "Password:", passField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Login", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String user = userField.getText();
            String pass = new String(passField.getPassword());

            try {
                List<DatiUtente> dati = Simulazione.esegui();

                if (user.equals("admin") && pass.equals("a")) {
                    mostraAdmin(dati);
                } else if (user.equals("utente") && pass.equals("b")) {
                    mostraUtente(dati.get(0));
                } else {
                    JOptionPane.showMessageDialog(null, "Credenziali errate");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void mostraAdmin(List<DatiUtente> dati) {
        JFrame frame = new JFrame("Admin - Report completo");
        JTextArea area = new JTextArea();

        for (int i = 0; i < dati.size(); i++) {
            DatiUtente d = dati.get(i);

            area.append("Utente " + i + "\n");
            area.append("Saldo iniziale: " + d.saldoIniziale + "\n");
            area.append("Totale speso in aste: " + d.totaleAste + "\n");
            area.append("Aste vinte: " + d.asteVinte + "\n");
            area.append("Spese mensili: " + d.speseMensili + "\n");
            area.append("Saldo finale: " + d.saldoFinale + "\n");
            area.append("------------------------\n");
        }

        frame.add(new JScrollPane(area));
        frame.setSize(500, 600);
        frame.setVisible(true);
    }

    private static void mostraUtente(DatiUtente d) {
        JFrame frame = new JFrame("Utente 0 - Report");
        JTextArea area = new JTextArea();

        area.append("Saldo iniziale: " + d.saldoIniziale + "\n");
        area.append("Totale speso in aste: " + d.totaleAste + "\n");
        area.append("Aste vinte: " + d.asteVinte + "\n");
        area.append("Spese mensili: " + d.speseMensili + "\n");
        area.append("Saldo finale: " + d.saldoFinale + "\n");

        frame.add(area);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }
}