package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import javax.swing.Timer;

import controller.UrbanScoot;
import dominio.Monopattino;
import dominio.Corsa; 
import stati.EsitoOperazione; 

public class FinestraMappa extends JFrame {
    private UrbanScoot controller;
    private MappaPanel pannello;
    private boolean isAdminSession;
    private String usernameSession;

    public FinestraMappa(boolean isAdmin, String username) {
        this.isAdminSession = isAdmin;
        this.usernameSession = username;
        controller = UrbanScoot.getInstance();

        setTitle("UrbanScoot - " + (isAdmin ? "Pannello Admin" : "App Utente: " + username));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); 

        pannello = new MappaPanel(controller, isAdmin, username);
        pannello.setPreferredSize(new Dimension(800, 600));

        // --- BARRA SUPERIORE ---
        JPanel barraSuperiore = new JPanel(new BorderLayout());
        barraSuperiore.setBackground(Color.DARK_GRAY);

        JPanel adminPanel = new JPanel();
        adminPanel.setOpaque(false); 
        
        if (isAdmin) {
            JButton btnAggiungi = new JButton("➕ Aggiungi Veicolo (UC2)");
            btnAggiungi.setBackground(Color.CYAN);
            btnAggiungi.addActionListener(e -> {
                pannello.attivaModalitaAggiunta();
                JOptionPane.showMessageDialog(this, "Clicca su un Parcheggio (P) per far spawnare il monopattino!");
            });
            adminPanel.add(btnAggiungi);
            JLabel lblInfo = new JLabel("  (Clicca sui veicoli Arancioni per Ripararli, Neri per Dismetterli)");
            lblInfo.setForeground(Color.WHITE);
            adminPanel.add(lblInfo);
        }

        JPanel logoutPanel = new JPanel();
        logoutPanel.setOpaque(false);
        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.addActionListener(e -> {
            this.dispose(); 
            new FinestraLogin().setVisible(true);
        });
        logoutPanel.add(btnLogout);
        
        barraSuperiore.add(adminPanel, BorderLayout.WEST);
        barraSuperiore.add(logoutPanel, BorderLayout.EAST);

        add(barraSuperiore, BorderLayout.NORTH); 

        // --- GESTIONE SCHEDE (TAB) PER L'UTENTE ---
        if (!isAdmin) {
            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.addTab("📍 Mappa & Noleggio", pannello);
            tabbedPane.addTab("💳 Ricarica Credito", creaPannelloRicarica());
            tabbedPane.addTab("📜 Storico Corse", creaPannelloStorico());
            
            add(tabbedPane, BorderLayout.CENTER);
        } else {
            add(pannello, BorderLayout.CENTER); 
        }

        pack(); 
        setLocationRelativeTo(null); 
    }

    private JPanel creaPannelloRicarica() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel lblTitolo = new JLabel("Ricarica il tuo Portafoglio");
        lblTitolo.setFont(new Font("Arial", Font.BOLD, 20));
        
        JLabel lblImporto = new JLabel("Inserisci l'importo (€):");
        JTextField txtImporto = new JTextField(10);
        txtImporto.setFont(new Font("Arial", Font.PLAIN, 16));
        
        JButton btnRicarica = new JButton("Conferma Ricarica");
        btnRicarica.setBackground(new Color(0, 153, 76));
        btnRicarica.setForeground(Color.WHITE);
        
        JLabel lblMessaggio = new JLabel(" ");
        lblMessaggio.setFont(new Font("Arial", Font.BOLD, 14));

        btnRicarica.addActionListener(e -> {
            try {
                float importo = Float.parseFloat(txtImporto.getText().replace(",", "."));
                float nuovoCredito = controller.ricaricaCredito(importo);
                
                if (nuovoCredito != -1.0f) {
                    lblMessaggio.setText("✅ Ricarica effettuata con successo! Nuovo saldo: " + String.format("%.2f", nuovoCredito) + " €");
                    lblMessaggio.setForeground(new Color(0, 150, 0));
                    txtImporto.setText(""); 
                } else {
                    lblMessaggio.setText("❌ Errore durante la ricarica.");
                    lblMessaggio.setForeground(Color.RED);
                }
            } catch (NumberFormatException ex) {
                lblMessaggio.setText("❌ Inserisci un importo valido (es. 10.50).");
                lblMessaggio.setForeground(Color.RED);
            }
        });

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; panel.add(lblTitolo, gbc);
        gbc.gridy = 1; gbc.gridwidth = 1; panel.add(lblImporto, gbc);
        gbc.gridx = 1; panel.add(txtImporto, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; panel.add(btnRicarica, gbc);
        gbc.gridy = 3; panel.add(lblMessaggio, gbc);

        return panel;
    }

    private JPanel creaPannelloStorico() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JButton btnAggiorna = new JButton("🔄 Aggiorna Storico");
        JTextArea txtStorico = new JTextArea();
        txtStorico.setEditable(false);
        txtStorico.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(txtStorico);

        btnAggiorna.addActionListener(e -> {
            txtStorico.setText("=== STORICO CORSE DI " + usernameSession.toUpperCase() + " ===\n\n");
            
            List<Corsa> corse = controller.richiediStoricoCorse();
            
            if (corse.isEmpty()) {
                txtStorico.append(" Nessuna corsa effettuata finora.\n");
            } else {
              for (Corsa c : corse) {
                    txtStorico.append("🛴 Veicolo:\t" + c.getMonopattino().getId() + "\n");
                    txtStorico.append("🕒 Data:\t" + c.getOrarioInizio().toLocalDate() + " " + c.getOrarioInizio().toLocalTime().withNano(0) + "\n");
                    txtStorico.append("💶 Costo:\t" + String.format("%.2f", c.getCostoTotale()) + " €\n");
                    txtStorico.append("--------------------------------------------------\n");
                }
            }
        });

        panel.add(btnAggiorna, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }
}

class MappaPanel extends JPanel {
    private UrbanScoot controller;
    private boolean isAdmin;
    private String usernameUtenteCorrente; 
    private final int DIM_CELLA = 50; 
    
    private int[][] mappaLayout = {
        {0, 0, 0, 1, 0, 0, 0, 0, 3, 3, 0, 0, 0, 0, 0, 0},
        {0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 0},
        {0, 1, 3, 3, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 3, 3, 0},
        {1, 1, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        {3, 3, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0},
        {0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
        {0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0},
        {0, 0, 0, 1, 0, 0, 0, 3, 3, 0, 0, 0, 1, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    };

    private boolean corsaAttiva = false;
    private boolean inMovimento = false; 
    private boolean modalitaAggiunta = false;
    private int contatoreVeicoli = 2; 
    
    private String idVeicoloInUso = "";
    private int scooterR = 0; 
    private int scooterC = 0;

    public MappaPanel(UrbanScoot controller, boolean isAdmin, String username) {
        this.controller = controller;
        this.isAdmin = isAdmin;
        this.usernameUtenteCorrente = username; 

        generaTrappolaCasuale();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inMovimento) return; 

                int colC = e.getX() / DIM_CELLA;
                int rigaC = e.getY() / DIM_CELLA;
                if (rigaC < 0 || rigaC >= mappaLayout.length || colC < 0 || colC >= mappaLayout[0].length) return;
                
                if (mappaLayout[rigaC][colC] == 1) return; 

                if (modalitaAggiunta && isAdmin) {
                    if (mappaLayout[rigaC][colC] == 3) {
                        String nuovoId = "MONO-0" + contatoreVeicoli++;
                        controller.aggiungiNuovoMonopattino(nuovoId, rigaC, colC);
                        modalitaAggiunta = false;
                        repaint();
                    } else {
                        JOptionPane.showMessageDialog(MappaPanel.this, 
                            "Operazione non consentita!\nI nuovi veicoli vanno posizionati nelle Zone di Sosta (P).", 
                            "Vincolo di Dominio", JOptionPane.WARNING_MESSAGE);
                    }
                    return;
                }

                if (!corsaAttiva) {
                    for (Monopattino m : controller.getFlotta()) {
                        if ((int)m.getLatitudine() == rigaC && (int)m.getLongitudine() == colC) {
                            
                            if (isAdmin && controller.isVeicoloInManutenzione(m.getId())) {
                                controller.prendiInCarico(m.getId());
                                controller.registraManutenzioneCompletata(m.getId());
                                JOptionPane.showMessageDialog(MappaPanel.this, "Veicolo Riparato! (UC4)");
                                repaint();
                                return;
                            } else if (isAdmin && controller.isVeicoloDismesso(m.getId())) {
                                int s = JOptionPane.showConfirmDialog(null, "Rimuovere definitivamente?", "Dismetti", JOptionPane.YES_NO_OPTION);
                                if (s == JOptionPane.YES_OPTION) {
                                    controller.dismettiVeicolo(m.getId());
                                    repaint();
                                }
                                return;
                            }
                            
                            avviaNoleggio(m, rigaC, colC);
                            return; 
                        }
                    }
                } 
                else {
                    if (mappaLayout[rigaC][colC] != 3) {
                        JOptionPane.showMessageDialog(MappaPanel.this, 
                            "Errore Sosta Vietata!\nDevi parcheggiare nelle zone delimitate dalla 'P' azzurra.", 
                            "Estensione 4a UC3", JOptionPane.WARNING_MESSAGE);
                        return; 
                    }

                    int scelta = JOptionPane.showConfirmDialog(null, "Parcheggiare qui?", "Termina", JOptionPane.YES_NO_OPTION);
                    if (scelta == JOptionPane.YES_OPTION) {
                        List<Point> itinerario = trovaPercorso(scooterR, scooterC, rigaC, colC);
                        if (itinerario.isEmpty()) return;

                        if (!controller.puoPermettersiCorsa(itinerario.size())) {
                            JOptionPane.showMessageDialog(MappaPanel.this, 
                                "Credito insufficiente per questa distanza!\nLa corsa verrà terminata nell'attuale posizione.", 
                                "Fondi Insufficienti", JOptionPane.WARNING_MESSAGE);
                            
                            // RISOLUZIONE BUG: se l'utente non ha soldi per questo tragitto, terminiamo 
                            // la corsa sul posto in modo che il monopattino torni disponibile.
                            controller.terminaCorsa(scooterR, scooterC, false, 0);
                            corsaAttiva = false;
                            inMovimento = false;
                            idVeicoloInUso = "";
                            repaint();
                            return; 
                        }
                        avviaAnimazioneViaggio(itinerario, rigaC, colC);
                    }
                }
            }
        });
    }

    private void avviaNoleggio(Monopattino m, int rigaC, int colC) {
        if (isAdmin) {
            JOptionPane.showMessageDialog(MappaPanel.this, 
                "Errore: Gli account Amministratore non sono abilitati al noleggio.", 
                "Accesso Negato", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int scelta = JOptionPane.showConfirmDialog(null, "Vuoi noleggiare " + m.getId() + "?", "Avvia", JOptionPane.YES_NO_OPTION);
        if (scelta == JOptionPane.YES_OPTION) {
            
            // 1. Invio del QR e verifica dell'esito
            EsitoOperazione esitoQR = controller.inserisciCodiceQR(m.getId(), this.usernameUtenteCorrente);
            if (esitoQR != EsitoOperazione.SUCCESSO) {
                gestisciErroreStato(esitoQR);
                return;
            }

            // RISOLUZIONE BUG: Verifichiamo a priori se l'utente possiede almeno i fondi per lo scatto iniziale
            if (!controller.puoPermettersiCorsa(0)) {
                JOptionPane.showMessageDialog(this, "Credito insufficiente per coprire i costi di sblocco.", "Fondi Insufficienti", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 2. Avvio effettivo e verifica dell'esito
            EsitoOperazione esitoAvvio = controller.confermaAvvioCorsa();
            if (esitoAvvio == EsitoOperazione.SUCCESSO) {
                corsaAttiva = true;
                idVeicoloInUso = m.getId();
                scooterR = rigaC;
                scooterC = colC;
                repaint();
            } else {
                gestisciErroreStato(esitoAvvio);
            }
        }
    }

    private void gestisciErroreStato(EsitoOperazione esito) {
        switch (esito) {
            case VEICOLO_IN_USO:
                JOptionPane.showMessageDialog(this, "Il veicolo è già in uso.", "Non Disponibile", JOptionPane.WARNING_MESSAGE);
                break;
            case VEICOLO_IN_MANUTENZIONE:
            case VEICOLO_IN_RIPARAZIONE:
                JOptionPane.showMessageDialog(this, "Il veicolo è attualmente guasto o in riparazione.", "Manutenzione", JOptionPane.WARNING_MESSAGE);
                break;
            case VEICOLO_DISMESSO:
                JOptionPane.showMessageDialog(this, "Il veicolo è stato dismesso e non può essere utilizzato.", "Dismesso", JOptionPane.ERROR_MESSAGE);
                break;
            case OPERAZIONE_NON_CONSENTITA:
                JOptionPane.showMessageDialog(this, "Operazione non consentita.\nVerifica di avere credito sufficiente o di non aver scansionato il veicolo errato.", "Accesso Negato", JOptionPane.ERROR_MESSAGE);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Si è verificato un errore imprevisto.", "Errore", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
    
    // Metodo helper per recuperare lo stato della batteria aggiornata
    private int getBatteriaAggiornata(String idVeicolo) {
        for (Monopattino m : controller.getFlotta()) {
            if (m.getId().equals(idVeicolo)) {
                return m.getBatteria();
            }
        }
        return 0;
    }

    private void generaTrappolaCasuale() {
        Random rand = new Random();
        int trappolePiazzate = 0;
        while (trappolePiazzate < 2) {
            int r = rand.nextInt(mappaLayout.length);
            int c = rand.nextInt(mappaLayout[0].length);
            if (mappaLayout[r][c] == 0) {
                mappaLayout[r][c] = 2; 
                trappolePiazzate++;
            }
        }
    }

    public void attivaModalitaAggiunta() { this.modalitaAggiunta = true; }

    private List<Point> trovaPercorso(int startR, int startC, int endR, int endC) {
        List<Point> percorso = new ArrayList<>();
        boolean[][] visitati = new boolean[mappaLayout.length][mappaLayout[0].length];
        Point[][] padri = new Point[mappaLayout.length][mappaLayout[0].length]; 
        Queue<Point> coda = new LinkedList<>();
        
        coda.add(new Point(startR, startC));
        visitati[startR][startC] = true;
        int[] dr = {-1, 1, 0, 0}; 
        int[] dc = {0, 0, -1, 1};
        boolean trovato = false;

        while (!coda.isEmpty()) {
            Point p = coda.poll();
            if (p.x == endR && p.y == endC) { trovato = true; break; }
            for (int i = 0; i < 4; i++) {
                int nr = p.x + dr[i], nc = p.y + dc[i];
                if (nr >= 0 && nr < mappaLayout.length && nc >= 0 && nc < mappaLayout[0].length 
                    && (mappaLayout[nr][nc] == 0 || mappaLayout[nr][nc] == 3 || mappaLayout[nr][nc] == 2) 
                    && !visitati[nr][nc]) {
                    
                    visitati[nr][nc] = true;
                    padri[nr][nc] = p;
                    coda.add(new Point(nr, nc));
                }
            }
        }
        if (trovato) {
            Point cur = new Point(endR, endC);
            while (cur != null && !(cur.x == startR && cur.y == startC)) {
                percorso.add(cur);
                cur = padri[cur.x][cur.y];
            }
            Collections.reverse(percorso);
        }
        return percorso;
    }

   private void avviaAnimazioneViaggio(List<Point> itinerario, int destR, int destC) {
        inMovimento = true;
        int maxPassi = Math.max(0, controller.getBatteriaSelezionato() / 4); 
        
        final List<Point> itinerarioEffettivo;
        if ((itinerario.size() - 1) > maxPassi) {
            JOptionPane.showMessageDialog(this, "Batteria in esaurimento! Si fermerà a metà strada.", "Attenzione", JOptionPane.WARNING_MESSAGE);
            itinerarioEffettivo = itinerario.subList(0, maxPassi + 1);
        } else {
            itinerarioEffettivo = itinerario;
        }

        Timer timer = new Timer(150, new ActionListener() {
            int step = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (step < itinerarioEffettivo.size()) {
                    scooterR = itinerarioEffettivo.get(step).x;
                    scooterC = itinerarioEffettivo.get(step).y;
                    repaint();
                    
                    if (mappaLayout[scooterR][scooterC] == 2) {
                        ((Timer) e.getSource()).stop(); 
                        
                        float costo = controller.terminaCorsa(scooterR, scooterC, true, step);
                        float saldoResiduo = controller.ricaricaCredito(0); // Interroghiamo il sistema a costo 0
                        int batteriaResidua = getBatteriaAggiornata(idVeicoloInUso);
                        
                        JOptionPane.showMessageDialog(MappaPanel.this, 
                            "A seguito di impatto con una buca il monopattino risulta danneggiato!\n" +
                            "La corsa è stata rimborsata per il disagio.\n\n" +
                            "Costo Addebitato: " + String.format("%.2f", costo) + " €\n" +
                            "Credito Residuo: " + String.format("%.2f", saldoResiduo) + " €\n" +
                            "Batteria Monopattino: " + batteriaResidua + "%",
                            "Ricevuta - Incidente", JOptionPane.ERROR_MESSAGE);
                        
                        controller.segnaVeicoloDismesso(idVeicoloInUso); 

                        corsaAttiva = false;
                        inMovimento = false;
                        idVeicoloInUso = ""; 
                        repaint();
                        return;
                    }
                    
                    step++;
                } else {
                    ((Timer) e.getSource()).stop(); 
                    int destRealeR = itinerarioEffettivo.get(itinerarioEffettivo.size() - 1).x;
                    int destRealeC = itinerarioEffettivo.get(itinerarioEffettivo.size() - 1).y;
                    
                    float costo = controller.terminaCorsa(destRealeR, destRealeC, false, itinerarioEffettivo.size() - 1);
                    float saldoResiduo = controller.ricaricaCredito(0);
                    int batteriaResidua = getBatteriaAggiornata(idVeicoloInUso);
                    
                    JOptionPane.showMessageDialog(MappaPanel.this, 
                        "Hai parcheggiato correttamente il veicolo.\n\n" +
                        "Distanza percorsa: " + (itinerarioEffettivo.size() - 1) + " passi\n" +
                        "Costo Addebitato: " + String.format("%.2f", costo) + " €\n" +
                        "Credito Residuo: " + String.format("%.2f", saldoResiduo) + " €\n" +
                        "Batteria Monopattino: " + batteriaResidua + "%\n\n" +
                        "Il tuo saldo è stato aggiornato.",
                        "Ricevuta Corsa", JOptionPane.INFORMATION_MESSAGE);
                        
                    corsaAttiva = false;
                    inMovimento = false;
                    idVeicoloInUso = ""; 
                    repaint();
                }
            }
        });
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        for (int r = 0; r < mappaLayout.length; r++) {
            for (int c = 0; c < mappaLayout[0].length; c++) {
                int x = c * DIM_CELLA, y = r * DIM_CELLA;
                
                if (mappaLayout[r][c] == 1) {
                    g2d.setColor(Color.DARK_GRAY); 
                } else if (mappaLayout[r][c] == 3) {
                    g2d.setColor(new Color(173, 216, 230)); 
                } else if (mappaLayout[r][c] == 2) {
                    g2d.setColor(Color.BLACK); 
                } else {
                    g2d.setColor(Color.LIGHT_GRAY); 
                }
                
                g2d.fillRect(x, y, DIM_CELLA, DIM_CELLA);
                g2d.setColor(Color.BLACK);
                g2d.drawRect(x, y, DIM_CELLA, DIM_CELLA);

                if (mappaLayout[r][c] == 3) {
                    g2d.setColor(Color.BLUE);
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("P", x + 18, y + 30);
                }
            }
        }

        for (Monopattino m : controller.getFlotta()) {
            if (corsaAttiva && m.getId().equals(idVeicoloInUso)) continue; 

            int px = (int)m.getLongitudine() * DIM_CELLA;
            int py = (int)m.getLatitudine() * DIM_CELLA;

            if (controller.isVeicoloDismesso(m.getId())) {
                g2d.setColor(Color.BLACK); 
            } else if (controller.isVeicoloInManutenzione(m.getId())) {
                g2d.setColor(Color.ORANGE); 
            } else {
                g2d.setColor(Color.GREEN); 
            }
            
            g2d.fillOval(px + 5, py + 5, DIM_CELLA - 10, DIM_CELLA - 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString(m.getId().replace("MONO-", ""), px + 18, py + 28);
        }

        if (corsaAttiva) {
            int px = scooterC * DIM_CELLA;
            int py = scooterR * DIM_CELLA;
            g2d.setColor(Color.RED); 
            g2d.fillOval(px + 5, py + 5, DIM_CELLA - 10, DIM_CELLA - 10);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            g2d.drawString("IN USO", px + 8, py + 28);
        }
    }
}