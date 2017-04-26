/**
 *
 */
package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.bt;

import android.util.Log;

import java.util.List;

import br.pucrio.inf.lac.mhubcddl.mhub.components.MOUUID;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;
import br.pucrio.inf.lac.mhubcddl.mhub.s2pa.base.TechnologyListener;

/**
 * @author bertodetacio
 */
public class BTTechnologyListener implements TechnologyListener {

    /**
     *
     */
    public BTTechnologyListener() {
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onMObjectFound(MOUUID mobileObject, Double rssi) {
        Log.i("Log", "Mobile Object " + mobileObject.toString() + " Found. RSSI: " + rssi + ".");

    }

    @Override
    public void onMObjectConnected(MOUUID mobileObject) {
        Log.i("Log", "Mobile Object " + mobileObject.toString() + " Connnected.");

    }

    @Override
    public void onMObjectDisconnected(MOUUID mobileObject, List<String> services) {
        Log.i("Log", "Mobile Object " + mobileObject.toString() + " Disconnnected.");

    }

    @Override
    public void onMObjectServicesDiscovered(MOUUID mobileObject,
                                            List<String> services) {
        for (String service : services) {
            //Log.i("Log", "Discovered Service: "+service+" from: "+mobileObject.toString());
        }

    }

    @Override
    public void onMObjectValueRead(MOUUID mobileObject, Double rssi,
                                   String serviceName, Double[] values) {
        //Log.i("Log", "Read "+serviceName+ " = "+values[0]+" MObj: "+mobileObject.toString()+ ".\nDate: "+getTime()+"\n\n");
    }


    @Override
    public void onMObjectValueRead(MOUUID mobileObject, Double rssi,
                                   String serviceName, SensorDataExtended value) {
        //Log.i("Log", "Read "+serviceName+ " = "+values[0]+" MObj: "+mobileObject.toString()+ ".\nDate: "+getTime()+"\n\n");
    }

}
