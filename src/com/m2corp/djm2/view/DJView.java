// Em view/DJView.java
package com.m2corp.djm2.view;

import com.m2corp.djm2.model.DJTable;
import com.m2corp.djm2.model.AudioTrack;
import com.m2corp.djm2.service.DemucsService;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class DJView {
    private final DJTable djTable;
    private JFrame frame;
    private JPanel tracksPanel;
    private JSlider progressSlider;
    private boolean isUserDraggingSlider = false;
    private JMenuItem openMenuItem;
    private JMenuItem separateMenuItem;
    private JTextArea logArea;

    public DJView(DJTable table) {
        this.djTable = table;
    }

    public void show() {
        frame = new JFrame("DJM2 - AI Stem Separator & Player");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.setMinimumSize(new Dimension(700, 500));

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                djTable.stopAllTracks();
                try { Thread.sleep(200); } catch (InterruptedException e) {}
                System.exit(0);
            }
        });

        createMenuBar();
        initComponents();

        djTable.startProgressUpdater(this::updateProgressGUI);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        openMenuItem = new JMenuItem("Abrir lista de instrumentos (.wav)...");
        openMenuItem.addActionListener(e -> openPreseparatedTracks());

        separateMenuItem = new JMenuItem("Separar música e carregar instrumentos");
        separateMenuItem.addActionListener(e -> separateAndLoadSong());

        fileMenu.add(separateMenuItem);
        fileMenu.add(openMenuItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);
    }

    private void initComponents() {
        tracksPanel = new JPanel();
        tracksPanel.setLayout(new BoxLayout(tracksPanel, BoxLayout.Y_AXIS));
        tracksPanel.setBorder(BorderFactory.createTitledBorder("Tracks"));
        tracksPanel.add(new JLabel("Use the 'File' menu to begin."));

        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setBorder(BorderFactory.createTitledBorder("Processing Log"));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tracksPanel), new JScrollPane(logArea));
        splitPane.setResizeWeight(0.7);

        frame.add(splitPane, BorderLayout.CENTER);

        frame.add(createMasterControlPanel(), BorderLayout.SOUTH);
    }

    private void openPreseparatedTracks() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione um ou mais arquivos .WAV");
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        fileChooser.setMultiSelectionEnabled(true);

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File[] audioFiles = fileChooser.getSelectedFiles();
            djTable.loadSong(audioFiles);
            updateTrackPanels();
        }
    }

    private void separateAndLoadSong() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione uma música para separar (wav)");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        if (fileChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File inputFile = fileChooser.getSelectedFile();

            separateMenuItem.setEnabled(false);
            openMenuItem.setEnabled(false);
            logArea.setText("");

            new SwingWorker<File[], String>() {
                private final DemucsService demucsService = new DemucsService();

                @Override
                protected File[] doInBackground() throws Exception {
                    return demucsService.separate(inputFile, this::publish);
                }

                @Override
                protected void process(List<String> chunks) {
                    for (String line : chunks) {
                        logArea.append(line + "\n");
                    }
                }

                @Override
                protected void done() {
                    try {
                        File[] stems = get();
                        djTable.loadSong(stems);
                        updateTrackPanels();
                    } catch (Exception e) {
                        logArea.append("\nERROR: " + e.getMessage());
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "An error occurred during separation.", "Error", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        separateMenuItem.setEnabled(true);
                        openMenuItem.setEnabled(true);
                    }
                }
            }.execute();
        }
    }

    private JPanel createMasterControlPanel() {
        JPanel masterPanel = new JPanel(new BorderLayout(10, 0));
        masterPanel.setBorder(BorderFactory.createTitledBorder("Controles"));

        JButton playPauseButton = new JButton("Pause");
        playPauseButton.addActionListener(e -> {
            if (djTable.isPlaying()) {
                djTable.pauseAll();
                playPauseButton.setText("Play");
            } else {
                djTable.resumeAll();
                playPauseButton.setText("Pause");
            }
        });

        progressSlider = new JSlider(0, 1000, 0);
        progressSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent e) {
                isUserDraggingSlider = true;
            }
            public void mouseReleased(java.awt.event.MouseEvent e) {
                long songLength = djTable.getSongLengthMicroseconds();
                if (songLength > 0) {
                    long newPosition = (long) (((double)progressSlider.getValue() / progressSlider.getMaximum()) * songLength);
                    djTable.seekAll(newPosition);
                }
                isUserDraggingSlider = false;
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(playPauseButton);

        masterPanel.add(buttonPanel, BorderLayout.WEST);
        masterPanel.add(progressSlider, BorderLayout.CENTER);
        return masterPanel;
    }

    private void updateProgressGUI(long currentPosition) {
        if (isUserDraggingSlider) return;

        long songLength = djTable.getSongLengthMicroseconds();
        if (songLength > 0 && progressSlider != null) {
            int sliderValue = (int) (((double)currentPosition / songLength) * progressSlider.getMaximum());

            SwingUtilities.invokeLater(() -> {
                progressSlider.setValue(sliderValue);
            });
        }
    }

    private void updateTrackPanels() {
        tracksPanel.removeAll();
        List<AudioTrack> tracks = djTable.getTracks();

        if (tracks.isEmpty()) {
            tracksPanel.add(new JLabel("Carregue os instrumentos usando o menu 'File'"));
        } else {
            for (AudioTrack track : tracks) {
                tracksPanel.add(createTrackPanel(track));
            }
        }

        if (progressSlider != null) {
            progressSlider.setValue(0);
        }

        tracksPanel.revalidate();
        tracksPanel.repaint();
        frame.pack();
    }

    private JPanel createTrackPanel(AudioTrack track) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JLabel label = new JLabel(track.getName());
        label.setPreferredSize(new Dimension(250, 20));
        JButton muteButton = new JButton(track.isMuted() ? "Desmutar" : "Mutar");
        muteButton.addActionListener(e -> {
            track.toggleMute();
            muteButton.setText(track.isMuted() ? "Desmutar" : "Mutar");
        });
        JSlider volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.addChangeListener(e -> {
            track.setVolume(volumeSlider.getValue());
        });
        track.setVolume(volumeSlider.getValue());
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlsPanel.add(muteButton);
        controlsPanel.add(new JLabel("Volume:"));
        controlsPanel.add(volumeSlider);
        panel.add(label, BorderLayout.WEST);
        panel.add(controlsPanel, BorderLayout.CENTER);
        return panel;
    }
}