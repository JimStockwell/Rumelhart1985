package dev.jimstockwell.rumelhart1985;

import static java.lang.Math.exp;

class LogisticActivationFunction implements ActivationFunction
{
    @Override
    public double f(double netpj, double threshold)
    {
        return 1/(1+exp(-(netpj+threshold)));
    }

/*
    // Not used for anything,
    // but possibly good to know.
    public double slope(double netpj, double threshold)
    {
        return f(netpj, threshold)*(1-f(netpj, threshold));
    }
*/

    @Override
    public double slopeForOutput(double output)
    {
        if(output < 0 || output > 1)
            throw new IllegalArgumentException(
                "output must be between 0 and 1 inclusive, but was "+output);

        return output*(1-output);
    }
}

