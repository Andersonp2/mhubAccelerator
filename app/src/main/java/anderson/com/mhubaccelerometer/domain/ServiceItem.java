package anderson.com.mhubaccelerometer.domain;

/**
 * Created by lcmuniz on 29/12/16.
 */

public class ServiceItem {

    public String publisher_id;
    public String serviceName;
    public boolean selected;

    @Override
    public boolean equals(Object o) {
        final ServiceItem obj = (ServiceItem) o;
        return (obj.serviceName.equals(this.serviceName)) && (obj.publisher_id.equals(this.publisher_id));
    }
}
