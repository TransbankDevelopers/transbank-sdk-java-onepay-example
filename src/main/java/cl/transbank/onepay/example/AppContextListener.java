package cl.transbank.onepay.example;

import cl.transbank.onepay.Onepay;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        if (System.getenv("LIVE_API_KEY") != null && System.getenv("LIVE_SHARED_SECRET") != null){
            Onepay.setIntegrationType(Onepay.IntegrationType.LIVE);
            Onepay.setApiKey(System.getenv("LIVE_API_KEY"));
            Onepay.setSharedSecret(System.getenv("LIVE_SHARED_SECRET"));
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
