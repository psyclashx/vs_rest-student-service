package app;

import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import service.PruefungsleistungService;
import service.StudentService;

import javax.swing.*;
import jakarta.ws.rs.ext.RuntimeDelegate;
import java.io.IOException;
import java.io.ObjectInputFilter;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class Server {

    public static final String DB_CONNECTION = "jdbc:mysql://im-vm-011/vs-08";
    public static final String DB_USERNAME = "vs-08";
    public static final String DB_PASSWORD = "vs-08-pw";

    public static HazelcastInstance hazelcast;

    public static void main(String[] args) throws IOException {

        //IN-MEMORY-DATAGRID
        //Create Hazelcast Instance
        Config hazelcastConfig = new Config();
        NetworkConfig networkConfig = hazelcastConfig.getNetworkConfig();
        networkConfig.setPortAutoIncrement(true);
        networkConfig.getInterfaces().setEnabled(true);

        networkConfig.getInterfaces().setInterfaces(Arrays.asList("192.*.*.*"));
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(true);
        joinConfig.getMulticastConfig().setMulticastGroup("224.2.2.3");
        joinConfig.getMulticastConfig().setMulticastPort(54327);

        //hazelcast node

        hazelcast = Hazelcast.newHazelcastInstance(hazelcastConfig);

        // Create configuration object for webserver instance
        ResourceConfig config = new ResourceConfig();
        // Register REST-resources (i.e. service classes) with the webserver
        config.register(ServerExceptionMapper.class);
        config.register(StudentService.class);
        config.register(PruefungsleistungService.class);
        // add further REST-resources like this:  config.register(Xyz.class);

        // Create webserver instance and start it
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        HttpHandler handler = RuntimeDelegate.getInstance().createEndpoint(config, HttpHandler.class);
        // Context is part of the URI directly after  http://domain.tld:port/
        server.createContext("/restapi", handler);
        server.start();

        // Show dialogue in order to prevent premature ending of server(s)
        JOptionPane.showMessageDialog(null, "Stop server...");
        server.stop(0);
        hazelcast.shutdown();
    }
}
