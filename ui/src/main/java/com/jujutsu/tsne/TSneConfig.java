package com.jujutsu.tsne;

import com.jujutsu.tsne.barneshut.TSneConfiguration;

public class TSneConfig implements TSneConfiguration {

    private int outputDims;
    private boolean silent;
    private boolean print_error;

    public TSneConfig(int outputDims, boolean silent, boolean print_error) {
        this.outputDims = outputDims;
        this.silent = silent;
        this.print_error = print_error;
    }


    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#getOutputDims()
     */
    @Override
    public int getOutputDims() {
        return outputDims;
    }

    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#setOutputDims(int)
     */
    @Override
    public void setOutputDims(int n) {
        this.outputDims = n;
    }

//	/* (non-Javadoc)
//	 * @see com.jujutsu.tsne.barneshut.TSneConfiguration#getInitialDims()
//	 */
//	@Override
//	public int getInitialDims() {
//		return pca_dims;
//	}
//
//	/* (non-Javadoc)
//	 * @see com.jujutsu.tsne.barneshut.TSneConfiguration#setInitialDims(int)
//	 */
//	@Override
//	public void setInitialDims(int initial_dims) {
//		this.pca_dims = initial_dims;
//	}


    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#silent()
     */
    @Override
    public boolean silent() {
        return silent;
    }

    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#setSilent(boolean)
     */
    @Override
    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#printError()
     */
    @Override
    public boolean printError() {
        return print_error;
    }

    /* (non-Javadoc)
     * @see com.jujutsu.tsne.barneshut.TSneConfiguration#setPrintError(boolean)
     */
    @Override
    public void setPrintError(boolean print_error) {
        this.print_error = print_error;
    }


}