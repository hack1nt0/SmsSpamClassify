package com.xiaomi.smsspam.Utils;

import com.sun.org.glassfish.external.statistics.Statistic;
import com.xiaomi.smsspam.Options;
import com.xiaomi.smsspam.preprocess.RuleManager;
import org.openjdk.jol.samples.JOLSample_20_Roots;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;

/**
 * Created by dy on 14-12-3.
 */
public class GUI1 {
    private JPanel panel1;
    private JRadioButton mergeOrderRadioButton;
    private JRadioButton absoluteRadioButton;
    private JRadioButton negativeRadioButton;
    private JRadioButton positiveRadioButton;
    private JSlider ProbSlider;
    private JSlider LORSumSlider;
    private JTextArea smsTextArea;
    private JButton anaysisButton;
    private JSlider slider4;
    private JPanel refreshPane;
    private JPanel statisticPane;

    String[] xName;
    double[] classPreProbMap;
    double[][] binomialCPD;
    Map<String, Integer> tokenId;
    Integer[] ord;
    double[][] LOR;
    double minLOR, maxLOR;
    int classCnt;
    int tokenCnt;
    int ruleCnt;

    public GUI1() {
        readModel("data/NB.model");
        anaysisButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //get LOR
                RuleManager ruleManager = new RuleManager();
                Corpus cps = new Corpus(smsTextArea.getText(), false, "unspecified");
                ruleManager.process(cps);
                Set<String> cpsTokens = new HashSet<String>(cps.getTokens());
                int xn = cps.getTokens().size() + 1 + cps.getX().length;
                int xindex = 0;

                ord = new Integer[xn];
                for (int i = 0; i < xn; ++i) ord[i] = i;
                LOR = new double[xn][classCnt];
                xName = new String[xn];
                minLOR = maxLOR = 0;
                for (int i = 0; i < cps.getTokens().size(); ++i) {
                    xName[xindex] = cps.getTokens().get(i);
                    LOR[xindex][1] = LOR[i][0] = 0;
                    if (tokenId.containsKey(cps.getTokens().get(i))) {
                        int acti = tokenId.get(cps.getTokens().get(i));
                        LOR[xindex][1] = Math.log(binomialCPD[Options.SPAM][acti] / binomialCPD[Options.NORMAL][acti]);
                        LOR[xindex][0] = Math.log((1 - binomialCPD[Options.SPAM][acti]) / (1 - binomialCPD[Options.NORMAL][acti]));
                    }
                    ++xindex;
                }
                xName[xindex] = "OtherTokens(NO)";
                for (String token: tokenId.keySet()) {
                    if (cpsTokens.contains(token)) continue;
                    int id = tokenId.get(token);
                    LOR[xindex][0] += Math.log(binomialCPD[Options.SPAM][id] / binomialCPD[Options.NORMAL][id]);
                    LOR[xindex][1] += Math.log((1 - binomialCPD[Options.SPAM][id]) / (1 - binomialCPD[Options.NORMAL][id]));
                }
                ++xindex;

                List<String> ruleNames = RuleManager.getRuleNames();
                for (int i = 0; i < cps.getX().length; ++i) {
                    int rb = 1, lb = 0;
                    xName[xindex] = ruleNames.get(i);
                    if (cps.getX()[i] == 0) {
                        xName[xindex] += "(NO)";
                        rb = 0; lb = 1;
                    }
                    int acti = i + tokenCnt + 1;
                    LOR[xindex][rb] = Math.log(binomialCPD[Options.SPAM][acti] / binomialCPD[Options.NORMAL][acti]);
                    LOR[xindex][lb] = Math.log((1 - binomialCPD[Options.SPAM][acti]) / (1 - binomialCPD[Options.NORMAL][acti]));
                    ++xindex;
                }
                paintRight(ord, xName, LOR, refreshPane);

                //paint Statistic Pane
                double lb = 0, rb = 0;
                double lorSum = 0;
                for (int i = 0; i < LOR.length; ++i) {
                    lorSum += LOR[i][1];
                    for (int j = 0; j < LOR[0].length; ++j) {
                        if (i == cps.getTokens().size() && j == 0) continue;
                        if (LOR[i][j] < 0) lb += LOR[i][j];
                        else rb += LOR[i][j];
                    }
                }
                int curLabelCnt = 10;
                int candLabelIndex = 0;
                Hashtable<Integer, JLabel> labelHashtable = new Hashtable<>();
                for (int i = 0; i <= curLabelCnt; ++i) {
                    double curLabel = lb + (rb - lb) / curLabelCnt * i;
                    String labelName = String.valueOf(curLabel);
                    int sigCnt = labelName.charAt(0) == '-' ? 5 : 4;
                    labelHashtable.put(i, new JLabel(labelName.substring(0, Math.min(sigCnt, labelName.length()))));
                    double candLabel = lb + (rb - lb) / curLabelCnt * candLabelIndex;
                    if (Math.abs(curLabel - lorSum) < Math.abs(candLabel - lorSum)) candLabelIndex = i;
                }
                LORSumSlider.removeAll();
                LORSumSlider.setLabelTable(labelHashtable);
                LORSumSlider.setValue(candLabelIndex);
                LORSumSlider.setMaximum(curLabelCnt);
                LORSumSlider.setMinimum(0);
                LORSumSlider.validate();
                LORSumSlider.repaint();

                double Prob = 1 / (1 + Math.exp(-Math.log(classPreProbMap[1] / classPreProbMap[0]) - lorSum));
                ProbSlider.setValue((int) Math.round(Prob * 100));
                System.out.println(Prob);
            }
        });
        mergeOrderRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ord.length; ++i) ord[i] = i;
                paintRight(ord, xName, LOR, refreshPane);
            }
        });

        absoluteRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ord.length; ++i) ord[i] = i;
                Arrays.sort(ord, (Integer a, Integer b) -> {
                    int al = (int)Math.abs(LOR[a][0] - LOR[a][1]);
                    int bl = (int)Math.abs(LOR[b][0] - LOR[b][1]);
                    return al - bl;
                });
                paintRight(ord, xName, LOR, refreshPane);
            }
        });
        positiveRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ord.length; ++i) ord[i] = i;
                Arrays.sort(ord, (Integer a, Integer b) -> {
                    int al = (int)Math.abs(Math.max(LOR[a][0], LOR[a][1]));
                    int bl = (int)Math.abs(Math.max(LOR[b][0], LOR[b][1]));
                    return al - bl;
                });
                paintRight(ord, xName, LOR, refreshPane);
            }
        });
        negativeRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0; i < ord.length; ++i) ord[i] = i;
                Arrays.sort(ord, (Integer a, Integer b) -> {
                    int al = (int)Math.abs(Math.min(LOR[a][0], LOR[a][1]));
                    int bl = (int)Math.abs(Math.min(LOR[b][0], LOR[b][1]));
                    return al - bl;
                });
                paintRight(ord, xName, LOR, refreshPane);

            }
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("NBCOLA");
        frame.setContentPane(new GUI1().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void paintRight(Integer[] ord, String[] xName, double[][] LOR, JPanel refreshedPanel) {
        GridBagConstraints c = new GridBagConstraints();
        int xn = ord.length;
        JSlider[] xSlider = new JSlider[xn];
        refreshedPanel.removeAll();
        for (int i = 0; i < ord.length; ++i) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;
            c.gridy = i;
            c.weightx = 0.3;
            JLabel tmpLabel = new JLabel(xName[ord[i]], JLabel.CENTER);
            refreshedPanel.add(tmpLabel, c);
            double lb = LOR[ord[i]][0];
            double rb = LOR[ord[i]][1];

            //System.out.println(lb + ", " + rb);

            int labelCnt = 10;
            int value = labelCnt;
            if (lb > rb) {
                value = 0;
                double t = lb;
                lb = rb;
                rb = t;
            }
            xSlider[i] = new JSlider(0, labelCnt + 1, value);
            Hashtable<Integer, JLabel> hashtable = new Hashtable<>();
            for (int k = 0; k <= labelCnt; ++k) {
                String labelName = String.valueOf(lb + (rb - lb) / labelCnt * k);
                int sigCnt = labelName.charAt(0) == '-' ? 5 : 4;
                hashtable.put(k, new JLabel(labelName.substring(0, Math.min(sigCnt, labelName.length()))));
            }
            xSlider[i].setLabelTable(hashtable);
            xSlider[i].setMajorTickSpacing(1);
            xSlider[i].setMinorTickSpacing(0);
            xSlider[i].setPaintTicks(true);
            xSlider[i].setPaintLabels(true);
            xSlider[i].addChangeListener(e -> {
                JSlider source = (JSlider) e.getSource();
                double newGrade = 0;
                for (int j = 0; j < ord.length; ++j) {
                    JLabel curLabel = (JLabel) xSlider[j].getLabelTable().get(xSlider[j].getValue());
                    newGrade += Double.valueOf(curLabel.getText());
                }
                int LORSumSliderIndex = 0;
                for (Object k: Collections.list(LORSumSlider.getLabelTable().keys())) {
                    double curGap = Math.abs(Double.valueOf(((JLabel)LORSumSlider.getLabelTable().get(k)).getText())- newGrade);
                    double canGap = Math.abs(Double.valueOf(((JLabel)LORSumSlider.getLabelTable().get(LORSumSliderIndex)).getText())- newGrade);
                    if (curGap < canGap) LORSumSliderIndex = (Integer)k;
                }
                LORSumSlider.setValue(LORSumSliderIndex);

                double Prob = 1 / (1 + Math.exp(-Math.log(classPreProbMap[1] / classPreProbMap[0]) - newGrade));

                ProbSlider.setValue((int)Math.round(Prob * 100)); //todo
                System.out.println(Prob);
                //System.out.println(source.getValue());
            });

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = i;
            c.weightx = 1.0;
            refreshedPanel.add(xSlider[i], c);
            //System.out.println("added one slider" + i + " of " + ord.length);
        }
        refreshedPanel.validate();
        refreshedPanel.repaint();
        //repaint();
    }

    private void readModel(String filePath) {
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
    }
}
