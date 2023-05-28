import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class CompilerGUI {
    private JFrame frame;
    private JTextArea sourceCodeTextArea;
    private JButton analyzeButton;
    private DefaultTableModel tokensTableModel;
    private JTable tokensTable;
    private JTextArea analysisTextArea;
    private JScrollPane tokensScrollPane;
    private JScrollPane syntaxScrollPane;
    public CompilerGUI() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame();
        frame.setTitle("C++ Analyzer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("nimbusBase", Color.BLACK);
            UIManager.put("nimbusBlueGray", Color.DARK_GRAY);
            UIManager.put("control", new Color(40, 40, 40));
            UIManager.put("text", Color.WHITE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.BLACK);

        JPanel lineNumberPanel = new JPanel(new BorderLayout());
        lineNumberPanel.setFocusable(false);    
        lineNumberPanel.setBackground(Color.BLACK);

        JTextArea lineNumberTextArea = new JTextArea();
        lineNumberPanel.setFocusable(false);
        lineNumberTextArea.setEditable(false);
        lineNumberTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        lineNumberTextArea.setForeground(Color.LIGHT_GRAY);
        lineNumberTextArea.setBackground(Color.BLACK);
        lineNumberTextArea.setMargin(new Insets(0, 5, 0, 0));
        lineNumberPanel.add(lineNumberTextArea, BorderLayout.CENTER);

        sourceCodeTextArea = new JTextArea();
        
        sourceCodeTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        sourceCodeTextArea.setForeground(Color.WHITE);
        sourceCodeTextArea.setBackground(Color.BLACK);
        sourceCodeTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLineNumbers();
            }

            private void updateLineNumbers() {
                String[] lines = sourceCodeTextArea.getText().split("\\n");
                int totalLines = lines.length;
                StringBuilder sb = new StringBuilder();
                for (int i = 1; i <= totalLines; i++) {
                    sb.append(i).append("\n");
                }
                lineNumberTextArea.setText(sb.toString());
            }
        });

        JScrollPane sourceCodeScrollPane = new JScrollPane(sourceCodeTextArea);
        sourceCodeScrollPane.setRowHeaderView(lineNumberPanel);
        mainPanel.add(sourceCodeScrollPane, BorderLayout.CENTER);

        analyzeButton = new JButton("Analyze");
        analyzeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                analysisTextArea.setText("");
                String sourceCode = sourceCodeTextArea.getText();
                List<Token> tokens = LexicalAnalyzer.analyze(sourceCode);
                displayTokens(tokens);
                analysisTextArea.append("Analysis Result: \n");
                SyntacticAnalyzer syntaxAnalyzer = new SyntacticAnalyzer(tokens);
                int errors = displayAnalysisResult(syntaxAnalyzer.analyze());
                System.out.println(errors);
                if(errors == 0){
                    SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(tokens);
                    displayAnalysisResult(semanticAnalyzer.analyze());
                }

                frame.revalidate();
                frame.repaint();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(analyzeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        initializeTokensTable();
        tokensScrollPane = new JScrollPane(tokensTable);
        tokensScrollPane.setVisible(false);  // Ocultar la tabla inicialmente
        mainPanel.add(tokensScrollPane, BorderLayout.EAST);

        analysisTextArea = new JTextArea();
        analysisTextArea.setEditable(false);
        analysisTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        analysisTextArea.setForeground(Color.WHITE);
        analysisTextArea.setBackground(Color.BLACK);
        syntaxScrollPane = new JScrollPane(analysisTextArea);
        mainPanel.add(syntaxScrollPane, BorderLayout.WEST);
        syntaxScrollPane.setVisible(false);

        frame.getContentPane().add(mainPanel);
    }

    private void initializeTokensTable() {
        tokensTableModel = new DefaultTableModel(new Object[]{"Token", "Type", "Count"}, 0);
        tokensTable = new JTable(tokensTableModel);
        tokensTable.setBackground(Color.BLACK);
        tokensTable.setForeground(Color.WHITE);
        tokensTable.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tokensTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
    }

    private void displayTokens(List<Token> tokens) {
        tokensTableModel.setRowCount(0);
        Map<String, Integer> tokenCounts = new HashMap<>();

        for (Token token : tokens) {
            String tokenKey = token.getValue()+" type: "+token.getType();
            int count = tokenCounts.getOrDefault(tokenKey, 0);
            tokenCounts.put(tokenKey, count + 1);
        }

        for (Map.Entry<String, Integer> entry : tokenCounts.entrySet()) {
            String[] rowData = entry.getKey().split(" type: ");
            String tokenValue = rowData[0];
            String tokenType = rowData[1];
            int count = entry.getValue();
            tokensTableModel.addRow(new Object[]{tokenValue, tokenType, count});
        }

        tokensScrollPane.setVisible(true);  // Mostrar la tabla después de analizar
        syntaxScrollPane.setVisible(true);
    }

    private int displayAnalysisResult(List<String> result) {
        result.forEach(t -> analysisTextArea.append(t + "\n"));
        return result.size();
    }

    public void show() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame.setVisible(true);
                sourceCodeTextArea.requestFocusInWindow();
            }
        });
    }

    public static void main(String[] args) {
        CompilerGUI gui = new CompilerGUI();
        gui.show();
    }
}
