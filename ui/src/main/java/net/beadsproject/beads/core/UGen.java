/*
 * This file is part of Beads. See http:
 */
package net.beadsproject.beads.core;

import jcog.data.list.FastCoWList;
import jcog.data.list.Lst;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Clock;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A UGen is the main base class for implementing signal generation and processing units (unit generators). UGens can have any number of audio input and output channels, which adopt the audio format of the {@link AudioContext} used to construct the UGen. Any UGen output can be connected to any other UGen input, using {@link #addInput(int, UGen, int)} (or use {@link #in(UGen)} to connect all outputs of one UGen to all inputs of another). UGens are constructed using an
 * AudioContext to determine the correct buffer size for audio processing. By connecting a UGen's output to another
 * UGen's input the source UGen is automatically added to a call chain that propagates
 * through subsequent UGens from the root UGen of the AudioContext. UGens that
 * do not have outputs (such as {@link Clock}) can be added
 * manually to the call chain using {@link #dependsOn(UGen)} from any UGen
 * that is part of the call chain (such as the root UGen of the {@link AudioContext}).
 * <p>
 * </p>UGen inherits the
 * {@link Auvent#start()}, {@link Auvent#stop()} and {@link Auvent#pause(boolean)} behaviour, and messaging system from
 * {@link Auvent}. Importantly, when UGens are paused, they cease audio processing, and when they are killed, they are automatically removed from any audio chains. This allows for very easy removal of elements from the call chain.
 * <p>
 * </p>The method {@link #gen()} must be implemented by subclasses of UGen that actually do something. Each UGen has two 2D arrays of floats, {@link #bufIn}, {@link #bufOut}, holding the current input and output audio buffers (this is stored in the form float[numChannels][bufferSize]). The goal of a {@link UGen#gen()} method, therefore, is to fill {@link #bufOut} with appropriate data for the current audio frame. Examples can be found in the source code of classes in the {@link net.beadsproject.beads.ugens} package.
 *
 * @author ollie
 */
public abstract class UGen extends Auvent {

	private static final Map<Class<?>, HashMap<String, Method>> envelopeGetterMethods = new HashMap<>();
	/**
	 * A collection of UGens that should be triggered by this one.
	 */
	private final FastCoWList<UGen> dependents;
	/**
	 * The AudioContext used by this buffer.
	 */
	protected AudioContext context;
	/**
	 * The number of inputs.
	 */
	protected int ins;
	/**
	 * The number of outputs.
	 */
	protected int outs;
	/**
	 * The buffer used internally to store input data. Array has dimensions [channels][bufferSize].
	 */
	protected float[][] bufIn;
	/**
	 * The buffer that will be grabbed by other UGens connected to this one. Array has dimensions [channels][bufferSize].
	 */
	protected float[][] bufOut;
	/**
	 * The buffer size. This is specified by {@link AudioContext}.
	 */
	protected int bufferSize;
	protected OutputInitializationRegime outputInitializationRegime;
	protected OutputPauseRegime outputPauseRegime;

	//private boolean timerMode;
	//private long timeTakenLastUpdate;
	//private long timeTemp;
	/**
	 * An collection of pointers to the output buffers of UGens connected to this UGen's inputs.
	 */
	private Lst<BufferPointer>[] inputsAtChannel;
	/**
	 * Flag used to avoid calling {@link #pullInputs()} unless required.
	 */
	private boolean noInputs;
	/**
	 * Counter to track of whether this UGen has been updated at this timeStep (determined by {@link AudioContext}).
	 */
	private long lastTimeStep;

	/**
	 * Create a new UGen from the given AudioContext with no inputs and the
	 * given number of outputs.
	 *
	 * @param context AudioContext to use.
	 * @param outs    number of outputs.
	 */
	public UGen(AudioContext context, int outs) {
		this(context, 0, outs);
	}


	/**
	 * Create a new UGen from the given AudioContext with the given number of
	 * inputs and outputs.
	 *
	 * @param context AudioContext to use.
	 * @param ins     number of inputs.
	 * @param outs    number of outputs.
	 */
	public UGen(AudioContext context, int ins, int outs) {
		dependents = new FastCoWList<>(UGen.class);
		noInputs = true;
		lastTimeStep = -1;
		outputInitializationRegime = OutputInitializationRegime.JUNK;
		outputPauseRegime = OutputPauseRegime.ZERO;
		//timerMode = false;
		//timeTemp = 0;

		setIns(ins);
		setOuts(outs);
		setContext(context);
	}

	/**
	 * Gets the AudioContext used by this UGen.
	 *
	 * @return the AudioContext.
	 */
	public AudioContext getContext() {
		return context;
	}

	/**
	 * Sets the AudioContext used by this UGen. Resetting the AudioContext after initialization could have unexpected consequences.
	 *
	 * @param context the AudioContext.
	 */
	private void setContext(AudioContext context) {
//	    if (context == null && this instanceof AudioContext)
//	        context = (AudioContext) this;

		this.context = context;
		if (context != null) {
			bufferSize = context.getBufferSize();
			setupInputBuffer();
			setupOutputBuffer();
			zeroIns();
			zeroOuts();
		} else {
			bufIn = null;
			bufOut = null;
		}
	}

	/**
	 * Gets the number of inputs.
	 *
	 * @return number of inputs.
	 */
	public int getIns() {
		return ins;
	}

	/**
	 * Set the number of inputs.
	 *
	 * @param ins number of inputs.
	 */
	@SuppressWarnings("unchecked")
	private synchronized void setIns(int ins) {
		this.ins = ins;
		inputsAtChannel = new Lst[ins];
		for (int i = 0; i < ins; i++) {
			inputsAtChannel[i] = new Lst<>();
		}
	}

	/**
	 * Gets the number of outputs.
	 *
	 * @return number of outputs.
	 */
	public int getOuts() {
		return outs;
	}

	/**
	 * Sets the number of outputs.
	 *
	 * @param outs number of outputs.
	 */
	private void setOuts(int outs) {
		this.outs = outs;
	}

	/**
	 * Sets up the input buffer. Called when number of inputs or buffer size is changed.
	 */
	private void setupInputBuffer() {
		bufIn = new float[ins][];
	}

	/**
	 * Sets up output buffer. Called when number of outputs or buffer size is changed.
	 */
	private void setupOutputBuffer() {
		bufOut = new float[outs][];
	}

	/**
	 * Sets the output buffers to zero.
	 */
	private void zeroOuts() {
        float[] z = context.getZeroBuf();
        Arrays.fill(bufOut, 0, outs, z);
	}

	/**
	 * Sets the input buffers to zero.
	 */
	private void zeroIns() {
        float[] z = context.getZeroBuf();
        Arrays.fill(bufIn, 0, ins, z);
	}

	private void setOutsToPause() {
		switch (outputPauseRegime) {
			case ZERO:
			    zeroOuts();
				break;
			case NULL:
			    Arrays.fill(bufOut, 0, outs, null);
				break;
			case RETAIN:
			default:
				break;
		}
	}

	private void initializeOuts() {
		switch (outputInitializationRegime) {
			case JUNK:
				for (int i = 0; i < outs; i++) {
					bufOut[i] = context.getBuf();
				}
				break;
			case ZERO:
				for (int i = 0; i < outs; i++) {
					bufOut[i] = context.getCleanBuf();
				}
				break;
			case RETAIN:
				break;
			case NULL:
			default:
				Arrays.fill(bufOut, 0, outs, null);
				break;
		}
	}

	/**
	 * Tells all UGens up the call chain, and all UGens that are dependents of this UGen, to calculate their ouput buffers.
	 */
	private synchronized void pullInputs() {


		dependents.removeIf(dependent -> {
			if (dependent.isDeleted()) {
				return true;
			} else {
				dependent.update();
				return false;
			}
		});


		if (!noInputs) {
			noInputs = true;
			for (int i = 0; i < inputsAtChannel.length; i++) {


				int size = inputsAtChannel[i].size();
				bufIn[i] = context.getZeroBuf();
				if (size == 1) {
					BufferPointer bp = inputsAtChannel[i].get(0);
					if (bp.ugen.isDeleted()) {
						removeInputAtChannel(i, bp);
					} else {
						bp.ugen.update();
						noInputs = false;

						bufIn[i] = bp.getBuffer();


						if (bufIn[i] == null) {
							float[] bi = bufIn[i] = context.getBuf();
							for (int j = 0; j < bufferSize; j++) {
								bi[j] = bp.get(j);
							}
						}
					}
				} else if (size != 0) {
					float[] bi = bufIn[i] = context.getCleanBuf();
					for (int index = 0; index < size; index++) {
						BufferPointer bp = inputsAtChannel[i].get(index);
						if (bp.ugen.isDeleted()) {

							removeInputAtChannel(i, bp);
							size--;
							index--;
						} else {
							bp.ugen.update();
							noInputs = false;
							for (int j = 0; j < bufferSize; j++) {
								bi[j] += bp.get(j);
							}
						}
					}

				}
			}
		} else if (ins != 0) {
			for (int i = 0; i < inputsAtChannel.length; i++) {
				bufIn[i] = context.getZeroBuf();
			}
		}
	}

	/**
	 * Updates the UGen. If the UGen is paused or has already been updated at
	 * this time step (according to the {@link AudioContext}) then this method does nothing. If the UGen does update, it
	 * will firstly propagate the {@link #update()} call up the call chain using {@link #pullInputs()}, and secondly, call its own {@link #gen()} method.
	 */
	public void update() {
		if (!isPaused()) {
			if (!isUpdated()) {
				//if (timerMode) {
				//timeTemp = System.nanoTime();
				//}
				lastTimeStep = context.getTimeStep();
				pullInputs();


				initializeOuts();
				gen();
				//if (timerMode) {
				//timeTakenLastUpdate = System.nanoTime() - timeTemp;
				//}
			}


		} else {
			setOutsToPause();
		}
	}

	/**
	 * Prints a list of UGens connected to this UGen's inputs to System.out.
	 */
	public void printInputList() {
		for (Lst<BufferPointer> anInputsAtChannel : inputsAtChannel) {
			System.out.print(anInputsAtChannel.size() + " inputs: ");
			for (BufferPointer bp : anInputsAtChannel) {
				System.out.print(bp.ugen + ":" + bp.index + ' ');
			}
			System.out.println();
		}
	}

	/**
	 * Connect another UGen's outputs to the inputs of this UGen. If the number
	 * of outputs is greater than the number of inputs then the extra outputs are not connected. If the number of inputs is greater than the number of outputs then the outputs are cycled to fill all inputs. If
	 * multiple UGens are connected to any one input then the outputs from those
	 * UGens are summed on their way into the input.
	 *
	 * @param sourceUGen the UGen to connect to this UGen.
	 */
	public synchronized <X extends UGen> X in(UGen sourceUGen) {
		if (ins != 0 && sourceUGen.outs != 0) {
			for (int i = 0; i < ins; i++) {

				addInput(i, sourceUGen, i % sourceUGen.outs);
			}
		}
		return (X) this;
	}

	/**
	 * Connect a specific output from another UGen to a specific input of this
	 * UGen.
	 *
	 * @param inputIndex        the input of this UGen to connect to.
	 * @param sourceUGen        the UGen to connect to this UGen.
	 * @param sourceOutputIndex the output of the connecting UGen with which to make the
	 *                          connection.
	 */
	public synchronized void addInput(int inputIndex, UGen sourceUGen, int sourceOutputIndex) {
		inputsAtChannel[inputIndex].add(new BufferPointer(sourceUGen, sourceOutputIndex));

		noInputs = false;
	}

	/**
	 * Performs a crossfade from one UGen (which must already be connected) to another. Only works if you
	 *
	 * @param source        the UGen to crossfade away from (assumed to already be connected), will be disconnected once cross-fade is over.
	 * @param destination   the UGen to crossfade towards.
	 * @param crossoverTime the time taken.
	 */
	public synchronized void crossfadeInput(UGen source, UGen destination, float crossoverTime) {
		removeAllConnections(source);

		Envelope fadeOut = new Envelope(context, 1.0f);
		Gain gOut = new Gain(context, source.outs, fadeOut);
		fadeOut.add(0.0f, crossoverTime, new KillTrigger(gOut));
		gOut.in(source);
		in(gOut);

		Envelope fadeIn = new Envelope(context, 0.0f);
		Gain gIn = new Gain(context, destination.outs, fadeIn);
		fadeIn.add(1.0f, crossoverTime, new Auvent() {
			@Override
			public void on(Auvent message) {
				removeAllConnections(gIn);
				in(destination);
			}
		});
		gIn.in(source);
		in(gIn);
	}

	/**
	 * Adds a UGen to this UGen's dependency list, causing the dependent UGen to
	 * get updated when this one does. This is used to add UGens without outputs (such as {@link Clock} to the call chain. As will UGens in the regular call chain, if a dependent UGen gets killed, this UGen will remove it from its dependency list.
	 *
	 * @param dependent the dependent UGen.
	 */
	public void dependsOn(UGen dependent) {
		dependents.add(dependent);
	}

	/**
	 * Removes the specified UGen from this UGen's dependency list.
	 *
	 * @param dependent UGen to remove.
	 */
	public void removeDepends(UGen dependent) {
		dependents.remove(dependent);
	}

	/**
	 * Clears the list of dependent UGens.
	 */
	public void clearDepends() {
		dependents.clear();
	}

	/**
	 * Gets the number of UGens connected at the specified input index of
	 * this UGen.
	 *
	 * @param index index of input to inspect.
	 * @return number of UGen outputs connected to that input.
	 */
	protected int connectedCount(int index) {
		return inputsAtChannel[index].size();
	}

	/**
	 * Gets the number of dependent UGens.
	 *
	 * @return number of dependent UGens.
	 */
	public int dependentCount() {
		return dependents.size();
	}

	/**
	 * Returns a flat Set (i.e. no copies) of all the UGens connected to the inputs of this one.
	 *
	 * @return set of UGens
	 */
	public synchronized Set<UGen> getConnectedInputs() {
		Set<UGen> connectedInputs = IntStream.range(0, ins).mapToObj(i -> inputsAtChannel[i].stream()).flatMap(Function.identity()).map(bp -> bp.ugen).collect(Collectors.toSet());
        return connectedInputs;
	}

//	/**
//	 * Returns a List of BufferPointers representing connected UGens and their output channels connected to the given input channel.
//	 *
//	 * @param index input channel index
//	 * @return List of BufferPointers (UGen and output channel)
//	 */
//	@SuppressWarnings("unchecked")
//	public synchronized List<BufferPointer> getBufferPointers(int index) {
//		return inputsAtChannel[index].clone();
//	}
//
//	private void findEnvelopeGetterMethods() {
//		Class<?> _c = getClass();
//		envelopeGetterMethods.computeIfAbsent(_c, (c) -> {
//			HashMap<String, Method> methodTable = new HashMap<>();
//			Method[] methods = c.getMethods();
//			for (Method m : methods) {
//				String name = m.getName();
//				if (name.startsWith("get") && name.endsWith("UGen") && m.getReturnType().equals(UGen.class)) {
//					String envelopeName = name.substring(3, 3).toLowerCase() + name.substring(4, name.length() - 8);
//					methodTable.put(envelopeName, m);
//				}
//			}
//			return methodTable;
//		});
//	}

	private void removeInputAtChannel(int channel, BufferPointer bp) {
		Lst<BufferPointer> ic = inputsAtChannel[channel];
		synchronized (ic) {
			ic.remove(bp);
		}
	}

	/**
	 * Disconnects the specified UGen from this UGen at all inputs.
	 *
	 * @param sourceUGen the UGen to disconnect.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void removeAllConnections(UGen sourceUGen) {
		if (!noInputs) {
			int inputCount = 0;
			for (int i = 0; i < inputsAtChannel.length; i++) {
				List<BufferPointer> bplist = inputsAtChannel[i].clone();
				for (BufferPointer bp : bplist) {
					if (sourceUGen.equals(bp.ugen)) {
						removeInputAtChannel(i, bp);
					} else
						inputCount++;
				}
			}
			if (inputCount == 0) {
				noInputs = true;
				zeroIns();
			}
		}
	}

//    /**
//     * Gets the envelopes controlling this UGen (using Reflection).
//     *
//     * @return Map of envelope names to envelopes.
//     */
//    public synchronized Map<String, UGen> getEnvelopes() {
//        Map<String, UGen> envelopes = new HashMap<>();
//        findEnvelopeGetterMethods();
//        HashMap<String, Method> methodTable = envelopeGetterMethods.get(getClass());
//        for (Map.Entry<String, Method> stringMethodEntry : methodTable.entrySet()) {
//            Method m = stringMethodEntry.getValue();
//            try {
//                envelopes.put(stringMethodEntry.getKey(), (UGen) m.invoke(this, ArrayUtil.EMPTY_OBJECT_ARRAY));
//            } catch (Exception e) {
//
//            }
//        }
//        return envelopes;
//    }

	/**
	 * Disconnects the connection from the specified UGen to this one.
	 *
	 * @param inputChannel        The channel of this UGen to check.
	 * @param sourceUGen          The UGen to disconnect.
	 * @param sourceOutputChannel The channel of the source UGen.
	 * @return True if a connection was removed; false otherwise.
	 */
	@SuppressWarnings("unchecked")
	public synchronized boolean removeConnection(int inputChannel,
												 UGen sourceUGen, int sourceOutputChannel) {

		if (!noInputs) {
			int inputCount = 0;
			boolean ret = false;
			List<BufferPointer> bplist = inputsAtChannel[inputChannel].clone();
			for (BufferPointer bp : bplist) {
				if (sourceUGen.equals(bp.ugen)
					&& bp.index == sourceOutputChannel) {
					removeInputAtChannel(inputChannel, bp);
					ret = true;
				} else {
					inputCount++;
				}
			}
			if (inputCount == 0) {

				int sum = Arrays.stream(inputsAtChannel).mapToInt(Lst::size).sum();
				inputCount += sum;
			}
			if (inputCount == 0) {
				noInputs = true;
				zeroIns();
			}
			return ret;
		} else {
			return false;
		}
	}

	/**
	 * Clear all of this UGen's input connections.
	 */
	@SuppressWarnings("unchecked")
	public synchronized void clearInputConnections() {
		for (int i = 0; i < inputsAtChannel.length; i++) {
			List<BufferPointer> bplist = inputsAtChannel[i].clone();
			for (BufferPointer bp : bplist)
				removeInputAtChannel(i, bp);
			noInputs = true;
			zeroIns();
		}
	}

	/**
	 * Prints the contents of the input buffers to System.out.
	 */
	public void printInBuffers() {
		for (int i = 0; i < bufferSize; i++) {
			System.out.print(this + " " + i + ' ');
			for (int j = 0; j < ins; j++) {
				System.out.print(bufIn[j][i] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Prints the contents of the output buffers to System.out.
	 */
	public void printOutBuffers() {
		for (int i = 0; i < bufferSize; i++) {
			System.out.print(this + " " + i + ' ');
			for (int j = 0; j < outs; j++) {
				System.out.print(bufOut[j][i] + " ");
			}
			System.out.println();
		}
	}

	/**
	 * Determines whether this UGen has no UGens connected to its inputs.
	 *
	 * @return true if this UGen has no UGens connected to its inputs, false
	 * otherwise.
	 */
	public boolean noInputs() {
		return noInputs;
	}

	/**
	 * Called by the signal chain to update this UGen's ouput data. Subclassses of UGen should implement the UGen's DSP perform routine here. In
	 * general this involves grabbing data from {@link #bufIn} and putting data
	 * into {@link #bufOut} in some way. {@link #bufIn} and {@link #bufOut} are 2D arrays of floats of the form float[numChannels][bufferSize]. The length of the buffers is given by
	 * {@link #bufferSize}, and the number of channels of the input and output buffers are given by {@link #ins} and {@link #outs} respectively.
	 */
	protected abstract void gen(); /* Must be implemented by subclasses.*/

	/**
	 * Gets a specific specified value from the output buffer, with indices i (channel)
	 * and j (offset into buffer).
	 *
	 * @param i channel index.
	 * @param j buffer frame index.
	 * @return value of specified sample.
	 */
	public float getValue(int i, int j) {
		return bufOut[i][j];
	}

	/**
	 * Gets an entire output buffer from a specific channel at a given time step. Note that output buffers are swapped
	 * every time step, so this float[] will only be valid between the time this UGen in updated
	 * and the beginning of the next update cycle. It's your job to make sure you're calling this
	 * at a sensible time.
	 *
	 * @param i the output channel.
	 * @return a buffer of output data.
	 */
	public float[] getOutBuffer(int i) {
		return bufOut[i];
	}

	/**
	 * Gets the value of the buffer, assuming that the buffer only has one value. This is mainly a convenience method for use with {@link net.beadsproject.beads.ugens.Static Static} type UGens. It is equivalent to {@link #getValue(int, int)} with zero args.
	 *
	 * @return the value.
	 */
	public float getValue() {
		return getValue(0, 0);
	}

	/**
	 * Sets the value of {@link #bufOut}. This is mainly a convenience method for use with {@link net.beadsproject.beads.ugens.Static Static} and {@link Envelope Envelope} type UGens.
	 *
	 * @param value the new value.
	 */
	public void setValue(float value) {
	}

	/**
	 * Gets the value as a double. Only overridden by certain classes that generate info in doubles. Gets a specific specified value from the output buffer, with indices i (channel)
	 * and j (offset into buffer).
	 *
	 * @param i channel index.
	 * @param j buffer frame index.
	 * @return value of specified sample.
	 */
	public double getValueDouble(int i, int j) {
		return getValue(i, j);
	}

	/**
	 * Gets the value as a double. Only overridden by certain classes that generate info in doubles. Gets the value of the buffer, assuming that the buffer only has one value. This is mainly a convenience method for use with {@link net.beadsproject.beads.ugens.Static Static} type UGens. It is equivalent to {@link #getValue(int, int)} with zero args.
	 *
	 * @return the value.
	 */
	public double getValueDouble() {
		return getValue();
	}

	/**
	 * Checks if this UGen has been updated in the current timeStep.
	 *
	 * @return true if the UGen has been updated in the current timeStep.
	 */
	private boolean isUpdated() {
		return lastTimeStep == context.getTimeStep();
	}

	/**
	 * Pauses/un-pauses the current UGen. When paused, a UGen does not perform an audio calculations and does not respond to messages.
	 *
	 * @param paused is true if paused.
	 * @see Auvent#pause(boolean)
	 */
	@Override
	public void pause(boolean paused) {
		if (!isPaused() && paused) {
			setOutsToPause();
		}
		super.pause(paused);
	}

	/**
	 * Used to determine how a UGen sets its outputs up before calculateBuffer() is called.
	 */
	protected enum OutputInitializationRegime {
		ZERO, NULL, JUNK, RETAIN
	}

	protected enum OutputPauseRegime {ZERO, RETAIN, NULL}

//    public boolean isTimerMode() {
//        return timerMode;
//    }
//
//    public void setTimerMode(boolean timerMode) {
//        this.timerMode = timerMode;
//    }

	/**
	 * BufferPointer is a nested class used by UGens to keep track of the output buffers of other UGens connected to their inputs.
	 */
	static class BufferPointer {

		/**
		 * The UGen that owns the output buffer.
		 */
		final UGen ugen;

		/**
		 * The index of the output buffer.
		 */
		final int index;

		/**
		 * Instantiates a new buffer pointer.
		 *
		 * @param ugen  the ugen to point to.
		 * @param index the index of the output of that ugen.
		 */
		BufferPointer(UGen ugen, int index) {
			this.ugen = ugen;
			this.index = index;
		}

		/**
		 * Gets the buffer.
		 *
		 * @return the buffer.
		 */
		float[] getBuffer() {
			return ugen.bufOut[index];
		}

		/**
		 * Gets the value at the given sample offset into the buffer.
		 *
		 * @param point the sample offset.
		 * @return the value at the given sample offset.
		 */
		float get(int point) {
			return ugen.getValue(index, point);
		}

	}

}