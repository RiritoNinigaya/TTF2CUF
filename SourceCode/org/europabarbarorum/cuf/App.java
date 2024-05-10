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
package org.europabarbarorum.cuf;

import java.awt.GraphicsEnvironment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.gui.ShellWindow;
import org.europabarbarorum.cuf.shell.ScriptRunner;
import org.europabarbarorum.cuf.shell.Shell;
import org.europabarbarorum.cuf.shell.ShellX;
import org.europabarbarorum.cuf.support.ResourceHelp.BooleanResource;
import org.europabarbarorum.cuf.support.Setting;

/**
 *
 * @author Johan Ouwerkerk
 */
public class App {

    private static Runnable interactive () {
        return new Runnable() {

            @Override
            public void run () {
                if (HasGUI.get()) {
                    Boolean decorated = DecoratedWindows.get();
                    JFrame.setDefaultLookAndFeelDecorated(decorated);
                    JDialog.setDefaultLookAndFeelDecorated(decorated);
                    ShellWindow window = new ShellWindow();
                    window.setLocationRelativeTo(null);
                    window.setVisible(true);
                }
                else {
                    new ShellX() {}.start(Shell.SystemLanguage.get());
                }
            }
        };
    }

    /**
     * {@link Boolean} preference which determines whether or not the CUF program should
     * ask the Java LookAndFeel to decorate the windows.
     */
    public static final Setting<Boolean> DecoratedWindows = new Setting<Boolean>(
            "cuf.windows.decorated",
            "false",
            new BooleanResource(),
            Modifiable.Cli);
    /**
     * {@link Boolean} preference which determines whether or not the shell runs with a GUI.
     * This setting cannot be altered if the JVM does not support a GUI.
     * @see GraphicsEnvironment#isHeadless()
     */
    public static final Setting<Boolean> HasGUI = new Setting<Boolean>(
            "cuf.shell.chrome",
            GraphicsEnvironment.isHeadless() ? "false" : "true",
            new BooleanResource(),
            GraphicsEnvironment.isHeadless() ? Modifiable.No : Modifiable.Cli);

    /**
     * Runs the program.
     * This is not the main entry point: that can be found at {@link Main#main(java.lang.String[]) }.
     * @param args commandline arguments to the program.
     * @throws Exception if an error occurs.
     */
    public static void main (final String[] args) throws Exception {

        java.awt.EventQueue.invokeAndWait(new Runnable() {

            @Override
            public void run () {
                Runnable impl = args.length == 0
                        ? interactive()
                        : new ScriptRunner(args);
                impl.run();
            }
        });
    }
}
