/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imagej.legacy;

import ij.ImagePlus;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.imagej.legacy.plugin.LegacyCommand;

/**
 * <p>
 * The legacy output tracker is responsible for tracking important changes to
 * the legacy ImageJ environment as a result of running a plugin. Important
 * changes include newly created {@link ImagePlus}es and {@link ImagePlus}es
 * whose window has closed.
 * <p>
 * The design maintains a pair of lists for each running {@link LegacyCommand}.
 * This is done by {@link ThreadGroup} rather than by {@link Thread}.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class LegacyOutputTracker {

	// TODO this class is loaded with static methods. There are certainly ways to
	// remove this need.

	// -- static variables --

	/**
	 * Tracks the tracker associated with each {@link LegacyCommand}'s
	 * {@link ThreadGroup}.
	 */
	private static HashMap<ThreadGroup, DefaultOutputTracker> trackers =
		new HashMap<ThreadGroup, DefaultOutputTracker>();

	private static OutputTracker nullTracker = new NullOutputTracker();
	
	// -- public static interface --

	// -- STATIC METHODS FOR TRACKING OUTPUTS --

	/**
	 * Gets a list of the currently known output {@link ImagePlus}es generated by
	 * all the threads associated with a single {@link LegacyCommand}. This code
	 * is thread safe.
	 */
	public static synchronized ImagePlus[] getOutputs() {
		return getTracker().getOutputs();
	}

	/**
	 * Add an {@link ImagePlus} to the current output list. Output lists are
	 * associated with {@link ThreadGroup}s hatched by {@link LegacyCommand}s.
	 * This code is thread safe.
	 */
	public static synchronized void addOutput(ImagePlus imp) {
		getTracker().addOutput(imp);
	}

	/**
	 * Remove an {@link ImagePlus} from the current output list. Output lists are
	 * associated with {@link ThreadGroup}s hatched by {@link LegacyCommand}s.
	 * This code is thread safe.
	 */
	public static synchronized void removeOutput(ImagePlus imp) {
		getTracker().removeOutput(imp);
	}

	/**
	 * Return true if the current output list contains the given {@link ImagePlus}
	 * . This code is thread safe.
	 */
	public static synchronized boolean containsOutput(ImagePlus imp) {
		return getTracker().containsOutput(imp);
	}

	/**
	 * Clears the list of {@link ImagePlus}es stored in the current output list.
	 * This code is thread safe.
	 */
	public static synchronized void clearOutputs() {
		getTracker().clearOutputs();
	}

	// -- STATIC METHODS FOR TRACKING CLOSED IMAGES --

	/**
	 * Gets a list of the currently known closed {@link ImagePlus}es generated by
	 * all the threads associated with a single {@link LegacyCommand}. This code
	 * is thread safe.
	 */
	public static synchronized ImagePlus[] getClosed() {
		return getTracker().getClosed();
	}

	/**
	 * Add an {@link ImagePlus} to the current closed list. Closed lists are
	 * associated with {@link ThreadGroup}s hatched by {@link LegacyCommand}s.
	 * This code is thread safe.
	 */
	public static synchronized void addClosed(ImagePlus imp) {
		getTracker().addClosed(imp);
	}

	/**
	 * Remove an {@link ImagePlus} from the current closed list. Closed lists are
	 * associated with {@link ThreadGroup}s hatched by {@link LegacyCommand}s.
	 * This code is thread safe.
	 */
	public static synchronized void removeClosed(ImagePlus imp) {
		getTracker().removeClosed(imp);
	}

	/**
	 * Return true if the current closed list contains the given {@link ImagePlus}
	 * . This code is thread safe.
	 */
	public static synchronized boolean containsClosed(ImagePlus imp) {
		return getTracker().containsClosed(imp);
	}

	/**
	 * Clears the list of {@link ImagePlus}es stored in the current closed list.
	 * This code is thread safe.
	 */
	public static synchronized void clearClosed() {
		getTracker().clearClosed();
	}

	// -- helpers --

	private static OutputTracker getTracker() {
		ThreadGroup group = Utils.findLegacyThreadGroup(Thread.currentThread());
		if (group == null) return nullTracker;
		DefaultOutputTracker tracker = trackers.get(group);
		if (tracker == null) {
			tracker = new DefaultOutputTracker();
			trackers.put(group, tracker);
		}
		return tracker;
	}

	private interface OutputTracker {

		void addClosed(ImagePlus imp);

		void addOutput(ImagePlus imp);

		void clearClosed();

		void clearOutputs();

		boolean containsClosed(ImagePlus imp);

		boolean containsOutput(ImagePlus imp);

		ImagePlus[] getClosed();

		ImagePlus[] getOutputs();

		void removeClosed(ImagePlus imp);

		void removeOutput(ImagePlus imp);
	}
	
	private static class NullOutputTracker implements OutputTracker {
		
		@Override
		public void addClosed(ImagePlus imp) {
			// do nothing
		}

		@Override
		public void addOutput(ImagePlus imp) {
			// do nothing
		}

		@Override
		public void clearClosed() {
			// do nothing
		}

		@Override
		public void clearOutputs() {
			// do nothing
		}

		@Override
		public boolean containsClosed(ImagePlus imp) {
			return false;
		}

		@Override
		public boolean containsOutput(ImagePlus imp) {
			return false;
		}

		@Override
		public ImagePlus[] getClosed() {
			return new ImagePlus[0];
		}

		@Override
		public ImagePlus[] getOutputs() {
			return new ImagePlus[0];
		}

		@Override
		public void removeClosed(ImagePlus imp) {
			// do nothing
		}

		@Override
		public void removeOutput(ImagePlus imp) {
			// do nothing
		}
	}

	private static class DefaultOutputTracker implements OutputTracker {

		// -- instance variables --

		/**
		 * Used to provide the list of output {@link ImagePlus}es associated with a
		 * {@link LegacyCommand}'s {@link ThreadGroup}.
		 */
		private Set<ImagePlus> outputs = new HashSet<ImagePlus>();

		/**
		 * Used to provide the list of closed {@link ImagePlus}es associated with a
		 * {@link LegacyCommand}'s {@link ThreadGroup}.
		 */
		private Set<ImagePlus> closed = new HashSet<ImagePlus>();

		// -- instance public interface --

		@Override
		public void addClosed(ImagePlus imp) {
			closed.add(imp);
		}

		@Override
		public void addOutput(ImagePlus imp) {
			outputs.add(imp);
		}

		@Override
		public void clearClosed() {
			closed.clear();
		}

		@Override
		public void clearOutputs() {
			outputs.clear();
		}

		@Override
		public boolean containsClosed(ImagePlus imp) {
			return closed.contains(imp);
		}

		@Override
		public boolean containsOutput(ImagePlus imp) {
			return outputs.contains(imp);
		}

		@Override
		public ImagePlus[] getClosed() {
			return closed.toArray(new ImagePlus[0]);
		}

		@Override
		public ImagePlus[] getOutputs() {
			return outputs.toArray(new ImagePlus[0]);
		}

		@Override
		public void removeClosed(ImagePlus imp) {
			closed.remove(imp);
		}

		@Override
		public void removeOutput(ImagePlus imp) {
			outputs.remove(imp);
		}

	}
}