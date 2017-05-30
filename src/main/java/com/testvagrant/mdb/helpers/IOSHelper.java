package com.testvagrant.mdb.helpers;


import com.testvagrant.mdb.builders.DeviceDetailsBuilder;
import com.testvagrant.mdb.core.CommandExecutor;
import com.testvagrant.mdb.core.Mobile;
import com.testvagrant.mdb.entities.DeviceDetails;
import com.testvagrant.mdb.enums.DeviceType;
import com.testvagrant.mdb.enums.Platform;
import com.testvagrant.mdb.enums.Status;
import com.testvagrant.mdb.enums.XCodeVersion;

import java.util.List;

import static com.testvagrant.mdb.utils.Commands.Instruments.*;
import static com.testvagrant.mdb.utils.Commands.Instruments.DEVICE_UDID_PATTERN;
import static com.testvagrant.mdb.utils.Commands.Instruments.SIMULATOR_UDID_PATTERN;

public class IOSHelper {

    private List<DeviceDetails> deviceDetails;
    private CommandExecutor commandExecutor;

    public IOSHelper(List<DeviceDetails> deviceDetails) {
        this.deviceDetails = deviceDetails;
        this.commandExecutor = new CommandExecutor();
    }


    public void initSimulators(List<String> processLog) {
        for (String process : processLog) {
            if (isSimulator(process)) {
                DeviceDetails simulator = getSimulator(process);
                deviceDetails.add(simulator);
            }
        }
    }

    public void initIDevices(List<String> processlog) {
        for (String process : processlog) {
            if (isIDevice(process)) {
                if (process.contains("(")) {
                    DeviceDetails device = getDevice(process);
                    deviceDetails.add(device);
                }
            }
        }

    }

    private DeviceDetails getSimulator(String line) {
        String udid = getUDID(line);
        String iosVersion = getIOSVersion(line);
        String deviceName = getDeviceName(line);
        return new DeviceDetailsBuilder()
                .withDeviceName(deviceName)
                .withOSVersion(Mobile.getOSVersion(Platform.IOS,iosVersion))
                .withDeviceUdid(udid)
                .withPlatform(Platform.IOS)
                .withDeviceType(DeviceType.SIMULATOR)
                .withStatus(Status.AVAILABLE)
                .build();
    }

    private DeviceDetails getDevice(String line) {
        String udid = getUDID(line);
        String iosVersion = getIOSVersion(line);
        String deviceName = getDeviceName(line);
        return new DeviceDetailsBuilder()
                .withDeviceName(deviceName)
                .withOSVersion(Mobile.getOSVersion(Platform.IOS,iosVersion))
                .withDeviceUdid(udid)
                .withPlatform(Platform.IOS)
                .withDeviceType(DeviceType.DEVICE)
                .build();
    }

    private String getUDID(String line) {
        String UDID;
        int udidStart = line.indexOf("[") + 1;
        int udidEnd = line.indexOf("]");
        UDID = line.substring(udidStart, udidEnd);
        return UDID;
    }

    private String getIOSVersion(String line) {
        String iosVersionString = line.replace("(Simulator)", "").trim();
        int iosStartingValue = iosVersionString.indexOf("(");
        int iosLastValue = iosVersionString.indexOf(")");
        return iosVersionString.substring(iosStartingValue + 1, iosLastValue);
    }

    private String getDeviceName(String line) {
        int deviceNameLastValue = line.indexOf("(");
        return line.substring(0, deviceNameLastValue).trim();
    }

    private String getXcodeVersion(String xcodeVersion) {
        List<String> xCodeInstallationDetails = commandExecutor.exec(XCODE_INSTALLATION_DETAILS).asList();
        for (String xCodeDetail : xCodeInstallationDetails) {
            if (xCodeDetail.matches(XCODE_VERSION)) {
                String[] xcodeVersions = xCodeDetail.split(" ");
                for (String xcodeVersionID : xcodeVersions) {
                    if (xcodeVersionID.matches(XCODE_VERSION_REGEX)) {
                        xcodeVersion = xcodeVersionID;
                    }
                }
            }
        }
        return xcodeVersion;
    }

    private XCodeVersion mapToXcodeVersion(String xcodeVersion) {
        XCodeVersion version = null;
        if (xcodeVersion.startsWith("8")) {
            version = XCodeVersion.XCODE8;
        } else if (xcodeVersion.startsWith("7")) {
            version = XCodeVersion.XCODE7;
        }
        return version;
    }

    private boolean isSimulator(String process) {
        if(!process.contains("[")) {
            return false;
        }
        String pattern = getPatternFromProcess(process);
        return pattern.matches(SIMULATOR_UDID_PATTERN)
                && process.startsWith("iPhone")
                && !process.contains("+");
    }

    private boolean isIDevice(String process) {
        if(!process.contains("[")) {
            return false;
        }
        String pattern = getPatternFromProcess(process);
        return pattern.matches(DEVICE_UDID_PATTERN);
    }


    private String getPatternFromProcess(String process) {
        int udidStart = process.indexOf("[") + 1;
        int udidEnd = process.indexOf("]");
        return process.substring(udidStart, udidEnd);
    }

}
