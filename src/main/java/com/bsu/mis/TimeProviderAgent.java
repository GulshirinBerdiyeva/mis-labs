package com.bsu.mis;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.IInternalAccess;
import jadex.bridge.component.IExecutionFeature;
import jadex.bridge.service.annotation.Service;
import jadex.commons.future.IFuture;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.commons.future.SubscriptionIntermediateFuture;
import jadex.commons.future.TerminationCommand;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentBody;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

@Agent
@Service
@ProvidedServices(@ProvidedService(type= TimeService.class))
public class TimeProviderAgent implements TimeService {
    private Set<SubscriptionIntermediateFuture<String>> subscriptions = new LinkedHashSet<>();
    private String location = determineLocation();

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public ISubscriptionIntermediateFuture<String> subscribe() {
        final SubscriptionIntermediateFuture<String> subscriber = new SubscriptionIntermediateFuture<>();
        subscriptions.add(subscriber);

        subscriber.setTerminationCommand(new TerminationCommand() {
            public void terminated(Exception reason) {
                System.out.println("Removed subscriber due to: " + reason);
                subscriptions.remove(subscriber);
            }
        });

        return subscriber;
    }

    @AgentBody
    public void body(IInternalAccess internalAccess) {
        IExecutionFeature executionFeature = internalAccess.getComponentFeature(IExecutionFeature.class);

        executionFeature.repeatStep(5000 - System.currentTimeMillis() % 5000, 5000,
                componentStep -> {
            subscriptions.forEach(subscriber -> subscriber.addIntermediateResultIfUndone(new Date().toString()));
            return IFuture.DONE;
        });
    }

    protected static String determineLocation() {
        String result;
        try {
            Scanner scanner = new Scanner(new URL("http://ip-api.com/json").openStream(), "UTF-8");

            String country = null;
            String city = null;
            scanner.useDelimiter(",");
            while(scanner.findWithinHorizon("\"([^\"]*)\"[^:]*:[^\"]*\"([^\"]*)\"", 0)!=null) {
                String key = scanner.match().group(1);
                String val = scanner.match().group(2);
                if("country".equals(key)) {
                    country = val;
                } else if("city".equals(key)) {
                    city = val;
                }
            }
            scanner.close();

            result = city != null ? country != null ? city + ", " + country : city : country != null ? country : "unknown";
        } catch(Exception e) {
            result = "unknown";
        }

        return result;
    }

    public static void main(String[] args) {
        PlatformConfiguration platformConfiguration = PlatformConfiguration.getDefaultNoGui();
        platformConfiguration.addComponent(TimeProviderAgent.class);
        Starter.createPlatform(platformConfiguration).get();
    }
}