package br.pucrio.inf.lac.mhubcddl.mhub.s2pa.technologies.internal.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import net.sf.cglib.core.Local;

import javax.xml.transform.Source;

import br.pucrio.inf.lac.mhubcddl.mhub.components.AppConfig;
import br.pucrio.inf.lac.mhubcddl.mhub.components.AppUtils;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SensorDataExtended;
import br.pucrio.inf.lac.mhubcddl.mhub.models.locals.SourceLocation;

/*
     * Location sensor. Uses GPS for location when available. Otherwise, uses network provider
     */
public class LocationSensor implements LocationListener, InternalSensor {

    public static final String NAME = "Location";

    /**
     * DEBUG
     */
    private static final String TAG = LocationSensor.class.getSimpleName();

    private InternalSensorListener listener;  // Listener pointing to SensorPhone class
    private Context ac;

    /**
     * Last location saved
     */
    private Location lastRegisteredLocation;

    /**
     * The two providers that we care
     */
    private String gpsProvider;
    private String networkProvider;

    /**
     * The location manager
     */
    private LocationManager lm;

    /**
     * Current location update interval
     */
    private Integer currentInterval;

    /**
     * GPS rate, since it consumes more battery than network
     */
    private static final int GPS_RATE = 4;

    /**
     * Time difference threshold set for two minutes
     */
    private static final int TIME_DIFFERENCE_THRESHOLD = 1 * 60 * 1000;

    private final String GPS_PROVIDER_NAME = "Location (GPS)";
    private final String NETWORK_PROVIDER_NAME = "Location (Net)";

    public LocationSensor(Context context) {
        this.ac = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        // Wait until we get a good enough location
        if (isBetterLocation(lastRegisteredLocation, location)) {
            AppUtils.logger('i', TAG, ">> NEW_LOCATION_SENT");

            SensorDataExtended data = new SensorDataExtended();

            data.setSensorName(location.getProvider().equals(
                    LocationManager.GPS_PROVIDER) ? GPS_PROVIDER_NAME
                    : NETWORK_PROVIDER_NAME);


            SourceLocation sourceLocation = new SourceLocation();
            sourceLocation.setLatitude(location.getLatitude());
            sourceLocation.setLongitude(location.getLongitude());
            sourceLocation.setAltitude(location.getAltitude());

            Double[] values = {location.getLatitude(), location.getLongitude(), location.getAltitude()};

            data.setSensorObjectValue(values);

            data.setAccuracy(location.getAccuracy());
            data.setMeasurementTime(System.currentTimeMillis());
            data.setAvailableAttributes(values.length);
            data.setSourceLocation(sourceLocation);

            listener.onInternalSensorChanged(data);

            // save the location as last registered
            lastRegisteredLocation = location;

        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        String statusAsString = "Available";
        if (status == LocationProvider.OUT_OF_SERVICE)
            statusAsString = "Out of service";
        else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            statusAsString = "Temporarily Unavailable";

        // Log any information about the status of the providers
        AppUtils.logger('i', TAG, provider + " provider status has changed: [" + statusAsString + "]");
    }

    @Override
    public void onProviderEnabled(String provider) {
        AppUtils.logger('i', TAG, "provider enabled: " + provider);

        // If it's a provider we care about, we start listening
        if (provider.equals(LocationManager.GPS_PROVIDER)) {
            gpsProvider = LocationManager.GPS_PROVIDER;


            if (ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            lm.requestLocationUpdates(gpsProvider,
                    currentInterval * GPS_RATE,
                    AppConfig.DEFAULT_LOCATION_MIN_DISTANCE,
                    this);
        } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
            networkProvider = LocationManager.NETWORK_PROVIDER;
            lm.requestLocationUpdates(networkProvider,
                    currentInterval,
                    AppConfig.DEFAULT_LOCATION_MIN_DISTANCE,
                    this);
        }

    }

    @Override
    public void onProviderDisabled(String provider) {
        AppUtils.logger('i', TAG, "provider disabled: " + provider);

        // If it's a provider we care about, we set it as null
        if (provider.equals(LocationManager.GPS_PROVIDER))
            gpsProvider = null;
        else if (provider.equals(LocationManager.NETWORK_PROVIDER))
            networkProvider = null;
    }

    /**
     * Decide if new location is better than older by following some basic criteria.
     *
     * @param oldLocation Old location used for comparison.
     * @param newLocation Newly acquired location compared to old one.
     * @return If new location is more accurate and suits your criteria more than the old one.
     */
    private boolean isBetterLocation(Location oldLocation, Location newLocation) {
        // If there is no old location, the new location is better
        if (oldLocation == null)
            return true;

        // Check if new location is newer in time
        long timeDelta = newLocation.getTime() - oldLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DIFFERENCE_THRESHOLD;
        boolean isSignificantlyOlder = timeDelta < -TIME_DIFFERENCE_THRESHOLD;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer)
            return true;
            // If the new location is more than two minutes older, it must be worse
        else if (isSignificantlyOlder)
            return false;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (newLocation.getAccuracy() - oldLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), oldLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate)
            return true;
        else if (isNewer && !isLessAccurate)
            return true;
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider)
            return true;

        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null)
            return provider2 == null;
        return provider1.equals(provider2);
    }

    @Override
    public void start() {
        // get location manager
        lm = (LocationManager) ac.getSystemService(ac.LOCATION_SERVICE);
        // Configurations
        bootstrap();
    }

    @Override
    public void stop() {
        if (lm != null)
            if (ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        lm.removeUpdates(this);
    }

    @Override
    public void setListener(InternalSensorListener listener) {
        this.listener = listener;
    }

    @Override
    public String getName() {
        return NAME;
    }


    /**
     * The bootstrap for the location service
     */
    private void bootstrap() {
        // check for the current value
        currentInterval = AppUtils.getCurrentLocationInterval(ac);
        if (currentInterval == null) // if null get the default
            currentInterval = AppConfig.DEFAULT_LOCATION_INTERVAL_HIGH;
        // save the current location interval to SPREF
        AppUtils.saveCurrentLocationInterval(ac, currentInterval);

        // Start listening location updated from gps and network, if enabled
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            gpsProvider = LocationManager.GPS_PROVIDER;

            if (ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ac, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            lm.requestLocationUpdates(gpsProvider,
                    currentInterval * GPS_RATE,
                    AppConfig.DEFAULT_LOCATION_MIN_DISTANCE,
                    this);

            AppUtils.logger('i', TAG, "GPS Location provider has been started");
        }

        // 4x faster refreshing rate since this provider doesn't consume much battery.
        if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            networkProvider = LocationManager.NETWORK_PROVIDER;
            lm.requestLocationUpdates(networkProvider,
                    currentInterval,
                    AppConfig.DEFAULT_LOCATION_MIN_DISTANCE,
                    this);

            AppUtils.logger('i', TAG, "NETWORK Location provider has been started");
        }

        if (gpsProvider == null && networkProvider == null)
            AppUtils.logger('e', TAG, "No providers available");

        // set all the default values for the options HIGH, MEDIUM and LOW
        if (AppUtils.getLocationInterval(ac, AppConfig.SPREF_LOCATION_INTERVAL_HIGH) == null)
            AppUtils.saveLocationInterval(ac,
                    AppConfig.DEFAULT_LOCATION_INTERVAL_HIGH,
                    AppConfig.SPREF_LOCATION_INTERVAL_HIGH);

        if (AppUtils.getLocationInterval(ac, AppConfig.SPREF_LOCATION_INTERVAL_MEDIUM) == null)
            AppUtils.saveLocationInterval(ac,
                    AppConfig.DEFAULT_LOCATION_INTERVAL_MEDIUM,
                    AppConfig.SPREF_LOCATION_INTERVAL_MEDIUM);

        if (AppUtils.getLocationInterval(ac, AppConfig.SPREF_LOCATION_INTERVAL_LOW) == null)
            AppUtils.saveLocationInterval(ac,
                    AppConfig.DEFAULT_LOCATION_INTERVAL_LOW,
                    AppConfig.SPREF_LOCATION_INTERVAL_LOW);
    }

}
