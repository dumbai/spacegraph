package com.jujutsu.tsne.barneshut;

public interface TSneConfiguration {

	int getOutputDims();

	void setOutputDims(int n);


	boolean silent();

	void setSilent(boolean silent);

	boolean printError();

	void setPrintError(boolean print_error);

}