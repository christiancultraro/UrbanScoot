package ui;

import javax.swing.*;
import controller.UrbanScoot;
import java.awt.*;

public class FinestraLogin extends JFrame {
    private UrbanScoot controller;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public FinestraLogin() {
        controller = UrbanScoot.getInstance();

        setTitle("UrbanScoot - Login & Registrazione");
        setSize(320, 250); 
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10)); 
        setLocationRelativeTo(null); 

        // 1. Pannello Username
        JPanel panelUser = new JPanel();
        panelUser.add(new JLabel("Username:"));
        txtUsername = new JTextField(15);
        panelUser.add(txtUsername);

        // 2. Pannello Password
        JPanel panelPass = new JPanel();
        panelPass.add(new JLabel("Password:"));
        txtPassword = new JPasswordField(15);
        panelPass.add(txtPassword);

        // 3. Pannello Bottoni Login & Registrati
        JPanel panelBtn = new JPanel();
        JButton btnLogin = new JButton("Accedi");
        JButton btnRegistrati = new JButton("Registrati");
        
        panelBtn.add(btnLogin);
        panelBtn.add(btnRegistrati);

        // --- AZIONE: LOGIN ---
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            String ruolo = controller.autenticaUtente(user, pass);

            if (ruolo.equals("ADMIN")) {
                JOptionPane.showMessageDialog(this, "Benvenuto Amministratore!");
                // Ora passiamo correttamente il parametro 'user'
                apriMappa(true, user); 
            } else if (ruolo.equals("UTENTE")) {
                JOptionPane.showMessageDialog(this, "Accesso effettuato.");
                // Ora passiamo correttamente il parametro 'user'
                apriMappa(false, user); 
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali errate!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- AZIONE: REGISTRAZIONE ---
        btnRegistrati.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Inserisci Username e Password per registrarti.", "Attenzione", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int scelta = JOptionPane.showConfirmDialog(this, 
                "Per registrarti devi collegare una carta di credito.\nVuoi simulare il collegamento ora?", 
                "Collega Carta", JOptionPane.YES_NO_OPTION);

            if (scelta == JOptionPane.YES_OPTION) {
                float creditoAssegnato = controller.registraNuovoUtente(user, pass);

                if (creditoAssegnato != -1.0f) {
                    JOptionPane.showMessageDialog(this, 
                        "Registrazione completata!\nCarta collegata. Credito caricato dalla banca: " + creditoAssegnato + "€", 
                        "Successo", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Username già esistente. Scegline un altro.", "Errore Registrazione", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        add(panelUser);
        add(panelPass);
        add(panelBtn);
    }

    // --- IL METODO AGGIORNATO CHE CAUSAVA L'ERRORE ---
    private void apriMappa(boolean isAdmin, String username) {
        this.dispose(); 
        new FinestraMappa(isAdmin, username).setVisible(true);
    }

    public static void main(String[] args) {
        UrbanScoot.getInstance().loadDatiIniziali();
        SwingUtilities.invokeLater(() -> new FinestraLogin().setVisible(true));
    }
}