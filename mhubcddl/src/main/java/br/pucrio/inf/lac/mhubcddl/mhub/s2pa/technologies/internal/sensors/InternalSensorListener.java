package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors;

import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;

public interface InternalSensorListener {

    public void onInternalSensorChanged(SensorDataExtended data);

}
