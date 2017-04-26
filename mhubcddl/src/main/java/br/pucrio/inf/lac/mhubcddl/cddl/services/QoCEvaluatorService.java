package br.pucrio.inf.lac.mhubcddl.cddl.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class QoCEvaluatorService extends Service  {

    private static final String TAG = QoCEvaluatorService.class.getSimpleName();

    private QoCEvaluator qocEvaluator;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        qocEvaluator = Provider.getQoCEvaluator();

        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
