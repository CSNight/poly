package csnight.spider.poly.context;

import csnight.spider.poly.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringContextEvent implements ApplicationListener<ApplicationEvent> {
    @Value(value = "${websocket.server}")
    private String wsServer;
    @Value(value = "${websocket.port}")
    private int wsPort;
    private static final Logger _log = LoggerFactory.getLogger(SpringContextEvent.class);
    private final WebSocketServer wss = WebSocketServer.getInstance();

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            wss.setHost(wsServer);
            wss.setPort(wsPort);
            try {
                wss.run();
                String OS = System.getProperty("os.name").toLowerCase();
                Runtime.getRuntime().exec((OS.contains("windows") ? "rundll32 url.dll,FileProtocolHandler " : "open") + " http://127.0.0.1:8020");
            } catch (Exception e) {
                e.printStackTrace();
            }
            _log.info("Storm Server Start Complete!");
        } else if (applicationEvent instanceof ContextClosedEvent) {
            wss.shutdown();
            _log.info("Storm Server Stop Complete!");
        }
    }
}

