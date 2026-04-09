package compiler.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class CompilerIDE extends JFrame {

    private static final String TARGET_FILE = "test_script.spy";
    
    private JTabbedPane tabbedPane;
    private JTextArea mainEditor; // The main editor for test_script.spy
    private JTextArea consoleArea;
    private JPanel vizPanel;
    private JButton btnUpload;
    private JButton btnRun;

    public CompilerIDE() {
        setTitle("SpyLang Studio");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Set modern font
        Font mainFont = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("Button.font", mainFont);
        UIManager.put("Label.font", mainFont);

        initComponents();
        refreshVisualizations(); // Load any existing visualizations on startup
    }

    private void initComponents() {
        // --- LEFT PANEL (Controls & Visualizations) ---
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setPreferredSize(new Dimension(250, 0));
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 5));

        // Actions Panel
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        actionPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Actions", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        btnUpload = new JButton("Upload .spy File");
        btnUpload.setFocusPainted(false);
        btnUpload.addActionListener(this::handleUpload);

        btnRun = new JButton("Run Script");
        btnRun.setFocusPainted(false);
        btnRun.setBackground(new Color(46, 204, 113)); // Soft Green
        btnRun.setForeground(Color.WHITE);
        btnRun.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRun.addActionListener(this::handleRun);

        actionPanel.add(btnUpload);
        actionPanel.add(btnRun);

        // Visualizations Panel (Dynamic)
        vizPanel = new JPanel();
        vizPanel.setLayout(new BoxLayout(vizPanel, BoxLayout.Y_AXIS));
        JScrollPane vizScroll = new JScrollPane(vizPanel);
        vizScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Visualizations & Outputs", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));

        leftPanel.add(actionPanel, BorderLayout.NORTH);
        leftPanel.add(vizScroll, BorderLayout.CENTER);

        // --- CENTER PANEL (Tabbed Code Editor) ---
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        mainEditor = new JTextArea();
        mainEditor.setFont(new Font("Consolas", Font.PLAIN, 16));
        mainEditor.setMargin(new Insets(10, 10, 10, 10));
        mainEditor.setTabSize(4);
        
        // Load existing script if it exists
        File target = new File(TARGET_FILE);
        if (target.exists()) {
            try {
                mainEditor.setText(new String(Files.readAllBytes(target.toPath())));
            } catch (IOException ignored) {}
        } else {
            mainEditor.setText("# Write your SpyLang code here...\n");
        }

        JScrollPane mainEditorScroll = new JScrollPane(mainEditor);
        tabbedPane.addTab(TARGET_FILE, mainEditorScroll); // Permanent first tab

        JPanel editorContainer = new JPanel(new BorderLayout());
        editorContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Editor", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        editorContainer.add(tabbedPane, BorderLayout.CENTER);

        // --- BOTTOM PANEL (Console) ---
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setBackground(new Color(30, 30, 30)); // Dark theme
        consoleArea.setForeground(new Color(0, 255, 0)); // Hacker green text
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        consoleArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane consoleScroll = new JScrollPane(consoleArea);
        consoleScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), "Console Output", 
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12)));
        consoleScroll.setPreferredSize(new Dimension(0, 200));

        // Split Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, editorContainer, consoleScroll);
        splitPane.setResizeWeight(0.7); 
        splitPane.setBorder(new EmptyBorder(10, 5, 10, 10));

        add(leftPanel, BorderLayout.WEST);
        add(splitPane, BorderLayout.CENTER);
    }

    private void handleUpload(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Spy Files (*.spy)", "spy"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String content = new String(Files.readAllBytes(selectedFile.toPath()));
                mainEditor.setText(content);
                tabbedPane.setSelectedIndex(0); // Switch back to main tab
                logToConsole("Loaded " + selectedFile.getName() + " into the editor.\n");
            } catch (IOException ex) {
                logToConsole("Error reading file: " + ex.getMessage() + "\n");
            }
        }
    }

    private void handleRun(ActionEvent e) {
        try (FileWriter writer = new FileWriter(TARGET_FILE)) {
            writer.write(mainEditor.getText());
        } catch (IOException ex) {
            logToConsole("Failed to save script: " + ex.getMessage() + "\n");
            return;
        }

        btnRun.setEnabled(false);
        consoleArea.setText(""); 
        logToConsole("Executing...\n");
        logToConsole("--------------------------------------------------\n");

        new Thread(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                    "java",
                    "-XX:+ShowCodeDetailsInExceptionMessages",
                    "-cp",
                    "target\\classes",
                    "compiler.Main",
                    TARGET_FILE
                );
                pb.redirectErrorStream(true); 
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logToConsole(line + "\n");
                    }
                }

                int exitCode = process.waitFor();
                logToConsole("--------------------------------------------------\n");
                logToConsole("Process finished with exit code " + exitCode + "\n");

                SwingUtilities.invokeLater(this::refreshVisualizations);

            } catch (Exception ex) {
                logToConsole("Execution failed: " + ex.getMessage() + "\n");
            } finally {
                SwingUtilities.invokeLater(() -> btnRun.setEnabled(true));
            }
        }).start();
    }

    private void refreshVisualizations() {
        vizPanel.removeAll();
        
        File dir = new File(".");
        // ADDED: ".tkn" is now recognized as a valid output file
        List<String> validExtensions = Arrays.asList(".png", ".dot", ".svg", ".pdf", ".html", ".tkn");
        
        File[] files = dir.listFiles((d, name) -> {
            String lower = name.toLowerCase();
            return validExtensions.stream().anyMatch(lower::endsWith) || name.contains("symbol_table");
        });

        if (files != null && files.length > 0) {
            for (File f : files) {
                JButton btnViz = new JButton(f.getName());
                btnViz.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                btnViz.setFocusPainted(false);
                btnViz.setAlignmentX(Component.CENTER_ALIGNMENT);
                btnViz.addActionListener(e -> handleFileClick(f));
                
                vizPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                vizPanel.add(btnViz);
            }
        } else {
            JLabel lblNoViz = new JLabel("No visual files found.");
            lblNoViz.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblNoViz.setForeground(Color.GRAY);
            vizPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            vizPanel.add(lblNoViz);
        }

        vizPanel.revalidate();
        vizPanel.repaint();
    }

    private void handleFileClick(File file) {
        String name = file.getName().toLowerCase();
        
        // Prevent HTML files from opening in the text editor, even if they contain "symbol_table"
        if (!name.endsWith(".html") && (name.endsWith(".tkn") || name.endsWith(".txt") || name.contains("symbol_table"))) {
            openInTab(file);
        } else {
            // Otherwise open images, PDFs, and HTML files externally (in the browser/default viewer)
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                    logToConsole("Opened externally: " + file.getName() + "\n");
                } else {
                    logToConsole("Desktop operations not supported on this OS.\n");
                }
            } catch (IOException e) {
                logToConsole("Failed to open file " + file.getName() + ": " + e.getMessage() + "\n");
            }
        }
    }

    private void openInTab(File file) {
        // 1. Check if it's already open. If so, just focus that tab.
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(file.getName())) {
                tabbedPane.setSelectedIndex(i);
                
                // Optional: refresh the content if it changed
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    Component comp = tabbedPane.getComponentAt(i);
                    if (comp instanceof JScrollPane) {
                        JViewport viewport = ((JScrollPane) comp).getViewport();
                        if (viewport.getView() instanceof JTextArea) {
                            ((JTextArea) viewport.getView()).setText(content);
                        }
                    }
                } catch (IOException ignored) {}
                return;
            }
        }

        // 2. Not open yet, read the file and create a new tab
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            JTextArea textArea = new JTextArea(content);
            textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
            textArea.setMargin(new Insets(10, 10, 10, 10));
            textArea.setEditable(false); // Make outputs read-only

            JScrollPane scrollPane = new JScrollPane(textArea);
            tabbedPane.addTab(file.getName(), scrollPane);
            
            int newIndex = tabbedPane.getTabCount() - 1;
            tabbedPane.setSelectedIndex(newIndex);

            // 3. Add a neat close ('x') button to the tab header
            JPanel tabHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            tabHeader.setOpaque(false);
            
            JLabel lblTitle = new JLabel(file.getName());
            JButton btnClose = new JButton("x");
            btnClose.setFont(new Font("Arial", Font.BOLD, 12));
            btnClose.setMargin(new Insets(0, 2, 0, 2));
            btnClose.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
            btnClose.setContentAreaFilled(false);
            btnClose.setFocusPainted(false);
            btnClose.setToolTipText("Close Tab");
            btnClose.setForeground(Color.GRAY);
            
            // Hover effect for the close button
            btnClose.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btnClose.setForeground(Color.RED); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btnClose.setForeground(Color.GRAY); }
            });

            btnClose.addActionListener(e -> tabbedPane.remove(scrollPane));

            tabHeader.add(lblTitle);
            tabHeader.add(btnClose);
            tabbedPane.setTabComponentAt(newIndex, tabHeader);

            logToConsole("Opened " + file.getName() + " in a new tab.\n");

        } catch (IOException ex) {
            logToConsole("Failed to read " + file.getName() + ": " + ex.getMessage() + "\n");
        }
    }

    private void logToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            consoleArea.append(text);
            consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new CompilerIDE().setVisible(true);
        });
    }
}