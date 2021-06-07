package dev.jimstockwell.rumelhart1985;

//
// This doesn't really work that well right now.
//
// In general, yes, slope is a function of netpj,
// but in important special cases (out case!),
// it is also an easy function of o_pj.
//
interface ActivationFunction
{
    /**
     * Calculates the output of a node.
     *
     * @param netpj     the net input to the node
     * @param threshold an offset to netpj
     * @return          the calculated node output 
     */
    double f(double netpj, double threshold);

    /**
     * Returns the derivitive at the output of a node.
     * Specifically,
     * the derivitive of the output
     * with respect to the input (netpj),
     * but, calculated based on the output level.
     *
     * @param output    the node output
     * @return          the derivitive
     */
    double slopeForOutput(double output);
}

