package com.testvagrant.mdb.builders;

import com.testvagrant.commons.entities.DeviceDetails;
import com.testvagrant.commons.entities.device.DeviceType;
import com.testvagrant.commons.entities.device.OSVersion;
import com.testvagrant.commons.entities.device.Platform;
import com.testvagrant.commons.entities.device.Status;

public class DeviceDetailsBuilder {

    private DeviceDetails deviceDetails;

    public DeviceDetailsBuilder() {
        deviceDetails = new DeviceDetails();
    }

    public DeviceDetailsBuilder withDeviceUdid(String deviceUdid) {
        deviceDetails.setDeviceUdid(deviceUdid);
        return this;
    }

    public DeviceDetailsBuilder withPlatform(Platform platform){
        deviceDetails.setPlatform(platform);
        return this;
    }

    public DeviceDetailsBuilder withOSVersion(OSVersion osVersion) {
        deviceDetails.setOsVersion(osVersion);
        return this;
    }

    public DeviceDetailsBuilder withDeviceType(DeviceType deviceType) {
        deviceDetails.setDeviceType(deviceType);
        return this;
    }

    public DeviceDetailsBuilder withStatus(Status status) {
        deviceDetails.setStatus(status);
        return this;
    }

    public DeviceDetailsBuilder withDeviceName(String deviceName) {
        deviceDetails.setDeviceName(deviceName);
        return this;
    }

    public DeviceDetails build() {
        return deviceDetails;
    }


}
