package eu.mcprotection.escanorwebhook.util;

import java.lang.management.ManagementFactory;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ServerResourcesUtils {

  @SneakyThrows({
      MalformedObjectNameException.class,
      ReflectionException.class,
      InstanceNotFoundException.class
  })
  public static double getProcessCpuLoad() {
    final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    final ObjectName objectName = ObjectName.getInstance("java.lang:type=OperatingSystem");
    final AttributeList attributeList =
        platformMBeanServer.getAttributes(objectName, new String[]{"ProcessCpuLoad"});
    if (attributeList.isEmpty()) {
      return Double.NaN;
    }

    final Attribute attribute = (Attribute) attributeList.get(0);
    final double value = (Double) attribute.getValue();
    if (value == -1.0) {
      return Double.NaN;
    }

    return ((int) (value * 1000) / 10.0);
  }

  public static long getMemory() {
    return bytesToMegabytes(getUsedMemory());
  }

  private static long getUsedMemory() {
    final Runtime runtime = Runtime.getRuntime();
    return runtime.totalMemory() - runtime.freeMemory();
  }

  private static long bytesToMegabytes(long bytes) {
    final long megabyte = 1024L * 1024L;
    return bytes / megabyte;
  }
}
