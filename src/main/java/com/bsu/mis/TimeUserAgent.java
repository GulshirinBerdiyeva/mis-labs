package com.bsu.mis;

import jadex.base.PlatformConfiguration;
import jadex.base.Starter;
import jadex.bridge.service.IService;
import jadex.commons.future.ISubscriptionIntermediateFuture;
import jadex.micro.annotation.*;

@Agent
@RequiredServices(
        @RequiredService(name="timeservices", type= TimeService.class, multiple=true, binding=@Binding(scope=Binding.SCOPE_GLOBAL))
)
public class TimeUserAgent {

    @AgentService
    public void addTimeService(TimeService timeService) {
        ISubscriptionIntermediateFuture<String> subscription = timeService.subscribe();
        while(subscription.hasNextIntermediateResult()) {
            String time = subscription.getNextIntermediateResult();
            String platform = ((IService)timeService).getServiceIdentifier().getProviderId().getPlatformName();
            System.out.println("New time received from " + platform + " at " + timeService.getLocation() + ": " + time);
        }
    }

    public static void main(String[] args) {
        PlatformConfiguration platformConfiguration = PlatformConfiguration.getDefaultNoGui();
        platformConfiguration.addComponent(TimeUserAgent.class);
        Starter.createPlatform(platformConfiguration).get();
    }
}