package anderson.com.mhubaccelerometer.controller;

import android.content.Context;

import java.util.ArrayList;

import anderson.com.mhubaccelerometer.domain.ServiceItem;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Publisher;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Subscriber;
import br.pucrio.inf.lac.mhubcddl.cddl.services.ConnectionService;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Preferences;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Provider;

/**
 * Created by lcmuniz on 26/02/17.
 */

public class AppController {

    private final String clientId = "lcmuniz@gmail.com";
    private final Publisher publisher;
    private final Subscriber subscriber;
    private final ArrayList<ServiceItem> services = new ArrayList<>();

    public AppController(Context context) {

        final Preferences preferences = new Preferences(context, Preferences.PREFERENCE_FILE_KEY);
        preferences.put(Preferences.CLIENT_ID_KEY, clientId);

        final ConnectionService connectionService = ConnectionService.getInstance();
        connectionService.setClientID(clientId);
        connectionService.connect();

        publisher = Provider.newPublisher();

        subscriber = Provider.newSubscriber();

    }

    public Publisher getPublisher() {
        return publisher;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public ArrayList<ServiceItem> getServices() {
        return services;
    }

}
