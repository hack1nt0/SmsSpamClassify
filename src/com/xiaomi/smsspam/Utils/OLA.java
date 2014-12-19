package com.xiaomi.smsspam.Utils;

import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.preprocess.RuleManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by dy on 14-11-30.
 */

public class OLA extends JPanel{
    private String[] xName;
    private JSlider totOR;
    private JSlider totGrade;
    private JButton toRight;
    private JButton fromRight;
    private JRadioButton[] sortRadioButton;
    private ButtonGroup buttonGroup;
    private JTextArea textArea;

    double[] classPreProbMap;
    double[][] binomialCPD;
    Map<String, Integer> tokenId;
    //Map<Integer, String> idToken;
    int[] ord;
    double[][] OR;
    double minLOR, maxLOR;
    int classCnt;
    int tokenCnt;
    int ruleCnt;

    private JPanel refreshedPanel;

    private LayoutManager lm;
    private GridBagConstraints c;

    public OLA(String filePath) {
        //read NB model
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String[] tmp = in.readLine().split("\\t");
            classCnt = Integer.valueOf(tmp[0]);
            tokenCnt = Integer.valueOf(tmp[1]);
            ruleCnt = Integer.valueOf(tmp[2]);
            classPreProbMap = new double[classCnt];
            binomialCPD = new double[classCnt][tokenCnt + 1 + ruleCnt];
            tokenId = new HashMap<>();
            //idToken = new HashMap<>();
            tmp = in.readLine().split("\\t");
            for (int i = 0; i < classCnt; ++i) classPreProbMap[i] = Double.valueOf(tmp[i + 1]);
            for (int i = 0; i < tokenCnt; ++i) {
                tmp = in.readLine().split("\\t");
                tokenId.put(tmp[0], i);
                //idToken.put(i, tmp[0]);
                for (int j = 0; j < classCnt; ++j)
                    binomialCPD[j][i] = Double.valueOf(tmp[j + 1]);
            }
            for (int i = 0; i < ruleCnt; ++i) {
                tmp = in.readLine().split("\\t");
                for (int j = 0; j < classCnt; ++j)
                    binomialCPD[j][i + tokenCnt + 1] = Double.valueOf(tmp[j + 1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        lm = new GridBagLayout();
        c = new GridBagConstraints();
        refreshedPanel = new JPanel(lm);

        buttonGroup = new ButtonGroup();
        String[] sortCommands = {"emerge", "absolute", "positive", "negative"};
        sortRadioButton = new JRadioButton[sortCommands.length];
        for (int i = 0; i < sortCommands.length; ++i) {
            sortRadioButton[i] = new JRadioButton(sortCommands[i]);
            sortRadioButton[i].setMnemonic(KeyEvent.VK_8);//?
            sortRadioButton[i].setActionCommand(sortCommands[i]);
            if (i == 0) sortRadioButton[i].setSelected(true);
            buttonGroup.add(sortRadioButton[i]);
        }
        totGrade = new JSlider(0, 100, 0);
        totGrade.setMajorTickSpacing(10);
        totGrade.setMinorTickSpacing(1);
        totGrade.setPaintTicks(true);
        totGrade.setPaintLabels(true);
        totOR = new JSlider(0, 100, 0);
        totOR.setMajorTickSpacing(10);
        totOR.setMinorTickSpacing(1);
        totOR.setPaintTicks(true);
        totOR.setPaintLabels(true);
        textArea = new JTextArea("您账户30元礼券最后2天！最强大促最后一波：全场1折起，夏装新品9元抢！购物100%享返现，最高返100%！退订复TD【梦芭莎】");
        textArea.setFont(new Font("Serif", Font.ITALIC, 16));
        textArea.setLineWrap(true);
        toRight = new JButton("toRight");
        toRight.addActionListener(e -> {
            RuleManager ruleManager = new RuleManager();
            Corpus cps = new Corpus(textArea.getText(), false, "unspecified");
            ruleManager.process(cps);
            Set<String> cpsTokens = new HashSet<String>(cps.getTokens());
            int xn = cps.getTokens().size() + 1 + cps.getX().length;
            ord = new int[xn];
            for (int i = 0; i < xn; ++i) ord[i] = i;
            OR = new double[xn][classCnt];
            xName = new String[xn];
            minLOR = maxLOR = 0;
            for (int i = 0; i < cps.getTokens().size(); ++i) {
                xName[i] = cps.getTokens().get(i);
                OR[i][1] = OR[i][0] = 0;
                if (tokenId.containsKey(cps.getTokens().get(i))) {
                    int acti = tokenId.get(cps.getTokens().get(i));
                    OR[i][1] = binomialCPD[Options.SPAM][acti] / binomialCPD[Options.NORMAL][acti];
                    OR[i][0] = (1 - binomialCPD[Options.SPAM][acti]) / (1 - binomialCPD[Options.NORMAL][acti]);
                }
            }
            xName[cps.getTokens().size()] = "ALL_UNSEEN_TOKENS";
            for (String token: tokenId.keySet()) {
                if (cpsTokens.contains(token)) continue;
                int id = tokenId.get(token);
                OR[cps.getTokens().size()][0] += binomialCPD[Options.SPAM][id] / binomialCPD[Options.NORMAL][id];
                OR[cps.getTokens().size()][1] += (1 - binomialCPD[Options.SPAM][id]) / (1 - binomialCPD[Options.NORMAL][id]);
            }
            List<String> ruleNames = RuleManager.getRuleNames();
            for (int i = 0; i < cps.getX().length; ++i) {
                xName[i + cps.getTokens().size() + 1] = ruleNames.get(i);
                int rb = 1, lb = 0;
                if (cps.getX()[i] == 0) {
                    rb = 0; lb = 1;
                }
                int acti = i + tokenCnt + 1;
                OR[i + cps.getTokens().size() + 1][rb] = binomialCPD[Options.SPAM][acti] / binomialCPD[Options.NORMAL][acti];
                OR[i + cps.getTokens().size() + 1][lb] = (1 - binomialCPD[Options.SPAM][acti]) / (1 - binomialCPD[Options.NORMAL][acti]);
            }

            for (int i = 0; i < OR.length; ++i)
                for (int j = 0; j < OR[0].length; ++j) {
                    minLOR = Math.min(Math.log(OR[i][j]), minLOR);
                    maxLOR = Math.max(Math.log(OR[i][j]), maxLOR);
                }

            paintRight(ord, xName, OR, refreshedPanel);

        });
        fromRight = new JButton("fromRight");

        //layout
        //GridBagConstraints c = new GridBagConstraints();
        //LayoutManager lm = new GridBagLayout();

        this.setLayout(lm);
        JPanel jPanel = new JPanel(lm);
        JScrollPane jScrollPane = new JScrollPane(refreshedPanel);
        jScrollPane.setPreferredSize(new Dimension(450, 1000));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = c.weighty = 1;
        JLabel jLabel = new JLabel("Single Point");
        jPanel.add(jLabel, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = c.weighty = 1;
        JSlider jSlider = new JSlider(-100, 100, 0);
        jSlider.setMajorTickSpacing(10);
        jSlider.setMinorTickSpacing(1);
        jSlider.setPaintTicks(true);
        jSlider.setPaintLabels(true);
        jPanel.add(jSlider, c);
        JPanel rightPanel = new JPanel(lm);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder("X-Points"),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                rightPanel.getBorder()));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        rightPanel.add(jPanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        rightPanel.add(jScrollPane, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 4;
        add(rightPanel, c);

        JPanel leftPanel = new JPanel(lm);

        jPanel = new JPanel(lm);
        jPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Sort"),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                        jPanel.getBorder()));
        for (int i = 0; i < sortRadioButton.length; ++i) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = i;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 1;
            jPanel.add(sortRadioButton[i], c);
        }
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        leftPanel.add(jPanel, c);

        jPanel = new JPanel(lm);
        jPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Points"),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                        jPanel.getBorder()));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        jPanel.add(totGrade, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        jPanel.add(totOR, c);
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        leftPanel.add(jPanel, c);


        jPanel = new JPanel(lm);
        jPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("SMS"),
                                BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                        jPanel.getBorder()));

        jScrollPane = new JScrollPane(textArea);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setPreferredSize(new Dimension(250, 250));
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        jPanel.add(jScrollPane, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        jPanel.add(toRight, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        jPanel.add(fromRight, c);


        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        leftPanel.add(jPanel, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 4;
        add(leftPanel, c);
    }

    private void paintRight(int[] ord, String[] xName, double[][] OR, JPanel refreshedPanel) {
        //GridBagConstraints c = new GridBagConstraints();
        int xn = ord.length;
        JSlider[] xSlider = new JSlider[xn];
        refreshedPanel.removeAll();
        for (int i = 0; i < ord.length; ++i) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = i + 1;
            c.weightx = 1;
            c.weighty = 1;
            JLabel tmpLabel = new JLabel(xName[ord[i]], JLabel.CENTER);
            refreshedPanel.add(tmpLabel, c);
            int lb = (int)Math.floor(OR[ord[i]][0] * 10);
            int rb = (int)Math.ceil(OR[ord[i]][1] * 10);
            xSlider[i] = new JSlider(-lb, rb, 0);
            //xSlider[i].setMajorTickSpacing(10);
            //xSlider[i].setMinorTickSpacing(1);
            //xSlider[i].setPaintTicks(true);
            //xSlider[i].setPaintLabels(true); //TODO
            xSlider[i].addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                int curGrade = 0;
                for (int j = 0; j < ord.length; ++j) curGrade += xSlider[j].getValue();
                totGrade.setValue(curGrade);
                System.out.println(source.getValue());
            });

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = i + 1;
            c.weightx = 1;
            c.weighty = 1;
            refreshedPanel.add(xSlider[i], c);
            System.out.println("added one slider" + i + " of " + ord.length);
        }
        refreshedPanel.validate();
        refreshedPanel.repaint();
        //repaint();
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("OLA");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        OLA ola = new OLA("data/NB.model");
        frame.add(ola);
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}

