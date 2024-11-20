package com.gcalendarinterpreter.view;

import javax.swing.*;
import java.awt.*;

public class LexicalAnalyzerView extends JFrame {
    public JTextPane textPane;
    public JButton compileButton;
    public JButton uploadButton;
    public JLabel resultLabel;
    public JTextArea tokensArea;

    public LexicalAnalyzerView() {
        initUI();
    }

    private void initUI() {
        setTitle("Lexical Analyzer");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textPane = new JTextPane();
        compileButton = new JButton("Compilar");
        uploadButton = new JButton("Subir archivo");
        resultLabel = new JLabel("");
        tokensArea = new JTextArea(10, 50);
        tokensArea.setEditable(false);


        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(new JScrollPane(tokensArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(uploadButton);
        buttonPanel.add(compileButton);

        setLayout(new BorderLayout());
        add(new JScrollPane(textPane), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.WEST);
        add(resultLabel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}