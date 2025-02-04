
package org.travice;

import org.travice.helper.Log;

import java.io.File;
import java.net.BindException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ServerManager {

    private final List<TrackerServer> serverList = new LinkedList<>();
    private final Map<String, BaseProtocol> protocolList = new ConcurrentHashMap<>();

    public ServerManager() throws Exception {

        List<String> names = new LinkedList<>();
        String packageName = "org.travice.protocol";
        String packagePath = packageName.replace('.', '/');
        URL packageUrl = Thread.currentThread().getContextClassLoader().getResource(packagePath);

        if (packageUrl.getProtocol().equals("jar")) {
            String jarFileName = URLDecoder.decode(packageUrl.getFile(), StandardCharsets.UTF_8.name());
            try (JarFile jf = new JarFile(jarFileName.substring(5, jarFileName.indexOf("!")))) {
                Enumeration<JarEntry> jarEntries = jf.entries();
                while (jarEntries.hasMoreElements()) {
                    String entryName = jarEntries.nextElement().getName();
                    if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length() + 5) {
                        names.add(entryName.substring(packagePath.length() + 1, entryName.lastIndexOf('.')));
                    }
                }
            }
        } else {
            File folder = new File(new URI(packageUrl.toString()));
            File[] files = folder.listFiles();
            if (files != null) {
                for (File actual: files) {
                    String entryName = actual.getName();
                    names.add(entryName.substring(0, entryName.lastIndexOf('.')));
                }
            }
        }

        for (String name : names) {
            Class protocolClass = Class.forName(packageName + '.' + name);
            if (BaseProtocol.class.isAssignableFrom(protocolClass)) {
                BaseProtocol baseProtocol = (BaseProtocol) protocolClass.newInstance();
                initProtocolServer(baseProtocol);
                protocolList.put(baseProtocol.getName(), baseProtocol);
            }
        }
    }

    public BaseProtocol getProtocol(String name) {
        return protocolList.get(name);
    }

    public void start() throws Exception {
        for (TrackerServer server: serverList) {
            try {
                server.start();
            } catch (BindException e) {
                Log.warning("One of the protocols is disabled due to port conflict");
            }
        }
    }

    public void stop() {
        for (TrackerServer server: serverList) {
            server.stop();
        }
        GlobalTimer.release();
    }

    private void initProtocolServer(final Protocol protocol) {
        if (Context.getConfig().hasKey(protocol.getName() + ".port")) {
            protocol.initTrackerServers(serverList);
        }
    }

}
