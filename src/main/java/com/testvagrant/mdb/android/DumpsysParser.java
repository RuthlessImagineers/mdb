package com.testvagrant.mdb.android;

import com.testvagrant.commons.entities.SmartBOT;
import com.testvagrant.commons.entities.performance.Activity;
import com.testvagrant.commons.entities.performance.CpuStatistics;
import com.testvagrant.commons.entities.performance.MemoryStatistics;
import com.testvagrant.mdb.core.CommandExecutor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.testvagrant.mdb.utils.Commands.AndroidCommands.*;
import static com.testvagrant.mdb.utils.Commons.convertToMB;
import static java.util.stream.Collectors.toList;

public class DumpsysParser {


    private SmartBOT smartBOT;
    private CommandExecutor commandExecutor;
    private static String previousActivity = "OptimusActivity";

    public DumpsysParser(SmartBOT smartBOT) {
        this.smartBOT = smartBOT;
        this.commandExecutor = new CommandExecutor();
    }

    public CpuStatistics getCpuUsage() {
        HashMap<String, String> userKernelInfo = new HashMap<>();
        String cpuInfoCommand = String.format(GET_CPUINFO,smartBOT.getDeviceUdid(),smartBOT.getAppPackageName());
        List<String> cpuInfo = commandExecutor.exec(cpuInfoCommand).asList();
        for (String s : cpuInfo) {
            if (s.contains("TOTAL")) {
                String cpuUsageOutput = s.split(":")[1].trim();
                System.out.println("String to be parsed -- " + cpuUsageOutput);
                Pattern p = Pattern.compile(CPU_REGEX);
                Matcher matcher = p.matcher(cpuUsageOutput);
                matcher.find();
                userKernelInfo.put(matcher.group(3), matcher.group(1));
                userKernelInfo.put(matcher.group(7), matcher.group(5));
            }
        }
        CpuStatistics cpuStatistics = new CpuStatistics();
        cpuStatistics.setUser(userKernelInfo.get("user"));
        cpuStatistics.setKernel(userKernelInfo.get("kernel"));
        return cpuStatistics;
    }


    public MemoryStatistics getMemoryInfo() {
        MemoryStatistics memoryStatistics = new MemoryStatistics();
        String memUsageCommand = String.format(GET_MEMINFO,smartBOT.getDeviceUdid(),smartBOT.getAppPackageName());
        List<String> memInfo = commandExecutor.exec(memUsageCommand).asList();
        Optional<String> memoryDetails = memInfo.stream().filter(line->line.trim().startsWith("TOTAL ")).findFirst();
        if(memoryDetails.isPresent()) {
            List<String> memoryTotal = Arrays.asList(memoryDetails.get().split(" "));
            List<String> collect = memoryTotal.stream().filter(line -> line.length() > 0).collect(toList());
            memoryStatistics.setTotal(convertToMB(collect.get(1)));
            memoryStatistics.setActual(convertToMB(collect.get(2)));
        }
        return memoryStatistics;
    }


    public Activity getCurrentActivity() {
        String focussedActivityCommand = String.format(GET_FOCUSSED_ACTIVITY,smartBOT.getDeviceUdid());
        List<String> activityDetails = commandExecutor.exec(focussedActivityCommand).asList();
        Optional<String> mCurrentFocus = activityDetails.stream().filter(line -> line.trim().startsWith("mCurrentFocus")).findFirst();
        Activity activity = new Activity();
        if(mCurrentFocus.isPresent()) {
            if(!mCurrentFocus.get().contains("null")) {
                String focussedActivity = Arrays.stream(mCurrentFocus.get().split("\\.")).filter(word -> word.endsWith("}")).findFirst().get().replaceAll("}", "");
                previousActivity = focussedActivity;
                activity.setFocussedActivity(focussedActivity);
            } else {
                activity.setFocussedActivity(previousActivity);
            }
        }
       return activity;
    }
}
