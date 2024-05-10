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
package org.europabarbarorum.cuf.strings.impl;

import java.util.Map;
import org.europabarbarorum.cuf.strings.impl.StringMapping.LayoutMapping;
import org.europabarbarorum.cuf.support.DefaultOption;
import org.europabarbarorum.cuf.support.IOHelp;
import org.europabarbarorum.cuf.support.OptionMap;

/**
 * Class to handle output validation/post-processing. An instance of this class is constructed to
 * ensure that the raw output obtained from resolving/applying macros is converted to a form
 * which conforms to certain output {@link DefaultOption} criteria
 * such as “no NULL characters in string data” or
 * “no consecutive space characters”.
 * @author Johan Ouwerkerk
 */
public class RawHandler {

    /**
     * Create a new {@link RawHandler} object. This constructor will throw an {@link IllegalArgumentException}
     * if the raw output supplied is too large for the “.strings.bin” format to handle.
     * @param compileOpts a {@link Map} of {@link DefaultOption} class keys to use with
     * corresponding values for the settings represented. This {@link Map} must be
     * modifiable and must not be null.
     * @param mapping the {@link StringMapping} that was used to create the raw output string.
     * @param key context key used in error message to help the user locate the source of errors.
     * @throws Exception if {@link StringMapping#getMappedString(java.lang.String) } fails.
     */
    protected RawHandler (OptionMap compileOpts,
                          StringMapping mapping, String key) throws Exception {
        this.compileOpts = compileOpts;
        this.raw = mapping.getMappedString(key);
        this.key = key;
        this.mapping = mapping;
        length = raw.length();
        outputBuffer = new StringBuilder();
    }
    private final String key;
    /**
     * Length of the {@link #raw output string}.
     */
    private final int length;
    /**
     * Marker value to track the last offset at which a chunk of output was sent to
     * {@link #outputBuffer the output buffer}.
     */
    private int mark = 0;
    /**
     * Marker value to track the last offset at which a newline was sent to
     * {@link #outputBuffer the output buffer}.
     */
    private int newline = 0;
    /**
     * The raw output to validate/post-process.
     */
    private final String raw;
    /**
     * A buffer to hold chunks of validated output.
     */
    private final StringBuilder outputBuffer;
    /**
     * The {@link StringMapping} that was used to create the raw output string.
     */
    private final StringMapping mapping;

    /**
     * Omit a character from the {@link #raw raw output}.
     * @param index the index of the character to skip in the {@link #raw raw output}.
     */
    protected void skip (int index) {
        outputBuffer.append(raw.substring(mark, index));
        mark(index);
    }

    /**
     * Advance {@link #mark} to its new position. The given index corresponds to the
     * index at which a character in the raw output triggered an `event' (a piece of post-process logic).
     * @param index the position of the last event reached.
     */
    private void mark (int index) {
        mark = index + 1;
    }

    /**
     * Inject a given string into the output; this may overwrite previously sent output.
     * @param index at which an event triggered this operation
     * @param substitute the substitute string to inject.
     * @param position postion at which the string should be injected.
     */
    protected void substitute (int index, String substitute, int position) {
        outputBuffer.replace(position, outputBuffer.length(), substitute);
        mark(index);
    }

    /**
     * Replace a character in the {@link #raw raw output}.
     * @param index the index of the character to replace in the {@link #raw raw output}.
     * @param replace the character to replace the original with
     */
    protected void replace (int index, char replace) {
        skip(index);
        outputBuffer.append(replace);
    }

    /**
     * Logic to handle NULL characters.
     * @param i index at which the character occurs in the {@link #raw output string}.
     * @param option a {@link NullCharacterOption} value determining method behaviour.
     */
    protected void handleNulls (int i, NullCharacterOption option) {
        switch (option) {
            case Disable:
                throw new IllegalArgumentException(
                        Messages.IllegalNullCharacter.format(args(i)));
            case Discard:
                IOHelp.warn(RawHandler.class,
                            Messages.IllegalNullCharacter,
                            args(i));
                skip(i);
                break;
            case Enable:
                break;
        }
    }

    /**
     * Logic to handle carriage return (CR) characters.
     * This method uses {@link #layoutLine} for conversion and layout behaviour.
     * @param i index at which the character occurs in the {@link #raw output string}.
     * @param option a {@link LineBreakOption} value determining method behaviour.
     */
    protected void handleLineBreakCR (int i, LineBreakOption option) {
        switch (option) {
            case Convert:
                IOHelp.warn(RawHandler.class,
                            Messages.LineBreakConvertWarning,
                            args(i));
                layoutLine.run(i, ControlCharacter.LineFeed.charValue);
                break;
            case Normalize:
                if (i != (length - 1) && raw.charAt(i + 1) != ControlCharacter.LineFeed.charValue) {
                    layoutLine.run(i, ControlCharacter.LineFeed.charValue);
                }
                else {
                    skip(i);
                }
                break;
            case Discard: // tumble through
                IOHelp.warn(RawHandler.class,
                            Messages.LineBreakDiscardCRWarning,
                            args(i));
            case Ignore:
                skip(i);
                break;
            case Keep: // listed here because it is an option; however nothing is to be done.
                break;
        }
    }
    /**
     * This {@link LayoutOperation} calculates text flow/layout on an entire line of text 
     * (demarcated by a newlines).
     */
    protected final LayoutOperation layoutLine = new LayoutOperation() {

        @Override
        public void run (LayoutMapping subject, int i, char c) {
            String buf = outputBuffer.substring(newline);
            String text = subject.layout(trimLine(buf));
            substitute(i, text, newline);
            outputBuffer.append(ControlCharacter.LineFeed.charValue);
            newline = outputBuffer.length();
            mark(i);
        }
    };
    /**
     * A “stop” operation which is run to ensure that the last line of text in the
     * {@link #outputBuffer} is laid out.
     */
    protected final LayoutOperation layoutStop = new LayoutOperation() {

        @Override
        protected void run (LayoutMapping subject, int index, char c) {
            layoutLine.run(subject, index, c);
            outputBuffer.deleteCharAt(outputBuffer.length() - 1);
        }
    };

    private String trimLine (String line) {
        switch (compileOpts.getOption(SpaceSequenceOption.class)) {
            case Compile:
                return line;
            case Coalesce:
                return stripSpace(line);
        }
        return line;
    }

    private String stripSpace (String text) {
        int start = 0, len = text.length(), stop = len - 1;
        if (len == 0) {
            return text;
        }
        while (ControlCharacter.forMappingCharacter(text.charAt(start), mapping) == ControlCharacter.NonBreakingSpace) {
            ++start;
        }
        while (ControlCharacter.forMappingCharacter(text.charAt(stop), mapping) == ControlCharacter.NonBreakingSpace) {
            --stop;
        }
        return text.substring(start, stop + 1);
    }
    /**
     * This {@link LayoutOperation} calculates the effect of a single character on 
     * text flow/layout. This operation should be invoked for characters that affect layout but do
     * not have their own {@link LayoutOperation}.
     */
    protected final LayoutOperation layoutEvent = new LayoutOperation() {

        @Override
        protected void run (int index, char c) {
            call = true;
            super.run(index, c);
            if (call) {
                replace(index, c);
            }
        }
        private boolean call;

        @Override
        protected void run (LayoutMapping subject, int index, char c) {
            call = false;
            int k = subject.layout(c), offset;
            if (k != LayoutMapping.NO_BREAK) {
                offset = newline + k;
                String append = outputBuffer.substring(offset);
                append = trimLine(append);
                outputBuffer.delete(offset, outputBuffer.length());
                layoutLine.run(subject, offset,
                               ControlCharacter.LineFeed.charValue);

                subject.advance(append);
                outputBuffer.append(append);

                mark(index - 1);
                event(index, c);
            }
            else {
                replace(index, c);
            }
        }
    };
    /**
     * This {@link LayoutOperation} calculates the effect of a single tab character on
     * text flow/layout. It should not be invoked for other characters.
     */
    protected final LayoutOperation layoutTab = new LayoutOperation() {

        @Override
        protected void run (LayoutMapping subject, int index, char c) {
            String indent = subject.indent();
            if (indent == null) {
                layoutLine.run(index, ControlCharacter.LineFeed.charValue);
            }
            else {
                outputBuffer.append(indent);
                mark(index);
            }
        }
    };

    /**
     * Logic to handle line feed (LF) characters.
     * @param i index at which the character occurs in the {@link #raw output string}.
     * @param option a {@link LineBreakOption} value determining method behaviour.
     */
    protected void handleLineBreakLF (int i, LineBreakOption option) {
        switch (option) {
            case Discard:
                IOHelp.warn(RawHandler.class,
                            Messages.LineBreakDiscardLFWarning,
                            args(i));
            case Ignore:
                skip(i);
                break;
            case Convert:
            case Normalize:
            case Keep:
                layoutLine.run(i, ControlCharacter.LineFeed.charValue);
                break;
        }
    }

    private Object[] args (int i) {
        return new Object[] { key, i, IOHelp.contextString(raw, i, length) };
    }

    /**
     * Logic to handle tab characters.
     * @param i index at which the character occurs in the {@link #raw output string}.
     * @param option a {@link TabOption} value determining method behaviour.
     */
    protected void handleTabs (int i, TabOption option) {
        switch (option) {
            case Discard:
                IOHelp.warn(RawHandler.class, Messages.TabWarning, args(i));
            case Ignore:
                skip(i);
                break;
            case Keep:
                layoutTab.run(i, ControlCharacter.Tab.charValue);
                break;
        }
    }

    private boolean requireCoalesceNbsp (Character c) {
        ControlCharacter k =
                ControlCharacter.forMappingCharacter(c,
                                                     mapping);
        return k != null && k != ControlCharacter.Null;
    }

    /**
     * Logic to handle non-breaking space characters.
     * @param i index at which the character occurs in the {@link #raw output string}.
     * @param option a {@link SpaceSequenceOption} value determining method behaviour.
     * @param chr the character at the given index as found in the raw output string.
     */
    private void handleNbsp (int i, SpaceSequenceOption option,
                             Character chr) {
        switch (option) {
            case Compile:
                layoutEvent.run(i, chr);
                break;
            case Coalesce:
                int l = outputBuffer.length();
                if (l == 0
                        || i == (length - 1)
                        || requireCoalesceNbsp(outputBuffer.charAt(l - 1))
                        || requireCoalesceNbsp(raw.charAt(i + 1))) {
                    skip(i);
                    break;
                }
                layoutEvent.run(i, chr);
                break;
        }
    }

    /**
     * Defines a step in molding a raw strings data into something with pre-computed
     * text flow/layout.
     */
    protected abstract class LayoutOperation {

        /**
         * Runs the {@link LayoutOperation}: this method merely invokes
         * {@link #run(org.europabarbarorum.cuf.strings.impl.StringMapping.LayoutMapping, int, char) }
         * if {@link #mapping} supports this operation.
         * @param index the event index of the given character.
         * @param c the character which triggered this {@link LayoutOperation}.
         */
        protected void run (int index, char c) {
            if (mapping instanceof LayoutMapping) {
                run((LayoutMapping) mapping, index, c);
            }
        }

        /**
         * Runs the {@link LayoutOperation}. This is the implementation of the actual operation.
         * This method should not usually be called directly: call {@link #run(int, char) } for a
         * more safe invocation.
         * @param subject a {@link LayoutMapping} which is responsible for computing the text flow/layout.
         * @param index the event index of the given character.
         * @param c the character which triggered this {@link LayoutOperation}.
         */
        protected abstract void run (LayoutMapping subject, int index, char c);
    }

    private void event (int i, Character chr) {
        ControlCharacter control = ControlCharacter.forMappingCharacter(chr,
                                                                        mapping);
        try {
            if (control == null) {
                layoutEvent.run(i, chr);
                return;
            }
            switch (control) {
                case Tab:
                    handleTabs(i, compileOpts.getOption(TabOption.class));
                    break;
                case Null:
                    handleNulls(i,
                                compileOpts.getOption(NullCharacterOption.class));
                    break;
                case NonBreakingSpace:
                    handleNbsp(i,
                               compileOpts.getOption(SpaceSequenceOption.class),
                               chr);
                    break;
                case CarriageReturn:
                    handleLineBreakCR(
                            i,
                            compileOpts.getOption(LineBreakOption.class));
                    break;
                case LineFeed:
                    handleLineBreakLF(
                            i,
                            compileOpts.getOption(LineBreakOption.class));
                    break;
                default:
                    layoutEvent.run(i, chr);
                    break;
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Messages.DebugException.format(
                    key,
                    i,
                    control == null ? chr : control.getText(),
                    IOHelp.contextString(raw, i, length),
                    e.getLocalizedMessage()),
                                               e);
        }
    }
    /**
     * Compilation options.
     */
    private final OptionMap compileOpts;

    /**
     * Run the validation/post-process logic.
     * This method loops over all the characters in the {@link #raw raw output string} and attempts to
     * match them against {@link ControlCharacter} instances. If succesful, the appropriate handler
     * function for that case is invoked with the index of the character that matches, and CompilerOption value
     * specified for the semantics of the handler function.
     * (E.g. the hanlders for the linebreak characters are called with an instance of
     * {@link LineBreakOption}.)
     */
    @SuppressWarnings("unchecked")
    protected void run () {

        for (int counter = 0; counter < length; ++counter) {
            event(counter, raw.charAt(counter));
        }
        skip(length);
        layoutStop.run(length, ControlCharacter.LineFeed.charValue);
    }

    /**
     * Get the validated/post-processed output chunks stored in {@link #outputBuffer}.
     * This method calls {@link #run() } and then returns the post processed output.
     * @return the result of applying the validation/post-process logic to {@link #raw the raw output}.
     */
    @Override
    public String toString () {
        run();
        return outputBuffer.toString();
    }

    /**
     * List of settings of how to handle linebreaks (carriage returns and line feed characters) after macro mapping.
     */
    public static enum LineBreakOption implements DefaultOption {

        /**
         * Discard carriage return and line feed characters; but emit a warning about it.
         */
        Discard,
        /**
         * Ignore carriage return and line feed characters. No warning is generated.
         */
        Ignore,
        /**
         * Keep the carriage return and line feed characters. No warning is generated.
         */
        Keep,
        /**
         * Convert carriage returns to line feed characters and emit a warning about this.
         * Treats line feed characters as in {@link #Keep}.
         */
        Convert,
        /**
         * Normalize “line breaks”: lone carriage return characters are converted to a line feed.
         * Furthermore a pair of carriage return and line feed characters is converted to a single line feed.
         * No warnings are generated and the line feed characters are treated as in {@link #Keep}.
         */
        Normalize;

        @Override
        public DefaultOption defaultOption () {
            return Normalize;
        }
    }

    /**
     * List of settings of what to do with null characters after macro mapping.
     */
    public static enum NullCharacterOption implements DefaultOption {

        /**
         * Allow NULL characters to be written to
         * the result file.
         */
        Enable,
        /**
         * Throw exceptions when a NULL character is found in a strings value.
         */
        Disable,
        /**
         * Omit NULL characters in a strings value from the output result, but warn about their presence.
         */
        Discard;

        @Override
        public DefaultOption defaultOption () {
            return Disable;
        }
    }

    /**
     * List of settings of what to do with a tab character.
     */
    public static enum TabOption implements DefaultOption {

        /**
         * Omit the tab characters from the output result, but warn about their presence.
         */
        Discard,
        /**
         * Same as {@link #Discard} except it does not generate warning.
         */
        Ignore,
        /**
         * Keep the tab characters as-is in the output result. Does not generate warnings.
         */
        Keep;

        @Override
        public DefaultOption defaultOption () {
            return Discard;
        }
    }

    /**
     * List of settings of what to do with sequences of multiple non-breaking spaces (\x20, &#92;u020).
     * This setting is intended to negate the effect of identation in sections of text that contain linebreaks;
     * as well as correct typing mistakes.
     */
    public static enum SpaceSequenceOption implements DefaultOption {

        /**
         * Coalesces multiple non-breaking spaces into a single space.
         * No warning is generated.
         */
        Coalesce,
        /**
         * Preserves all non-breaking spaces in the output; no warning is generated.
         */
        Compile;

        @Override
        public DefaultOption defaultOption () {
            return Coalesce;
        }
    }
}
