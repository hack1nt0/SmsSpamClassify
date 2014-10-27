package com.xiaomi.smsspam;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
  
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import com.xiaomi.smsspam.Utils.Corpus;
import org.json.JSONException;
import org.json.JSONObject;

class CheckBoxEditor extends DefaultCellEditor implements ItemListener {
	
	private JCheckBox button;
    
    public CheckBoxEditor(JCheckBox checkBox) {
        super(checkBox);
    }
  
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        if (value == null)
            return null;
        button = (JCheckBox) value;
        button.addItemListener(this);
        return (Component) value;
    }
  
    public Object getCellEditorValue() {
        button.removeItemListener(this);
        return button;
    }
  
    public void itemStateChanged(ItemEvent e) {
        super.fireEditingStopped();
    }
}

class CheckBoxRenderer implements TableCellRenderer {
    
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null)
            return null;
        return (Component) value;
    }
}


public class Tables implements MouseListener {
    JTable table = new JTable();
    private JButton btnNext;
    private JButton btnDone;
    private JTextArea txtPhrase;
    private JTextArea txtSeq;
    private JFrame mFrame;
    
    

    private JTextArea txtModifiedPhrase;
    private JButton btnModify;
    
    List<Corpus> mOrignCorpus;
    List<PairCount> mPhrases;
    int mCurrentIndex = -1;

    Font font=new Font("宋体",Font.BOLD, 24);
    public Tables(List<Corpus> orignCorpus, Map<String, PairCount>[] filteredPhrases) {
        mOrignCorpus= orignCorpus;
        mPhrases = new ArrayList<PairCount>();
        for(int i = filteredPhrases.length - 1; i >= 0; --i){
            Map<String, PairCount> map = filteredPhrases[i];
            Iterator<Map.Entry<String, PairCount>> it = map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<String, PairCount> entry = it.next();
                PairCount pc = entry.getValue();
                mPhrases.add(pc);
            }
        }

        mFrame = new JFrame("sjh");
        mFrame.setLayout(null);
  
        txtPhrase = new JTextArea();
        txtPhrase.setSize(1500, 40);
        txtPhrase.setLocation(0, 0);
        txtPhrase.setEnabled(false);
        txtPhrase.setText("sdlfkjsdlfo sdfsd");
        txtPhrase.setBackground(Color.BLACK);
        txtPhrase.setFont(font);
        mFrame.add(txtPhrase);
        
        txtSeq = new JTextArea();
        txtSeq.setSize(140, 40);
        txtSeq.setLocation(txtPhrase.getWidth() + 10, 0);
        txtSeq.setEnabled(false);
        txtSeq.setBackground(Color.BLACK);
        txtSeq.setFont(font);
        mFrame.add(txtSeq);
        
        table = this.gettable();
        table.addMouseListener(this);
        table.setRowHeight(24);
        table.setFont(new Font("Menu.font", Font.PLAIN, 18));
        JScrollPane src = new JScrollPane(table);
        src.setBounds(0, txtPhrase.getHeight() + 10, 1800, 800);
        mFrame.setSize(new Dimension(1800, 1000));
        mFrame.add(src);
        
        btnNext = new JButton("Next");
        btnNext.setSize(80,30);
        btnNext.setLocation(txtPhrase.getWidth() + 156, 5);
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                next();
            }
        });

        btnDone = new JButton("Done");
        btnDone.setSize(80,40);
        btnDone.setLocation((src.getWidth()) - 140, src.getHeight() + 100);
        btnDone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                try {
                    doneAndSave();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });


        txtModifiedPhrase = new JTextArea();
        txtModifiedPhrase.setSize(160, 40);
        txtModifiedPhrase.setLocation(100,  src.getHeight() + 100);
        txtModifiedPhrase.setFont(font);
        mFrame.add(txtModifiedPhrase);

        btnModify = new JButton("Modify");
        btnModify.setSize(80,40);
        btnModify.setLocation(100 + txtModifiedPhrase.getWidth() + 30, src.getHeight() + 100);
        btnModify.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                doModify();
            }
        });
        
        
        mFrame.add(btnNext);
        mFrame.add(btnDone);
        mFrame.add(btnModify);

        next();
        mFrame.setVisible(true);
    }

    private void doModify(){
        PairCount pc = mPhrases.get(mCurrentIndex);
        String suggestion = txtModifiedPhrase.getText();
        // Assume: the phrases behind are longer, so they may include current phrase
        for(int i = 0; i < mCurrentIndex; ++i){
            if(mPhrases.get(i).phs.equals(suggestion)){
                System.out.println("Already appear behind!");
                return;
            }
        }

        int spamCount = 0;
        for(int i = 0; i < mOrignCorpus.size(); ++i){
            Corpus cps = mOrignCorpus.get(i);
            String body = cps.getRefinedBody();
            if(body.indexOf(suggestion) != -1 && !cps.getMarked()){
                mCurrentSmses.add(cps);
                if(cps.getIsSpam()){
                    spamCount++;
                }
            }
        }
        
        pc.phs = suggestion;

        txtPhrase.setText(pc.phs + "  " + spamCount + ":" + (mCurrentSmses.size() - spamCount));
        txtSeq.setText((mCurrentIndex + 1) + "/" + mPhrases.size());

        updateTable();
    }

    private void doneAndSave() throws JSONException, IOException{
        int totalChanged = 0;
        final String OUT_FILE = Options.FilePath + "_C";
        File f = new File(OUT_FILE);
        if(f.exists()){
            f.delete();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(OUT_FILE));

        for(int i = 0; i < mOrignCorpus.size(); ++i){
            Corpus cps = mOrignCorpus.get(i);
            if(cps.getCatChanged()){
                totalChanged++;
            }
            
            JSONObject object = new JSONObject();
            object.put(Corpus.BODY, cps.getOriginBody());
            object.put(Corpus.SPAM, cps.getIsSpam());
            object.put(Corpus.ADDRESS, cps.getAddress());
            bw.write(object.toString());
            bw.newLine();
        }
        bw.close();
        System.out.println("Total " + totalChanged + " smses are marked differently");
    }
    
    private List<Corpus> mCurrentSmses = new ArrayList<Corpus>();
    Object[][] mData;

    private void next(){
        if(mCurrentIndex < mPhrases.size() - 1){
            if(mCurrentIndex >= 0){
                for(int i = 0; i < mCurrentSmses.size(); ++i){
                    JCheckBox cb = (JCheckBox)(mData[i][1]);
                    Corpus cps = mCurrentSmses.get(i);
                    cps.setMarked(true);
                    boolean spam = cps.getIsSpam();
                    boolean markSpam = cb.isSelected();
                    if(spam != markSpam){
                        cps.setIsSpam(markSpam);
                        cps.setCatChanged(true);
                        System.out.println("mark one to:" + Boolean.toString(markSpam));
                    }
                }
            }
            
        }else{
            return;
        }

        boolean isCurrSpam = false;
        mCurrentSmses.clear();
        while(mCurrentSmses.size() == 0 && mCurrentIndex < mPhrases.size() - 1){
            mCurrentIndex++;
            PairCount pc = mPhrases.get(mCurrentIndex);
            if(pc.vals[0] == 0 || pc.vals[1] == 0){
                continue;
            }
            int min = pc.vals[0] < pc.vals[1] ? pc.vals[0] : pc.vals[1];
            int total = pc.vals[0] + pc.vals[1];
            if(min > 10 || (1.0 * min / total) > 0.1){
                continue;
            }
            isCurrSpam = pc.vals[0] > pc.vals[1];
            for(int i = 0; i < mOrignCorpus.size(); ++i){
                Corpus cps = mOrignCorpus.get(i);
                String body = cps.getRefinedBody();
                if(body.indexOf(pc.phs) != -1 && !cps.getMarked()){
                    mCurrentSmses.add(cps);
                }
            }
        }

        PairCount pc = mPhrases.get(mCurrentIndex);
        txtPhrase.setText(pc.phs + "  " + pc.vals[0] + ":" + pc.vals[1]);

        txtSeq.setText((mCurrentIndex + 1) + "/" + mPhrases.size());

        final boolean isSpam = isCurrSpam;
        Collections.sort(mCurrentSmses, new Comparator<Corpus>(){
                public int compare(Corpus arg0, Corpus arg1) {
                    if(arg0.getIsSpam() == arg1.getIsSpam()){
                        return 0;
                    }else{
                        if(isSpam == arg0.getIsSpam()){
                            return 1;
                        }else{
                            return -1;
                        }
                    }
                }
        });

        updateTable();
    }

    private void updateTable(){
        PairCount pc = mPhrases.get(mCurrentIndex);
        mData = new Object[mCurrentSmses.size()][2];
        for(int i = 0; i < mCurrentSmses.size(); ++i){
            mData[i] = new Object[2];

            mData[i][0] = mCurrentSmses.get(i).getOriginBody();
            JCheckBox cb = new JCheckBox();
            cb.setSelected(mCurrentSmses.get(i).getIsSpam());
            mData[i][1] = cb;
        }
        mDm.setDataVector(mData, COLOMN);
        table.getColumn(SPAM).setCellEditor( new CheckBoxEditor(new JCheckBox()));
        table.getColumn(SPAM).setCellRenderer(new CheckBoxRenderer());

        final String keyWord = pc.phs;
        table.getColumn(CONTENT).setCellRenderer(new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable arg0, Object arg1,
                    boolean arg2, boolean arg3, int arg4, int arg5) {
                JTextArea jt = new JTextArea();
                String text = arg1.toString();
                jt.setText(text);
                jt.setFont(new Font("Menu.font", Font.PLAIN, 18));

                Highlighter highLighter = jt.getHighlighter();
                DefaultHighlighter.DefaultHighlightPainter p = new DefaultHighlighter.DefaultHighlightPainter(Color.PINK);
                int pos = 0;
                while ((pos = text.indexOf(keyWord, pos)) >= 0)
                {
                    try
                    {
                        highLighter.addHighlight(pos, pos + keyWord.length(), p);
                        pos += keyWord.length();
                    }
                    catch (BadLocationException e)
                    {
                        e.printStackTrace();
                    }
                }
                return jt;
            }
        });
        table.getColumn(SPAM).setPreferredWidth(160);
        table.getColumn(SPAM).setMaxWidth(160);
        table.getColumn(SPAM).setMinWidth(160);
        mDm.fireTableDataChanged();
    }
    
    private static final String CONTENT = "内容";
    private static final String SPAM = "类别";
    private static Object[] COLOMN = {CONTENT, SPAM};

    DefaultTableModel mDm = new DefaultTableModel();
    public JTable gettable() {
        mDm = new DefaultTableModel();
          JTable table = new JTable(mDm) {
            public void tableChanged(TableModelEvent e) {
                super.tableChanged(e);
                repaint();
            }
        };
        return table;
    }

    public void mouseClicked(MouseEvent arg0) {
        System.out.println("mouseClicked:");
    }
  
    public void mouseEntered(MouseEvent arg0) {
    }
  
    public void mouseExited(MouseEvent arg0) {
    }
  
    public void mousePressed(MouseEvent arg0) {
    }
  
    public void mouseReleased(MouseEvent arg0) {
    }
}
