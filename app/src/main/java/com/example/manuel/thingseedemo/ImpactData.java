package com.example.manuel.thingseedemo;

/**
 * Created by manuel on 2/19/18.
 */

public class ImpactData extends DataWithTime {

    double pressure;

    public double getImpact() {
        return pressure;
    }

    public void setImpact(double impact) {
        this.pressure = impact;
    }


    @Override
    public DataWithTime interpolate(Object that, double time) {
        if (that instanceof ImpactData) {
            ImpactData thatImpact = (ImpactData) that;
            ImpactData result = new ImpactData();
            double factor = time / (thatImpact.getTime() - this.getTime());

            result.setImpact(this.getImpact() + factor * (thatImpact.getImpact() - this.getImpact()));

            return result;
        }
        return null;
    }
}
