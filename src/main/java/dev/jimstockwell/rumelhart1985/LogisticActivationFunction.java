package dev.jimstockwell.rumelhart1985;

import static java.lang.Math.exp;

class LogisticActivationFunction implements ActivationFunction
{
    @Override
    public double f(double netpj, double threshold)
    {
        return 1/(1+exp(-(netpj+threshold)));
    }

    @Override
    public double slope(double netpj, double threshold)
    {
        return f(netpj, threshold)*(1-f(netpj, threshold));
    }
}

