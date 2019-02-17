package Bin;

import Component.tool.Tools;
import Component.unit.Configure;
import Component.unit.Opts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

public class Guide {
    private JButton Button_Configure;
    private JPanel Panel1;
    private JTextArea TextArea_Out;
    private JList list1;
    private JButton Button_Show;
    private JButton Button_Run;
    private JTextArea TextArea_Err;
    private JButton Button_LoadConfigure;
    private Config ConfigDialog;
    private Thread MainThread;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Guide");
        frame.setContentPane(new Guide().Panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - frame.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - frame.getHeight()) / 2);

        frame.setVisible(true);
    }

    public Guide() {
//        System.setOut(new PrintStream(new GuideOutStream(TextArea_Out)));
//        System.setErr(new PrintStream(new GuideOutStream(TextArea_Err)));
        Button_Configure.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ConfigDialog = new Config();
                ConfigDialog.pack();
                ConfigDialog.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - ConfigDialog.getWidth()) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - ConfigDialog.getHeight()) / 2);
                ConfigDialog.setVisible(true);
            }
        });
        Button_Show.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Configure.Update();
                Print(Configure.ShowParameter());
            }
        });
        Button_Run.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MainThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            PrintWriter out = new PrintWriter(new PrintStream(new GuideOutStream(TextArea_Out)));
                            PrintWriter err = new PrintWriter(new PrintStream(new GuideOutStream(TextArea_Err)));
                            File configfile = new File(Opts.JarFile.getParent() + "/" + Configure.Prefix + ".conf");
                            Configure.SaveParameter(configfile);
                            Tools.ExecuteCommandStr("java -jar " + Opts.JarFile + " -conf " + configfile, out, err);

                        } catch (InterruptedException | IOException exp) {
                            exp.printStackTrace();
                        }
                    }
                });
                MainThread.start();
            }
        });
        Button_LoadConfigure.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser jc = new JFileChooser();
                jc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jc.showDialog(null, null);
                if (jc.getSelectedFile() != null) {
                    try {
                        Configure.GetOption(jc.getSelectedFile(), null);
                    } catch (IOException ignored) {

                    }
                }
            }
        });
    }

    public void Update() {

    }

    private void Print(String s) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TextArea_Out.append(s);
            }
        });
    }
}

class GuideOutStream extends OutputStream {
    private JTextArea J;

    public GuideOutStream(JTextArea j) {
        J = j;
    }

    @Override
    public void write(int b) throws IOException {

    }

    @Override
    public void write( byte[] b) throws IOException {
        J.append(new String(b));
    }

    @Override
    public void write( byte[] b, int off, int len) throws IOException {
        J.append(new String(b, off, len));
        J.setCaretPosition(J.getText().length());
    }
}
