package br.pucrio.inf.lac.mhubcddl.cddl.util;

import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.CDDLFilter;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.CDDLFilterImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Client;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Filter;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.FilterImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Monitor;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.MonitorImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Publisher;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.PublisherImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.Subscriber;
import br.pucrio.inf.lac.mhubcddl.cddl.pubsub.SubscriberImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.services.LocalDirectory;
import br.pucrio.inf.lac.mhubcddl.cddl.services.LocalDirectoryImpl;
import br.pucrio.inf.lac.mhubcddl.cddl.services.QoCEvaluator;
import br.pucrio.inf.lac.mhubcddl.cddl.services.QoCEvaluatorImpl;

/**
 * Created by lcmuniz on 05/03/17.
 */
public class Provider {

    private static LocalDirectory localDirectory;
    private static QoCEvaluator qocEvaluator;

    public static LocalDirectory getLocalDirectory() {
        if (localDirectory == null) {
            localDirectory = new LocalDirectoryImpl();
        }
        return localDirectory;
    }

    public static QoCEvaluator getQoCEvaluator() {
        if (qocEvaluator == null) {
            qocEvaluator= new QoCEvaluatorImpl();
        }
        return qocEvaluator;
    }

    public static Publisher newPublisher() {
        return new PublisherImpl();
    }

    public static Subscriber newSubscriber() {
        return new SubscriberImpl();
    }

    public static CDDLFilter newCDDLFilter(String eplFilter) {
        return new CDDLFilterImpl(eplFilter);
    }

    public static Filter newFilter(Client subscriber) {
        return new FilterImpl(subscriber);
    }

    public static Monitor newMonitor() {
        return new MonitorImpl();
    }
}
