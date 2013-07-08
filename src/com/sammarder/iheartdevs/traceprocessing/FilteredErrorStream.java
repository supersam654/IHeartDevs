package com.sammarder.iheartdevs.traceprocessing;

import java.io.PrintStream;
import java.util.Locale;

import com.sammarder.iheartdevs.InputProcessingException;

/**
 * Pseudo PrintStream that redirects all output to each IMessageProcessor passed into the constructor. Once an
 * IMessageProcessor "accepts" the message, further IMessageProcessors will not be called.
 */
public class FilteredErrorStream extends PrintStream {
	// I know this is technically just a char, but it is easier to call it a string.
	public static final String NL = "\n";

	// The size of this can't change so it might as well be ultra-efficient.
	private final IMessageProcessor[] processors;

	/**
	 * Constructor for initializing a new FilteredErrorStream.
	 * 
	 * @param processors
	 *            A list of IMessageProcessors to call when input is received.
	 */
	public FilteredErrorStream(IMessageProcessor... processors) {
		super(System.err);
		this.processors = processors;
	}

	/**
	 * All printX, format, append, and write calls get forwarded here. This calls all IMessageProcessors associated with
	 * this object in an attempt to successfully resolve the message.
	 * 
	 * @param s
	 *            The message to interpret.
	 */
	private void process(String s) {
		for (IMessageProcessor processor : processors) {
			if (processor.process(s)) {
				// process() returns true if the processor successfully digested the message.
				return;
			}
		}
		// None of the processors accepted the message, so print a stack trace.
		// Note that I am fully aware that I will "catch" this and print it to a file.
		// Based on how the program could potentially get to this point, throwing the exception is worthless.
		new InputProcessingException("Input: " + s).printStackTrace();
	}

	// All of the below methods defer the act of writing output to process(). process() will determine what to do with
	// the message and act accordingly.

	// Also overrides anything to do with setting the error state of this stream. Because this stream doesn't actually
	// write anything, it can never be in an error state.

	// flush() and close() get passed up the parent. Like the real System.err, you should never call either of those
	// anyways.

	@Override
	public PrintStream append(char c) {
		process(String.valueOf(c));
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq) {
		process(csq.toString());
		return this;
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		process(csq.subSequence(start, end).toString());
		return this;
	}

	@Override
	public boolean checkError() {
		// This stream cannot be in an error state.
		return false;
	}

	@Override
	protected void clearError() {
		// There is no error so there is nothing to do.
	}

	@Override
	public void close() {
		// Not sure why anyone would want to, but it's allowed.
		super.close();
	}

	@Override
	public void flush() {
		// Not sure why anyone would want to, but it's allowed.
		super.flush();
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		process(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream format(String format, Object... args) {
		process(String.format(format, args));
		return this;
	}

	@Override
	public void print(boolean b) {
		process(String.valueOf(b));
	}

	@Override
	public void print(char c) {
		process(String.valueOf(c));
	}

	@Override
	public void print(char[] chars) {
		process(String.valueOf(chars));
	}

	@Override
	public void print(double d) {
		process(String.valueOf(d));
	}

	@Override
	public void print(float f) {
		process(String.valueOf(f));
	}

	@Override
	public void print(int i) {
		process(String.valueOf(i));
	}

	@Override
	public void print(long l) {
		process(String.valueOf(l));
	}

	@Override
	public void print(Object obj) {
		process(String.valueOf(obj));
	}

	@Override
	public void print(String s) {
		process(s);
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		process(String.format(l, format, args));
		return this;
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		process(String.format(format, args));
		return this;
	}

	@Override
	public void println() {
		process(NL);
	}

	@Override
	public void println(boolean b) {
		process(String.valueOf(b) + NL);
	}

	@Override
	public void println(char c) {
		process(String.valueOf(c) + NL);
	}

	@Override
	public void println(char[] chars) {
		process(String.valueOf(chars) + NL);
	}

	@Override
	public void println(double d) {
		process(String.valueOf(d) + NL);
	}

	@Override
	public void println(float f) {
		process(String.valueOf(f) + NL);
	}

	@Override
	public void println(int i) {
		process(String.valueOf(i) + NL);
	}

	@Override
	public void println(long l) {
		process(String.valueOf(l) + NL);
	}

	@Override
	public void println(Object o) {
		process(String.valueOf(o) + NL);
	}

	@Override
	public void println(String s) {
		process(String.valueOf(s) + NL);
	}

	@Override
	public void setError() {
		// Do nothing because this stream can't be in an error state.
	}

	@Override
	public void write(int b) {
		process(String.valueOf(b));
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		process(new String(buf, off, len));
	}
}
