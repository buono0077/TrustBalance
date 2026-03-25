package Progetto;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

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

                System.out.println("Utente " + idUtente +
                        " offre " + String.format("%.2f £", offerta));
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