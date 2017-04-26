package br.pucrio.inf.lac.mhubcddl.cddl.message;


public class ServiceInformationMessage extends Message {

    private String serviceName;

    private long age;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public long getAge() {
        return age;
    }

    public void setAge(long age) {
        this.age = age;
    }

}
