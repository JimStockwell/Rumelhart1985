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
    double f(double netpj, double threshold);
    double slope(double netpj, double threshold);
}

