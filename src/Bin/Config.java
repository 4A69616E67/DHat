package Bin;

import Component.File.FastQFile.FastqFile;
import Component.Software.Bwa;
import Component.Software.Python;
import Component.tool.Tools;
import Component.unit.Chromosome;
import Component.unit.Configure;
import Component.FragmentDigested.RestrictionEnzyme;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;

/**
 * Created by snowf on 2019/2/17.
 */
class Config extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField TextField_InputFile;
    private JButton Button_InputFile;
    private JTextField TextField_GenomeFile;
    private JButton Button_GenomeFile;
    private JTextField TextField_HalfLinker2;
    private JTextField TextField_HalfLinker1;
    private JTabbedPane tabbedPane1;
    private JTextField TextField_Restriction;
    private JTextField TextField_OutPath;
    private JButton Button_OutPath;
    private JTextField TextField_Prefix;
    private JTextField TextField_Index;
    private JButton Button_Index;
    private JTextField TextField_Chromosome;
    private JTextField TextField_AdapterSeq;
    private JTextField TextField_Resolution;
    private JTextField TextField_DrawRes;
    //    private JTextField TextField_DetectRes;
    private JTextField TextField_Thread;
    private JTextField TextField_MatchScore;
    private JTextField TextField_MisMatchScore;
    private JTextField TextField_InDelScore;
    private JTextField TextField_MaxReadsLen;
    private JTextField TextField_MinReadsLen;
    private JTextField TextField_MinLinkerLen;
    private JTextField TextField_AlignType;
    private JTextField TextField_AlignThread;
    private JTextField TextField_AlignMisMatchNum;
    private JTextField TextField_MinMappingScore;
    private JTextField TextField_DebugLevel;
    private JLabel Label_Bottom;
    private JCheckBox CheckBox_IterationAlign;
    private JTextField textField_bwa;
    private JLabel bwa;
    private JTextField textField_python;
    private JLabel python;

    public Config() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setTitle("Configure");
        setResizable(false);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Button_InputFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextField_InputFile.setText(getFile().toString());
            }
        });
        Button_GenomeFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextField_GenomeFile.setText(getFile().toString());
            }
        });
        Button_Index.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextField_Index.setText(getFile().toString());
            }
        });
        Button_OutPath.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TextField_OutPath.setText(getDir().toString());
            }
        });
        Init();
    }

    private void Init() {
        TextField_InputFile.setText(Configure.InputFile == null ? "" : Configure.InputFile.toString());
        TextField_GenomeFile.setText(Configure.GenomeFile == null ? "" : Configure.GenomeFile.toString());
        TextField_Restriction.setText(Configure.Restriction == null ? "" : Configure.Restriction.toString());
        String[] strs;
        if (Configure.HalfLinker == null) {
            TextField_HalfLinker1.setText("");
            TextField_HalfLinker2.setText("");
        } else {
            strs = Configure.HalfLinker;
            if (strs.length < 1) {
                TextField_HalfLinker1.setText("");
                TextField_HalfLinker2.setText("");
            } else if (strs.length < 2) {
                TextField_HalfLinker1.setText(Configure.HalfLinker[0]);
                TextField_HalfLinker2.setText("");
            } else {
                TextField_HalfLinker1.setText(Configure.HalfLinker[0]);
                TextField_HalfLinker2.setText(Configure.HalfLinker[1]);
            }
        }
        //=========================================================================
        TextField_OutPath.setText(Configure.OutPath.toString());
        TextField_Prefix.setText(Configure.Prefix);
        TextField_Index.setText(Configure.Index == null ? "" : Configure.Index.toString());
        TextField_Chromosome.setText(Configure.Chromosome == null ? "" : Tools.ArraysToString(Configure.Chromosome));
        TextField_AdapterSeq.setText(Configure.AdapterSeq == null ? "" : Tools.ArraysToString(Configure.AdapterSeq));
        TextField_Thread.setText(String.valueOf(Configure.Thread));
        TextField_Resolution.setText(Tools.ArraysToString(Configure.Resolution));
        TextField_DrawRes.setText(Tools.ArraysToString(Configure.DrawResolution));
//        TextField_DetectRes.setText(String.valueOf(Configure.DetectResolution));
        TextField_MatchScore.setText(String.valueOf(Configure.MatchScore));
        TextField_MisMatchScore.setText(String.valueOf(Configure.MisMatchScore));
        TextField_InDelScore.setText(String.valueOf(Configure.InDelScore));
        TextField_MinLinkerLen.setText(Configure.MinLinkerLen != 0 ? String.valueOf(Configure.MinLinkerLen) : "");
        TextField_MinReadsLen.setText(String.valueOf(Configure.MinReadsLen));
        TextField_MaxReadsLen.setText(String.valueOf(Configure.MaxReadsLen));
        TextField_AlignType.setText(Configure.AlignType);
        TextField_AlignMisMatchNum.setText(String.valueOf(Configure.AlignMisMatch));
        TextField_MinMappingScore.setText(Configure.MinUniqueScore != 0 ? String.valueOf(Configure.MinUniqueScore) : "");
        TextField_AlignThread.setText(String.valueOf(Configure.AlignThread));
        CheckBox_IterationAlign.setSelected(Configure.Iteration);
        TextField_DebugLevel.setText(String.valueOf(Configure.DeBugLevel));
        textField_bwa.setText(Configure.Bwa == null ? "" : Configure.Bwa.Exe());
        textField_python.setText(Configure.Python == null ? "" : Configure.Python.Exe());
    }

    private File getFile() {
        JFileChooser jc = new JFileChooser();
        jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jc.showDialog(null, null);
        return jc.getSelectedFile() == null ? new File("") : jc.getSelectedFile();
    }

    private File getDir() {
        JFileChooser jc = new JFileChooser();
        jc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jc.showDialog(null, null);
        return jc.getSelectedFile() == null ? new File("") : jc.getSelectedFile();
    }

    private void SaveOption() {
        if (tabbedPane1.getSelectedIndex() == 0) {
            Configure.InputFile = new FastqFile(TextField_InputFile.getText().trim());
            Configure.GenomeFile = new File(TextField_GenomeFile.getText().trim());
            Configure.Restriction = new RestrictionEnzyme(TextField_Restriction.getText().trim());
            Configure.HalfLinker = String.join(" ", new String[]{TextField_HalfLinker1.getText().trim(), TextField_HalfLinker2.getText().trim()}).split("\\s+");
            Configure.OutPath = new File(TextField_OutPath.getText().trim());
            Configure.Prefix = TextField_Prefix.getText().trim();
            Configure.Index = new File(TextField_Index.getText().trim());
            String[] s = TextField_Chromosome.getText().trim().split("\\s+");
            if (s.length > 0 && !s[0].equals("")) {
                Configure.Chromosome = new Chromosome[s.length];
                for (int i = 0; i < Configure.Chromosome.length; i++) {
                    Configure.Chromosome[i] = new Chromosome(s[i]);
                }
            }
            Configure.AdapterSeq = TextField_AdapterSeq.getText().trim().split("\\s+");
            Configure.Thread = Configure.GetIntItem(TextField_Thread.getText().trim(), Configure.Thread);
            Configure.Resolution = Configure.GetIntArray(TextField_Resolution.getText().trim(), Configure.Resolution);
            Configure.DrawResolution = Configure.GetIntArray(TextField_DrawRes.getText().trim(), Configure.DrawResolution);
//            Configure.DetectResolution = Configure.GetIntItem(TextField_DetectRes.getText().trim(), Configure.DetectResolution);
        } else if (tabbedPane1.getSelectedIndex() == 1) {
            Configure.MatchScore = Configure.GetIntItem(TextField_MatchScore.getText().trim(), Configure.MatchScore);
            Configure.MisMatchScore = Configure.GetIntItem(TextField_MisMatchScore.getText().trim(), Configure.MisMatchScore);
            Configure.InDelScore = Configure.GetIntItem(TextField_InDelScore.getText().trim(), Configure.InDelScore);
            Configure.MinLinkerLen = Configure.GetIntItem(TextField_MinLinkerLen.getText().trim(), Configure.MinLinkerLen);
            Configure.MinReadsLen = Configure.GetIntItem(TextField_MinReadsLen.getText().trim(), Configure.MinReadsLen);
            Configure.MaxReadsLen = Configure.GetIntItem(TextField_MaxReadsLen.getText().trim(), Configure.MaxReadsLen);
            Configure.AlignType = TextField_AlignType.getText().trim();
            Configure.AlignMisMatch = Configure.GetIntItem(TextField_AlignMisMatchNum.getText().trim(), Configure.AlignMisMatch);
            Configure.MinUniqueScore = Configure.GetIntItem(TextField_MinMappingScore.getText().trim(), Configure.MinUniqueScore);
            Configure.AlignThread = Configure.GetIntItem(TextField_AlignThread.getText().trim(), Configure.AlignThread);
            Configure.Iteration = CheckBox_IterationAlign.isSelected();
            Configure.DeBugLevel = Configure.GetIntItem(TextField_DebugLevel.getText().trim(), Configure.DeBugLevel);
            Configure.Bwa = new Bwa(textField_bwa.getText().trim());
            Configure.Python = new Python(textField_python.getText().trim());
        }
        Configure.Update();
    }

    private void onOK() {
        // add your code here
        SaveOption();
        Label_Bottom.setText("Save Success!");
//        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    public static void main(String[] args) {
        Config dialog = new Config();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

}
