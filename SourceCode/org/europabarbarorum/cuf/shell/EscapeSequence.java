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

/**
 * Interface to define bits of styling information, known as escape sequences in terminal emulators.
 * @author Johan Ouwerkerk
 */
public interface EscapeSequence {

    /**
     * Produces the right incantation to get a certain style for a given {@link EscapeType}.
     * @param type the {@link EscapeType} used by the {@link Shell}. {@link EscapeSequence} objects
     * should define multiple codes for different types of environments (if possible) so that the
     * {@link Shell} can declaratively use styles instead of having to handle all the code format conversions.
     * @return the magic codes that will generate the desired effect
     */
    String sequence (EscapeType type);

    //<editor-fold defaultstate="collapsed" desc="Style escape codes">
    /**
     * List of predefined styles for
     * {@link Shell#makeup(java.lang.Object, org.europabarbarorum.cuf.shell.EscapeSequence.Style, org.europabarbarorum.cuf.shell.EscapeSequence.Foreground, org.europabarbarorum.cuf.shell.EscapeSequence.Background) }.
     * @see EscapeType
     * @see EscapeSequence
     */
    public enum Style implements EscapeSequence {

        /**
         * Bold (light text in ANSI) style.
         */
        BOLD("1", "font-weight:bold;"),
        /**
         * Underline style.
         */
        UNDERLINE("4", "text-decoration:underline;"),
        /**
         * Blink style to annoy people with.
         */
        BLINK("5", "text-decoration:blink;"),
        /**
         * Invert, a style only useful to invert {@link Background} and {@link Foreground} colours in ANSI mode.
         */
        INVERT("7", "");
        private final String _s;
        private final String _h;

        private Style (String seq, String css) {
            this._s = seq;
            this._h = css;
        }

        @Override
        public String sequence (EscapeType type) {
            switch (type) {
                case ANSI:
                    return _s;
                case HTML:
                    return _h;
                default:
                    return "";
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Foreground colour escape codes">
    /**
     * List of predefined foreground colours for
     * {@link Shell#makeup(java.lang.Object, org.europabarbarorum.cuf.shell.EscapeSequence.Style, org.europabarbarorum.cuf.shell.EscapeSequence.Foreground, org.europabarbarorum.cuf.shell.EscapeSequence.Background) }.
     * @see EscapeType
     * @see EscapeSequence
     */
    public enum Foreground implements EscapeSequence {

        /**
         * Black foreground colour.
         */
        BLACK("30", "color:#000000"),
        /**
         * Dark red foreground colour.
         */
        RED("31", "color:#800000"),
        /**
         * Dark green foreground colour.
         */
        GREEN("32", "color:#008000"),
        /**
         * Brown foregroud
         */
        BROWN("33", "color:#804000"),
        /**
         * Dark blue foreground colour.
         */
        BLUE("34", "color:#000080"),
        /**
         * Purple foreground colour.
         */
        PURPLE("35", "color:#800080"),
        /**
         * Dark cyan foreground colour.
         */
        CYAN("36", "color:#008080"),
        /**
         * Dark grey foreground colour.
         */
        DARK_GREY("37", "color:#808080"),
        /**
         * Light grey foreground colour.
         */
        GREY("1;30", "color:#C0C0C0"),
        /**
         * Bright red foreground colour.
         */
        LIGHT_RED("1;31", "color:#ff0000"),
        /**
         * Bright green foreground colour.
         */
        LIGHT_GREEN("1;32", "color:#00ff00"),
        /**
         * Yellow foreground colour.
         */
        YELLOW("1;33", "color:#f4e400"),
        /**
         * Bright blue foreground colour.
         */
        LIGHT_BLUE("1;34", "color:#0000ff"),
        /**
         * Bright pink foreground colour.
         */
        LIGHT_PURPLE("1;35", "color:#ff00ff"),
        /**
         * Bright cyan foreground colour.
         */
        LIGHT_CYAN("1;36", "color:#00ffff");
        private final String _s;
        private final String _h;

        private Foreground (String seq, String css) {
            this._s = seq;
            this._h = css;
        }

        @Override
        public String sequence (EscapeType type) {
            switch (type) {
                case ANSI:
                    return _s;
                case HTML:
                    return _h;
                default:
                    return "";
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Background colour escape codes">
    /**
     * List of predefined background colours for 
     * {@link Shell#makeup(java.lang.Object, org.europabarbarorum.cuf.shell.EscapeSequence.Style, org.europabarbarorum.cuf.shell.EscapeSequence.Foreground, org.europabarbarorum.cuf.shell.EscapeSequence.Background) }
     * @see EscapeType
     * @see EscapeSequence
     */
    public enum Background implements EscapeSequence {

        /**
         * Black background colour.
         */
        BLACK("40", "background:#000000"),
        /**
         * Red background colour.
         */
        RED("41", "background:#ff0000"),
        /**
         * Green background colour.
         */
        GREEN("42", "background:#00ff00"),
        /**
         * Brown background colour.
         */
        BROWN("43", "background:#804000"),
        /**
         * Blue background colour.
         */
        BLUE("44", "background:#0000ff"),
        /**
         * Purple background colour.
         */
        PURPLE("45", "background:#800080"),
        /**
         * Cyan background colour.
         */
        CYAN("46", "background:#008080"),
        /**
         * Light grey background colour.
         */
        GREY("47", "background:#C0C0C0");
        private final String _s;
        private final String _h;

        private Background (String seq, String css) {
            this._s = seq;
            this._h = css;
        }

        @Override
        public String sequence (EscapeType type) {
            switch (type) {
                case ANSI:
                    return _s;
                case HTML:
                    return _h;
                default:
                    return "";
            }
        }
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Escape code types">
    /**
     * Type of escape sequences supported by the {@link Shell}.
     * @see EscapeSequence
     */
    public static enum EscapeType {

        /**
         * Use HTML to style text. This works only within a Swing type GUI.
         */
        HTML,
        /**
         * Use ANSI escape sequences to style text. This works only on the commandline, of
         * compatible terminal emulators and tty's.
         */
        ANSI,
        /**
         * Use no makeup. Works everywhere.
         */
        NONE;
    }
    //</editor-fold>
}
