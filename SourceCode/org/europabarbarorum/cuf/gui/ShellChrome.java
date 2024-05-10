/*
 *
 *  Copyright (C) 2010, 2011, 2012 The Europa Barbarorum Team
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of The Europa Barbarorum Team nor the
 *        names of other contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL The Europa Barbarorum Team BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.europabarbarorum.cuf.gui;

import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import org.europabarbarorum.cuf.gui.support.ScrollingSupport;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import org.europabarbarorum.cuf.gui.support.TextActions;
import org.europabarbarorum.cuf.shell.CompileUnit;
import org.europabarbarorum.cuf.shell.IOService.StdStream;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.shell.EscapeSequence.Background;
import org.europabarbarorum.cuf.shell.EscapeSequence.EscapeType;
import org.europabarbarorum.cuf.shell.EscapeSequence.Foreground;
import org.europabarbarorum.cuf.shell.EscapeSequence.Style;
import org.europabarbarorum.cuf.shell.IOService.InputService;
import org.europabarbarorum.cuf.shell.IOService.OutputService;
import org.europabarbarorum.cuf.shell.PredefinedScriptNames;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.ShellX;
import org.europabarbarorum.cuf.support.Classloader;
import org.europabarbarorum.cuf.support.CyclicList;
import org.europabarbarorum.cuf.support.ResourceHelp.PositiveInteger;
import org.europabarbarorum.cuf.support.Setting;

/**
 * Provides a GUI around a {@link Shell}. This class also provides a basic command history.
 * @author Johan Ouwerkerk
 */
public class ShellChrome extends JPanel {

    /** 
     * Creates a new chrome (panel) to interact with a {@link ShellChrome.XShell} instance.
     */
    public ShellChrome () {
        initComponents();
    }
    /**
     * {@link Integer} preference which denotes the number of lines of shell commands that must be kept as in memory.
     */
    public static final Setting<Integer> InputHistoryWindow = new Setting<Integer>(
            "cuf.shell.input.history", "50",
            new PositiveInteger(),
            Modifiable.Conf);
    /**
     * {@link Integer} preference which denotes the number of lines of shell output that must be kept in memory.
     */
    public static final Setting<Integer> OutputHistoryWindow = new Setting<Integer>(
            "cuf.shell.output.history", "240",
            new PositiveInteger(),
            Modifiable.Conf);

    /**
     * Create and start a shell so the GUI will be populated; also bind a window to the 
     * create shell so its title can be updated from time to time.
     * @param frame the window to be bound to a shell.
     */
    public void init (JFrame frame) {
        if (window == null) {
            /*
             * Note the reason for the existence of this method
             * is that the code in this method cannot be incorporated
             * as part of the default constructor because at design time
             * (as opposed to run time) it would throw exceptions
             * which means that this class would be unusable with Mattisse.
             *
             * More precisely it would throw a class cast exception because the underlying code makes
             * assumptions that don't work in the Netbeans IDE/platform.
             * The reason being that Netbeans uses a custom classloading mechanism which
             * is completely different from what an ordinary JVM environment uses.
             *
             * Since a setter method is needed anyway to ensure that the GUI window can be updated
             * from time to time; this method becomes public and functions as dump site for
             * initialisation code. As added bonus the footprint of the component should be lower at
             * design time since all fields can be initialised here as well.
             */

            try {
                commands = new LinkedBlockingQueue<String>();
                outputHistory = new OutputHistory(OutputHistoryWindow.get());
                inputHistory = new CyclicList<String>(InputHistoryWindow.get());
                outputService = new OutputService() {

                    @Override
                    protected Thread createThread (Runnable task) {
                        return outputFactory.newThread(task);
                    }
                };
                inputService = new InputService() {

                    @Override
                    protected Thread createThread (Runnable task) {
                        return inputFactory.newThread(task);
                    }
                };
                window = frame;
                window.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing (WindowEvent e) {
                        try {
                            Shell s = Shell.getCurrentShell();
                            int num = s.number(), choice;
                            if (num == 1) {
                                s.exit();
                            }
                            else {
                                choice =
                                        JOptionPane.showConfirmDialog(window, Messages.MultipleShellExit.
                                        getText(),
                                                                      Messages.MultipleShellExitTitle.
                                        getText(),
                                                                      JOptionPane.OK_CANCEL_OPTION,
                                                                      JOptionPane.WARNING_MESSAGE);
                                if (choice == JOptionPane.OK_OPTION) {
                                    s.killAll();
                                }
                            }
                        }
                        catch (Throwable t) {
                            IOHelp.handleExceptions(ShellChrome.class,
                                                    "window$windowClosing", t,
                                                    "unable to get the current shell?");
                        }
                    }
                });
                new XShell().start(Shell.SystemLanguage.get());
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run () {
                        KeyboardFocusManager.getCurrentKeyboardFocusManager().
                                focusNextComponent(output);
                    }
                });
            }
            catch (Exception e) {
                IOHelp.handleExceptions(ShellChrome.class,
                                        "init",
                                        e,
                                        e.toString());
            }
        }
    }
    private OutputService outputService;
    private InputService inputService;
    private static ThreadFactory inputFactory = Classloader.threadFactory(
            "[ShellChrome]: input#%1$d");
    private static ThreadFactory outputFactory = Classloader.threadFactory(
            "[ShellChrome]: output#%1$d");

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        command = new JTextField();
        JScrollPane jScrollPane1 = new ScrollingSupport();
        output = new JTextPane();

        setName("Form"); // NOI18N

        command.setName("command"); // NOI18N
        TextActions.setPopupMenu(command, null);
        command.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent evt) {
                commandlistenCommand(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); // NOI18N
        jScrollPane1.setRequestFocusEnabled(false);

        output.setContentType("text/html;encoding=utf-8"); // NOI18N
        output.setEditable(false);
        output.setDragEnabled(true);
        output.setFocusCycleRoot(false);
        output.setName("output"); // NOI18N
        TextActions.setPopupMenu(output, null);
        jScrollPane1.setViewportView(output);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE)
                    .addComponent(command, GroupLayout.DEFAULT_SIZE, 398, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(command, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    private int historyCursor = 0;

    private void commandlistenCommand (KeyEvent evt) {//GEN-FIRST:event_commandlistenCommand

        switch (evt.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                sendCommand();
                break;
            case KeyEvent.VK_UP:
                moveHistCursor(historyCursor > -1, -1);
                break;
            case KeyEvent.VK_DOWN:
                moveHistCursor(historyCursor < inputHistory.count(), 1);
                break;
        }
    }//GEN-LAST:event_commandlistenCommand
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected JTextField command;
    protected JTextPane output;
    // End of variables declaration//GEN-END:variables

    private void moveHistCursor (boolean cond, int toAdd) {
        if (cond) {
            historyCursor += toAdd;
            command.setText(inputHistory.get(historyCursor));
        }
        else {
            command.setText("");
        }
    }

    private synchronized void sendCommand () {
        String cmd = this.command.getText();
        commands.add(cmd);
        this.command.setText("");
        inputHistory.add(cmd);
        historyCursor = inputHistory.count();
    }
    private LinkedBlockingQueue<String> commands;
    private static String newLine = "\n";

    private static class OutputHistory extends CyclicList<String> {

        private OutputHistory (int allocSize) {
            super(allocSize);
        }

        private String findMakeup (String last) {
            EscapeType t = ShellX.EscapeSequenceType.get();
            if (last != null && t != EscapeType.NONE) {
                String start = ShellX.makeupOpenTag(t);
                String end = ShellX.makeupCloseTag(t);
                int _end = -1, _start = last.lastIndexOf(start);
                if (_start != -1) {
                    _end = last.indexOf(end, _start);
                }
                if (_end != -1) {
                    return last.substring(_start, _end) + end;
                }
            }
            return null;
        }
        private boolean append;
        private String lastMakeup = "";

        private void addString (String s) {
            String last = findMakeup(s);
            if (append) {
                String l = getLast();
                if (l == null) {
                    setLast(s);
                }
                else {
                    setLast(getLast() + s);
                }
                append = false;
            }
            else {
                add(lastMakeup + s);
            }
            if (last != null) {
                lastMakeup = last;
            }
        }

        private void printImpl (StringBuilder s) {
            int index = -1, offset = 0, l = s.length();
            String current;
            do {
                index = s.indexOf(newLine, offset);
                if (index == -1) {
                    current = s.substring(offset);
                    addString(current);
                    return;
                }
                else {
                    current = s.substring(offset, index);
                    offset = index + 1;
                    addString(current);
                }
            }
            while (offset < l);
            addString("");
        }

        public void print (Object o) {
            printImpl(new StringBuilder(o.toString()));
            append = true;
        }

        public void println (Object o) throws Exception {
            printImpl(new StringBuilder(o.toString()));
            append = false;
        }

        @Override
        public String toString () {
            StringBuilder sb = new StringBuilder("<html><body><pre>");
            for (String s : this) {
                sb.append(s).append("<br/>");
            }
            return sb.toString();
        }
    }
    private OutputHistory outputHistory;
    private CyclicList<String> inputHistory;

    private void refresh () throws Exception {
        EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run () {
                output.setText(outputHistory.toString());
            }
        });
    }
    //</editor-fold>

    class XShell extends Shell {

        protected XShell (String name, BufferedReader reader, PrintStream stdout,
                          PrintStream stderr, String script) {
            super(name, reader, stdout, stderr, script);
        }

        protected XShell () {
            super(PredefinedScriptNames.CommandLine);
        }

        protected XShell (String name, String scriptName, PrintStream stdout,
                          PrintStream stderr, CompileUnit job) {
            super(name, scriptName, stdout, stderr, job);
        }

        @Override
        protected Shell createShellImpl (String name, String scriptName,
                                         CompileUnit u, PrintStream out,
                                         PrintStream err) {
            return new XShell(name, scriptName, out, err, u);
        }

        @Override
        protected XShell createShellImpl (
                String name, BufferedReader r,
                PrintStream stdout,
                PrintStream stderr,
                String scriptName) {
            return new XShell(name, r, stdout, stderr, scriptName);
        }

        @Override
        protected PredefinedScriptNames defaultScriptName () {
            return PredefinedScriptNames.CommandLine;
        }

        @Override
        protected void displayPrompt (final String ps) {
            outputThread().submit(new Callable<Void>() {

                @Override
                public Void call () throws Exception {
                    forwardOutput(StdStream.Stdout, ps, true);
                    return null;
                }
            });
            java.awt.EventQueue.invokeLater(new Runnable() {

                @Override
                public void run () {
                    if (window != null) {
                        window.setTitle(pathParser().getPWD().toString());
                    }
                }
            });
        }

        @Override
        public String makeup (Object text, Style style, Foreground fg,
                              Background bg) {
            if (text == null) {
                return null;
            }

            return super.makeup(text.toString().replace("&", "&amp;").
                    replace("<", "&lt;").
                    replace(">", "&gt;"), style, fg, bg);
        }

        @Override
        protected OutputService outputThread () {
            return outputService;
        }

        @Override
        protected InputService inputThread () {
            return inputService;
        }

        @Override
        protected String readLineImpl () throws Exception {
            return commands.take();
        }

        @Override
        protected void forwardFlush (StdStream stream) throws Exception {
            refresh();
        }

        @Override
        protected void forwardOutput (StdStream type, Object toWrite, boolean ln) throws
                Exception {
            if (ln) {
                outputHistory.println(toWrite);
            }
            else {
                outputHistory.print(toWrite);
            }
            refresh();
        }
    }
    private JFrame window;
}
