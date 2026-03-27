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
  

        setTitle("UrbanScoot - Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 1, 10, 10)); // Layout a griglia semplice
        setLocationRelativeTo(null); // Centra nello schermo

        // Pannello Username
        JPanel panelUser = new JPanel();
        panelUser.add(new JLabel("Username:"));
        txtUsername = new JTextField(15);
        panelUser.add(txtUsername);

        // Pannello Password
        JPanel panelPass = new JPanel();
        panelPass.add(new JLabel("Password:"));
        txtPassword = new JPasswordField(15);
        panelPass.add(txtPassword);

        // Pannello Bottone
        JPanel panelBtn = new JPanel();
        JButton btnLogin = new JButton("Accedi");
        
        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());

            // Chiediamo al Controller di verificare
            String ruolo = controller.autenticaUtente(user, pass);

            if (ruolo.equals("ADMIN")) {
                JOptionPane.showMessageDialog(this, "Benvenuto Amministratore!");
                apriMappa(true); // Passiamo true = è Admin
            } else if (ruolo.equals("UTENTE")) {
                JOptionPane.showMessageDialog(this, "Accesso effettuato.");
                apriMappa(false); // Passiamo false = è Utente semplice
            } else {
                JOptionPane.showMessageDialog(this, "Credenziali errate!", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        panelBtn.add(btnLogin);

        add(panelUser);
        add(panelPass);
        add(panelBtn);
    }

    private void apriMappa(boolean isAdmin) {
        // Chiude la finestra di login e apre la mappa!
        this.dispose(); 
        new FinestraMappa(isAdmin).setVisible(true);
    }

    public static void main(String[] args) {
        UrbanScoot.getInstance().loadDatiIniziali();
    
        // Il vero avvio ora è il Login:
        SwingUtilities.invokeLater(() -> new FinestraLogin().setVisible(true));
    }
}