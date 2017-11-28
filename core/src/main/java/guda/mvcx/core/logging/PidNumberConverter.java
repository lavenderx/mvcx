package guda.mvcx.core.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.lang.management.ManagementFactory;

public class PidNumberConverter extends ClassicConverter {

    private final String pid;

    public PidNumberConverter() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        this.pid = processName.split("@")[0];
    }

    @Override
    public String convert(ILoggingEvent event) {
        return pid;
    }
}
