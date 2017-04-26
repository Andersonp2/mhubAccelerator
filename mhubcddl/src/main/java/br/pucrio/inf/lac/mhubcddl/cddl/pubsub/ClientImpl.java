package br.pucrio.inf.lac.mhubcddl.cddl.pubsub;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import br.pucrio.inf.lac.mhubcddl.cddl.listeners.IClientQoSListener;
import br.pucrio.inf.lac.mhubcddl.cddl.message.LivelinessMessage;
import br.pucrio.inf.lac.mhubcddl.cddl.message.Message;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.DeadlineQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.DestinationOrderQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.DurabilityQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.History;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.HistoryQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LatencyBudgetQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LifespanQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.LivelinessQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.ReliabilityQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.qos.TimeBasedFilterQoS;
import br.pucrio.inf.lac.mhubcddl.cddl.services.ConnectionService;
import br.pucrio.inf.lac.mhubcddl.cddl.util.Time;

/**
 * Created by bertodetacio on 05/03/17.
 */
public abstract class ClientImpl extends Thread implements Client {

    private DeadlineQoS deadlineQoS = new DeadlineQoS();

    private ReliabilityQoS reliabilityQoS = new ReliabilityQoS();

    private DurabilityQoS durabilityQoS = new DurabilityQoS();

    private HistoryQoS historyQoS = new HistoryQoS();

    private DestinationOrderQoS destinationOrderQoS = new DestinationOrderQoS();

    private History history = new History(historyQoS, destinationOrderQoS);

    private TimeBasedFilterQoS timeBasedFilterQoS = new TimeBasedFilterQoS();

    private LatencyBudgetQoS latencyBudgetQoS = new LatencyBudgetQoS();

    private LifespanQoS lifespanQoS = new LifespanQoS();

    private LivelinessQoS livelinessQoS = new LivelinessQoS();

    private DeadlineTimerTask deadlineTimerTask = null;

    private TimeBasedFilterTimerTask timeBasedFilterTimerTask = null;

    private LatencyBudgetTimerTask latencyBudgetTimerTask = null;

    private LivelinessTimerTask livelinessTimerTask = null;

    private Vector<LifespanTimerTask> lifespanTimerTasks = new Vector<LifespanTimerTask>();

    private Message lastDeadlineMessage = null;

    private LivelinessMessage lastLivelinessMessage = null;

    private Message lastMessage = null;

    private ConcurrentHashMap<String, Message> timeBasedFilterMessagesMap = new ConcurrentHashMap<String, Message>();

    protected Vector<Message> messageQueue = new Vector<Message>();

    protected Timer timer = new Timer();

    protected Time time = Time.getInstance();

    protected ConnectionService connectionService = ConnectionService.getInstance();
    protected final String clientId = connectionService.getClientID();

    protected IClientQoSListener clientQoSListener;

    public ClientImpl() {
        this.start();
    }

    public ClientImpl(IClientQoSListener clientQoSListener) {
        this.clientQoSListener = clientQoSListener;
        this.start();
    }

    protected abstract void on_message_received(Message message);

    protected abstract void on_latency_budget_timer_finish();

    protected abstract boolean isDeadlineMissed(long deadline);

    protected abstract boolean isLivelinessMissed(long leaseDuration);

    public void addToQueue(Message message) {
        messageQueue.add(message);
        putToTimeBasedFilterMessagesMap(message);
        unlock();
    }

    @Override
    public void run() {
        while (true) {
            while (messageQueue.isEmpty()) {
                waiting();
            }
            removeMessagesFromQueue();
        }
    }

     public DurabilityQoS getDurabilityQoS() {
        return durabilityQoS;
    }

    public void setDurabilityQoS(DurabilityQoS durabilityQoS) {
        this.durabilityQoS = durabilityQoS;
    }

    public HistoryQoS getHistoryQoS() {
        return historyQoS;
    }

    public void setHistoryQoS(HistoryQoS historyQoS) {
        this.historyQoS = historyQoS;
        this.history.setHistoryQoS(historyQoS);
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public ReliabilityQoS getReliabilityQoS() {
        return reliabilityQoS;
    }

    @Override
    public void setReliabilityQoS(ReliabilityQoS reability) {
        this.reliabilityQoS = reability;
    }

    public DeadlineQoS getDeadlineQoS() {
        return deadlineQoS;
    }

    public void setDeadlineQoS(DeadlineQoS deadlineQoS) {

        long period = deadlineQoS.getPeriod();

        if (this.deadlineQoS.getPeriod() != period) {
            this.deadlineQoS = deadlineQoS;

            if (period == DeadlineQoS.DEFAULT_DEADLINE && deadlineTimerTask != null) {
                deadlineTimerTask.cancel();
            } else if (period > DeadlineQoS.DEFAULT_DEADLINE) {
                if (deadlineTimerTask != null) {
                    deadlineTimerTask.cancel();
                }
                deadlineTimerTask = new DeadlineTimerTask(time.getCurrentTimestamp() + deadlineQoS.getPeriod());
                timer.scheduleAtFixedRate(deadlineTimerTask, period, period);
            }

        }
    }

    public TimeBasedFilterQoS getTimeBasedFilterQos() {
        return timeBasedFilterQoS;
    }

    public void setTimeBasedFilterQoS(TimeBasedFilterQoS timeBasedFilterQoS) {

        long minSeparation = timeBasedFilterQoS.getMinSeparation();

        if (this.timeBasedFilterQoS.getMinSeparation() != minSeparation) {
            this.timeBasedFilterQoS = timeBasedFilterQoS;

            if (minSeparation == TimeBasedFilterQoS.DEFAULT_MIN_SEPARATION_INTERVAL && timeBasedFilterTimerTask != null) {
                timeBasedFilterTimerTask.cancel();
            } else if (minSeparation > TimeBasedFilterQoS.DEFAULT_MIN_SEPARATION_INTERVAL) {
                if (timeBasedFilterTimerTask != null) {
                    timeBasedFilterTimerTask.cancel();
                }
                timeBasedFilterTimerTask = new TimeBasedFilterTimerTask();
                timer.scheduleAtFixedRate(timeBasedFilterTimerTask, minSeparation, minSeparation);
            }

        }

    }

    public LatencyBudgetQoS getLatencyBudgetQoS() {
        return latencyBudgetQoS;
    }

    public void setLatencyBudgetQoS(LatencyBudgetQoS latencyBudgetQoS) {

        long delay = latencyBudgetQoS.getDelay();

        if (this.latencyBudgetQoS.getDelay() != delay) {
            this.latencyBudgetQoS = latencyBudgetQoS;

            if (delay == LatencyBudgetQoS.DEFAULT_DELAY && latencyBudgetTimerTask != null) {
                latencyBudgetTimerTask.cancel();
            } else if (delay > LatencyBudgetQoS.DEFAULT_DELAY) {
                if (latencyBudgetTimerTask != null) {
                    latencyBudgetTimerTask.cancel();
                }
                latencyBudgetTimerTask = new LatencyBudgetTimerTask();
                timer.scheduleAtFixedRate(latencyBudgetTimerTask, delay, delay);
            }

        }
    }

    public LifespanQoS getLifespanQoS() {
        return lifespanQoS;
    }

    public void setLifespanQoS(LifespanQoS lifespanQoS) {

        long expirationTime = lifespanQoS.getExpirationTime();

        if (this.lifespanQoS.getExpirationTime() != expirationTime) {
            this.lifespanQoS = lifespanQoS;

            if (expirationTime == LifespanQoS.INFINITE_DURATION && !lifespanTimerTasks.isEmpty()) {
                for (LifespanTimerTask lifespanTimerTask : lifespanTimerTasks) {
                    lifespanTimerTask.cancel();
                }
            }
        }
    }


    public LivelinessQoS getLivelinessQoS() {
        return livelinessQoS;
    }

    @Override
    public void setLivelinessQoS(LivelinessQoS livelinessQoS) {

        if (this.livelinessQoS.getKind() == LivelinessQoS.MANUAL && livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getLeaseDuration() > LivelinessQoS.DEFAULT_LEASE_DURANTION ) {
            livelinessTimerTask = new LivelinessTimerTask(time.getCurrentTimestamp() + livelinessQoS.getLeaseDuration());
            timer.scheduleAtFixedRate(livelinessTimerTask, livelinessQoS.getLeaseDuration(), livelinessQoS.getLeaseDuration());
        }

        if (this.livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getKind() == LivelinessQoS.MANUAL && livelinessTimerTask != null) {
            livelinessTimerTask.cancel();
        } else if (this.livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && livelinessQoS.getKind() == LivelinessQoS.AUTOMATIC && this.livelinessQoS.getLeaseDuration() != livelinessQoS.getLeaseDuration() && livelinessTimerTask != null) {
            livelinessTimerTask.cancel();
            if (livelinessQoS.getLeaseDuration() > LivelinessQoS.DEFAULT_LEASE_DURANTION) {
                livelinessTimerTask = new LivelinessTimerTask(time.getCurrentTimestamp() + livelinessQoS.getLeaseDuration());
                timer.scheduleAtFixedRate(livelinessTimerTask, livelinessQoS.getLeaseDuration(), livelinessQoS.getLeaseDuration());
            }
        }

        this.livelinessQoS = livelinessQoS;

    }

    public DestinationOrderQoS getDestinationOrderQoS() {
        return destinationOrderQoS;
    }


    public void setDestinationOrderQoS(DestinationOrderQoS destinationOrderQoS) {
        this.destinationOrderQoS = destinationOrderQoS;
        this.history.setDestinationOrderQoS(destinationOrderQoS);
    }


   protected void putToTimeBasedFilterMessagesMap(Message message) {
        String topic = message.getTopic();
        if (!timeBasedFilterMessagesMap.containsKey(topic)) {
            timeBasedFilterMessagesMap.put(topic, message);
        }
    }


    protected synchronized void waiting() {
        try {
            wait();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected synchronized void unlock() {
        notifyAll();
    }

    public Message getLastDeadlineMessage() {
        return lastDeadlineMessage;
    }

    public void setLastDeadlineMessage(
            Message lastMessagePublished) {
        this.lastDeadlineMessage = lastMessagePublished;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LivelinessMessage getLastLivelinessMessage() {
        return lastLivelinessMessage;
    }

    public void setLastLivelinessMessage(LivelinessMessage lastLivelinessMessage) {
        this.lastLivelinessMessage = lastLivelinessMessage;
    }


    protected void startLisfespanClock(Message message, long lifeTime) {
        if (lifeTime >= 0) {
            LifespanTimerTask lifespanTimerTask = new LifespanTimerTask(message);
            lifespanTimerTasks.add(lifespanTimerTask);
            timer.schedule(lifespanTimerTask, lifeTime);
        }
    }


    private void removeMessagesFromQueue() {
        Message message = messageQueue.remove(0);
        if (message != null && timeBasedFilterQoS.getMinSeparation() == TimeBasedFilterQoS.DEFAULT_MIN_SEPARATION_INTERVAL) {
            on_message_received(message);
        }
    }


    protected void on_deadline_missed() {
        if (clientQoSListener != null) {
            clientQoSListener.onExpectedDeadlineMissed();
        }
    }


    protected void on_deadline_fulfilled() {
        if (clientQoSListener != null) {
            clientQoSListener.onExpectedDeadlineFulfilled();
        }
    }


    protected void on_liveliness_missed() {
        if (clientQoSListener != null) {
            clientQoSListener.onExpectedLivelinessMissed();
        }
    }


    protected void on_liveliness_fulfilled() {
        if (clientQoSListener != null) {
            clientQoSListener.onExpectedLivelinessFulfilled();
        }
    }



    protected void on_lifespan_message_expired(Message message) {
        if (clientQoSListener != null) {
            clientQoSListener.onLifespanExpired(message);
        }
    }


    public long getLifeTime(Message message, boolean publisher) {

        LifespanQoS lifespanQoS = getLifespanQoS();
        long expirationTime = 0;
        long lifeTime = 0;

        if (lifespanQoS.getExpirationTime() != LifespanQoS.INFINITE_DURATION) {
            if (lifespanQoS.getExpirationTime() <= LifespanQoS.EXPIRATION_TIME_FROM_MESSAGE) {
                expirationTime = message.getExpirationTime();
            } else {
                expirationTime = lifespanQoS.getExpirationTime();
            }
            if (lifespanQoS.getTimestampKind() != LifespanQoS.RECEPTION_TIMESTAMP_KIND) {
                long timestamp = 0;
                if (lifespanQoS.getTimestampKind() == LifespanQoS.MENSUREMENT_TIMESTAMP_KIND) {
                    timestamp = message.getMeasurementTime();
                } else if (lifespanQoS.getTimestampKind() == LifespanQoS.PUBLICATION_TIMESTAMP_KIND) {
                    if (publisher) {
                        lifeTime = expirationTime;
                        return lifeTime;
                    } else {
                        timestamp = message.getPublicationTimestamp();
                    }
                }
                long currentTimestamp = time.getCurrentTimestamp();
                long differenceTime = currentTimestamp - timestamp;
                /*System.out.println("expiration time = "+expirationTime);
				System.out.println("base timestamp = "+timestamp);
				System.out.println("current timestamp = "+currentTimestamp);
				System.out.println("diference time = "+differenceTime);*/
                if (differenceTime >= 0) {
                    lifeTime = expirationTime - differenceTime;
                    if (lifeTime <= 0) {
                        //descomentar se quiser ser notificado de mensagens que expiraram antes de serem inseridas
                        //no cache do subscritor, ou seja, chegaram mortas
                        on_lifespan_message_expired(message);
                    }
                } else {
                    //indica que o recebedor está com relógio atrasado em relação ao publicador
                    //duas opções: considerar o tempo da recepção (default) ou considerar a mensagem como expirada
                    if (lifespanQoS.isUseReceptionTimeForEarlyClocks()) {
                        lifeTime = expirationTime;
                    } else {
                        lifeTime = -1;
                    }
                }
            } else {
                lifeTime = expirationTime;

            }
        }
		

        return lifeTime;
    }

    public void setClientQoSListener(IClientQoSListener clientQoSListener) {
        this.clientQoSListener = clientQoSListener;
    }

    @Override
    public abstract void send(Message message, String topic);

    private class DeadlineTimerTask extends TimerTask {

        private long deadline;

        public DeadlineTimerTask(long deadline) {
            super();
            this.deadline = deadline;
        }

        @Override
        public void run() {
            if (isDeadlineMissed(deadline)) {
                on_deadline_missed();
            } else {
                on_deadline_fulfilled();
            }
            deadline = deadline + deadlineQoS.getPeriod();
        }

    }

    private class TimeBasedFilterTimerTask extends TimerTask {
        @Override
        public void run() {
            Collection<Message> currentMessages = timeBasedFilterMessagesMap.values();
            for (Message message : currentMessages) {
                on_message_received(message);
                currentMessages.remove(message);
            }
        }
    }


    private class LatencyBudgetTimerTask extends TimerTask {
        @Override
        public void run() {
            on_latency_budget_timer_finish();
        }
    }

    private class LifespanTimerTask extends TimerTask {

        private Message message;
        private LifespanQoS lifespanQoS = getLifespanQoS();
        private ReliabilityQoS reliabilityQoS = getReliabilityQoS();

        public LifespanTimerTask(Message message) {
            super();
            this.message = message;
        }

        @Override
        public void run() {
            if (lifespanQoS.getExpirationTime() != LifespanQoS.INFINITE_DURATION) {
                History history = getHistory();
                history.remove(message);
                on_lifespan_message_expired(message);
            }
        }
    }


    private class LivelinessTimerTask extends TimerTask {

        private long leaseDuration;

        public LivelinessTimerTask(long leaseDuration) {
            super();
            this.leaseDuration = leaseDuration;
        }

        @Override
        public void run() {

            if (isLivelinessMissed(leaseDuration)) {
                on_liveliness_missed();
            } else {
                on_liveliness_fulfilled();
            }
            leaseDuration = leaseDuration + livelinessQoS.getLeaseDuration();
        }

    }


}
