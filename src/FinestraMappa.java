import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class FinestraMappa extends JFrame {
    private UrbanScoot controller;
    private MappaPanel pannello;
    private boolean isAdminSession;

    public FinestraMappa(boolean isAdmin) {
        this.isAdminSession = isAdmin;
        controller = UrbanScoot.getInstance();

        setTitle("UrbanScoot - " + (isAdmin ? "Pannello Admin" : "App Utente"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); 

        pannello = new MappaPanel(controller, isAdmin);
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
            // Istituzioni per l'admin
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
        add(pannello, BorderLayout.CENTER); 

        pack(); 
        setLocationRelativeTo(null); 
    }
}

class MappaPanel extends JPanel {
    private UrbanScoot controller;
    private boolean isAdmin;
    private final int DIM_CELLA = 50; 
    
    // 0 = Strada, 1 = Palazzo, 3 = PARCHEGGIO (P) - Ora sono 5 coppie (10 caselle tot)
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

    public MappaPanel(UrbanScoot controller, boolean isAdmin) {
        this.controller = controller;
        this.isAdmin = isAdmin;

        // Genera le Trappole Nere (Valore 2)
        generaTrappolaCasuale();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inMovimento) return; 

                int colC = e.getX() / DIM_CELLA;
                int rigaC = e.getY() / DIM_CELLA;
                if (rigaC < 0 || rigaC >= mappaLayout.length || colC < 0 || colC >= mappaLayout[0].length) return;
                
                if (mappaLayout[rigaC][colC] == 1) return; // I palazzi sono bloccati per tutti

                // --- 1. MODALITÀ AGGIUNTA (Solo Admin) ---
                if (modalitaAggiunta && isAdmin) {
                    if (mappaLayout[rigaC][colC] == 3) { // Solo sui parcheggi!
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

                // --- 2. GESTIONE A CORSA FERMA (Noleggio o Riparazione) ---
                if (!corsaAttiva) {
                    for (Monopattino m : controller.getFlotta()) {
                        if ((int)m.getLatitudine() == rigaC && (int)m.getLongitudine() == colC) {
                            
                            // Poteri esclusivi dell'Admin: Riparare e Dismettere
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
                            
                            // Se il veicolo è sano, TUTTI possono noleggiarlo (Utente e Admin)
                            avviaNoleggio(m, rigaC, colC);
                            return; 
                        }
                    }
                } 
                // --- 3. GESTIONE A CORSA ATTIVA (Parcheggio) ---
                else {
                    // Sosta consentita solo sulla 'P' per tutti!
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

                        // Qui il Controller leggerà l'Admin e vedrà i suoi 9999 EUR!
                        if (!controller.puoPermettersiCorsa(itinerario.size())) {
                            JOptionPane.showMessageDialog(MappaPanel.this, "Credito insufficiente per questo tragitto!", "Errore", JOptionPane.ERROR_MESSAGE);
                            return; 
                        }
                        avviaAnimazioneViaggio(itinerario, rigaC, colC);
                    }
                }
            }
        });
    }

    // Metodo helper per separare i conti!
    private void avviaNoleggio(Monopattino m, int rigaC, int colC) {
        if (controller.isVeicoloInManutenzione(m.getId()) || controller.isVeicoloDismesso(m.getId())) {
            JOptionPane.showMessageDialog(MappaPanel.this, "Questo veicolo è guasto o dismesso!", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int scelta = JOptionPane.showConfirmDialog(null, "Vuoi noleggiare " + m.getId() + "?", "Avvia", JOptionPane.YES_NO_OPTION);
        if (scelta == JOptionPane.YES_OPTION) {
            
            // ECCO IL FIX DEI CONTI SEPARATI:
           // Sceglie l'account giusto in base a chi ha fatto il Login!
        String idAccount = isAdmin ? "ADMIN-01" : "USR-01";
        controller.inserisciCodiceQR(m.getId(), idAccount);
            
            if (controller.confermaAvvioCorsa()) {
                corsaAttiva = true;
                idVeicoloInUso = m.getId();
                scooterR = rigaC;
                scooterC = colC;
                repaint();
            } else {
                JOptionPane.showMessageDialog(MappaPanel.this, "Impossibile avviare! Batteria/Credito bassi.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void generaTrappolaCasuale() {
        Random rand = new Random();
        int trappolePiazzate = 0;
        // ORA PIAZZA 2 TRAPPOLE!
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
                        JOptionPane.showMessageDialog(MappaPanel.this, 
                            "BOOM! NINO è finito in una buca enorme!\nVeicolo distrutto irreparabilmente.", 
                            "Incidente", JOptionPane.ERROR_MESSAGE);
                        
                        controller.terminaCorsa(scooterR, scooterC, true, step);
                        
                        controller.getFlotta().stream().filter(m -> m.getId().equals(idVeicoloInUso))
                                  .findFirst().ifPresent(m -> m.setStato(new Dismesso()));

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
                    controller.terminaCorsa(destRealeR, destRealeC, false, itinerarioEffettivo.size() - 1);
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