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
package org.europabarbarorum.cuf.shell;

import java.io.File;
import java.net.InetAddress;
import org.europabarbarorum.cuf.support.Setting.Modifiable;
import org.europabarbarorum.cuf.shell.EscapeSequence.Background;
import org.europabarbarorum.cuf.shell.EscapeSequence.Foreground;
import org.europabarbarorum.cuf.shell.EscapeSequence.Style;
import org.europabarbarorum.cuf.support.Escapes;
import org.europabarbarorum.cuf.support.Escapes.EscapeMapper;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.ResourceHelp.CharResource;
import org.europabarbarorum.cuf.support.ResourceHelp.StringResource;
import org.europabarbarorum.cuf.support.Setting;

/**
 * A class to describe the prompt displayed by a {@link Shell} to the user.
 * @author Johan Ouwerkerk
 */
public class PromptSettings extends EscapeMapper {

    private String makeupReset () {
        return shell.makeupReset(null)
                + shell.makeup("", style, foreground, background);
    }
    private Class nextSetting = null;
    private Foreground foreground = null;
    private Background background = null;
    private Style style = null;
    private final Shell shell;

    /**
     * Create new {@link PromptSettings}.
     * @param shell the {@link Shell} which uses these {@link PromptSettings}.
     */
    protected PromptSettings (Shell shell) {
        this.shell = shell;
    }

    @Override
    protected String mapEscape (Character code) {
        PromptEscapes p = PromptEscapes.find(code);
        if (p == null) {
            return Escapes.escape + code;
        }
        else {
            Object o = p.map(shell, this);
            if (o == null) {
                return "";
            }
            return p.isSetting() ? o.toString() : shell.makeup(o, style,
                                                               foreground,
                                                               background);
        }
    }

    private Void set (Object o) {
        if (nextSetting == null) {
            return null;
        }
        if (EscapeSequence.class.isAssignableFrom(nextSetting)) {
            Integer i = (Integer) o;

            if (nextSetting.equals(Foreground.class)) {
                Foreground[] f = Foreground.values();
                this.foreground = f.length > i ? f[i] : null;
                return null;
            }
            if (nextSetting.equals(Style.class)) {
                Style[] f = Style.values();
                this.style = f.length > i ? f[i] : null;
                return null;
            }
            if (nextSetting.equals(Background.class)) {
                Background[] f = Background.values();
                this.background = f.length > i ? f[i] : null;
                return null;
            }
        }
        return null;
    }
    private String ps1 = PS1.get();
    private String ps2 = PS2.get();

    /**
     * Set the primary prompt.
     * @param ps1 a format string for formatting the primary prompt.
     */
    public void setPS1 (String ps1) {
        this.ps1 = ps1;
    }

    /**
     * Get the primary prompt.
     * @return the format string used for formatting the primary prompt.
     * @see #getPS2()
     */
    public String getPS1 () {
        return ps1;
    }

    /**
     * Set the secondary prompt.
     * @param ps2 a format string for formatting the secondary prompt.
     */
    public void setPS2 (String ps2) {
        this.ps2 = ps2;
    }

    /**
     * Get the secondary prompt.
     * @return the format string used for formatting the secondary prompt.
     * @see #getPS1()
     */
    public String getPS2 () {
        return ps2;
    }

    /**
     * Get a prompt string.
     * @param prompt a prompt string to use
     * @return a formatted string to use for prompting shell commands.
     * @see #getPS1()
     * @see #getPS2() 
     */
    protected String formatPrompt (String prompt) {
        return substitute(prompt);
    }
    /**
     * {@link Character} preference which controls the character that triggers
     * multi-line prompt behaviour.
     */
    public static final Setting<Character> ContinuePromptChar =
            new Setting<Character>("cuf.shell.nextprompt", "\\",
                                 new CharResource(),
                                 Modifiable.Conf);
    /**
     * A {@link String} preference controlling the default PS1 prompt format used.
     */
    public static final Setting<String> PS1 = new Setting<String>("cuf.shell.ps1",
                                                              "[\\y\\0\\I\\y]:\\v\\2\\N\\v@\\y\\1\\p\\y> ",
                                                              new StringResource(),
                                                              Modifiable.Conf);
    /**
     * A {@link String} preference controlling the default PS2 prompt format used.
     */
    public static final Setting<String> PS2 = new Setting<String>("cuf.shell.ps2",
                                                              "\\\\> ",
                                                              new StringResource(),
                                                              Modifiable.Conf);

    //<editor-fold defaultstate="collapsed" desc="Prompt escapes">
    /**
     * Escape sequences that may be used in a {@link Shell} prompt format string.
     */
    public static enum PromptEscapes {

        /**
         * Escape character: \\. Used to get a single back slash.
         */
        Escape('\\') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                return '\\';
            }
        },
        /**
         * Toggles foreground makeup information: \v.
         * Note that selectors (e.g. \0) are used to select a
         * {@link Foreground} by index. For instance:
         * <blockquote><pre>
         * {@code This is default text.
         * \\v\\2This has a green foreground.\\v
         * This is normal again.
         * }</pre></blockquote>
         */
        SetForegound('v') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                boolean s = Foreground.class.equals(set.nextSetting);
                if (s) {
                    set.foreground = null;
                    set.nextSetting = null;
                    return set.makeupReset();
                }
                else {
                    set.nextSetting = Foreground.class;
                }
                return null;
            }

            @Override
            protected boolean isSetting () {
                return true;
            }
        },
        /**
         * Toggles background makeup information: \w.
         * Note that selectors (e.g. \0) are used to select a
         * {@link Background} by index. For instance:
         * <blockquote><pre>
         * {@code This is default text.
         * \\w\\4This has a blue background.\\w
         * This is normal again.
         * }</pre></blockquote>
         */
        SetBackground('w') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                boolean s = Background.class.equals(set.nextSetting);
                if (s) {
                    set.background = null;
                    set.nextSetting = null;

                    return set.makeupReset();
                }
                else {
                    set.nextSetting = Background.class;
                }
                return null;
            }

            @Override
            protected boolean isSetting () {
                return true;
            }
        },
        /**
         * Toggles style makeup information: \y.
         * Note that selectors (e.g. \0) are used to select a
         * {@link Style} by index. For instance:
         * <blockquote><pre>
         * {@code This is default text.
         * \\y\\0This is bold text.\\y
         * This is normal again.
         * }</pre></blockquote>
         */
        SetStyle('y') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                boolean s = Style.class.equals(set.nextSetting);
                if (s) {
                    set.style = null;
                    set.nextSetting = null;

                    return set.makeupReset();
                }
                else {
                    set.nextSetting = Style.class;
                }
                return null;
            }

            @Override
            protected boolean isSetting () {
                return true;
            }
        },
        /**
         * Resets makeup information: \z.
         * Implicitly closes any open \v,\w or \y sequences.
         */
        ResetMakeup('z') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                set.background = null;
                set.foreground = null;
                set.style = null;
                if (set.nextSetting != null
                        && EscapeSequence.class.isAssignableFrom(set.nextSetting)) {
                    set.nextSetting = null;
                }

                return set.makeupReset();
            }

            @Override
            protected boolean isSetting () {
                return true;
            }
        },
        /**
         * Selector: \0.
         * Selects 1st. option.
         */
        opt1('0'),
        /**
         * Selector: \1.
         * Selects 2nd. option.
         */
        opt2('1'),
        /**
         * Selector: \2.
         * Selects 3rd. option.
         */
        opt3('2'),
        /**
         * Selector: \3.
         * Selects 4th. option.
         */
        opt4('3'),
        /**
         * Selector: \4.
         * Selects 5th. option.
         */
        opt5('4'),
        /**
         * Selector: \5.
         * Selects 6th. option.
         */
        opt6('5'),
        /**
         * Selector: \6.
         * Selects 7th. option.
         */
        opt7('6'),
        /**
         * Selector: \7.
         * Selects 8th. option.
         */
        opt8('7'),
        /**
         * Selector: \8.
         * Selects 9th. option.
         */
        opt9('8'),
        /**
         * Selector: \9.
         * Selects 10th. option.
         */
        opt10('9'),
        /**
         * Selector: \a.
         * Selects 11th. option.
         */
        opt11('a'),
        /**
         * Selector: \b.
         * Selects 12th. option.
         */
        opt12('b'),
        /**
         * Selector: \c.
         * Selects 13th. option.
         */
        opt13('c'),
        /**
         * Selector: \d.
         * Selects 14th. option.
         */
        opt14('d'),
        /**
         * Selector: \e.
         * Selects 15th. option.
         */
        opt15('e'),
        /**
         * Selector: \f.
         * Selects 16th. option.
         */
        opt16('f'),
        /**
         * Index of the shell in the list of sub shells: \I.
         */
        Index('I') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                return instance.index();
            }
        },
        /**
         * File seperator character: \F.
         */
        FileSep('F') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return File.separator;
            }
        },
        /**
         * Path seperator character: \P.
         */
        PathSep('P') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return File.pathSeparator;
            }
        },
        /**
         * Newline: \n.
         */
        Newline('n') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return "\n";
            }
        },
        /**
         * Current working path: \p.
         */
        Path('p') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                return instance.pathParser().getPWD();
            }
        },
        /**
         * Host name of the machin: \h.
         */
        HostName('h') {

            @Override
            protected Object map (Shell instance, PromptSettings set) {
                try {
                    return InetAddress.getLocalHost().getHostName();
                }
                catch (Exception e) {
                    IOHelp.Log.Debug.log(Shell.class,
                                         String.format(
                            "Unable to get hostname: %s%n",
                            e.getLocalizedMessage()));
                    return "localhost";
                }
            }
        },
        /**
         * Scripting language loaded: \S.
         */
        Language('S') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return instance.loadedLanguage();
            }
        },
        /**
         * Name of the shell: \N.
         */
        Name('N') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return instance.shellName();
            }
        },
        /**
         * Time in 24-hour clock format: \T
         */
        Time24('T') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return String.format("%tT", System.currentTimeMillis());
            }
        },
        /**
         * User (account) name: &#92;u.
         */
        User('u') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return System.getProperty("user.name");
            }
        },
        /**
         * Time in 12-hour clock format: \r
         */
        Time12('r') {

            @Override
            protected String map (Shell instance, PromptSettings set) {
                return String.format("%tr", System.currentTimeMillis());
            }
        };

        private PromptEscapes (Character c) {
            this.chr = c;
        }
        private final Character chr;

        /**
         * Expand the text.
         * @param instance {@link Shell} for which to expand the text.
         * @param set {@link PromptSettings} that may be used.
         * @return an object for display, or null if the text expands to nothing.
         * @see #isSetting()
         */
        protected Object map (Shell instance, PromptSettings set) {
            String v = "" + chr;
            return set.set(Integer.parseInt(v, 16));
        }

        private static PromptEscapes find (Character c) {
            for (PromptEscapes p : PromptEscapes.values()) {
                if (p.chr == c) {
                    return p;
                }
            }
            return null;
        }

        /**
         * Return whether or not the escape sequence is modifies the
         * {@link PromptSettings} state rather than returning an expanded string.
         * @return true if a setting, false if not.
         * @see #map(org.europabarbarorum.cuf.shell.Shell, org.europabarbarorum.cuf.shell.PromptSettings) 
         */
        protected boolean isSetting () {
            return false;
        }
    }//</editor-fold>
}
