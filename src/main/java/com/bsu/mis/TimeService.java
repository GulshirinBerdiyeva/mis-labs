package com.bsu.mis;

import jadex.bridge.service.annotation.Security;
import jadex.commons.future.ISubscriptionIntermediateFuture;

@Security(Security.UNRESTRICTED)
public interface TimeService {
    String getLocation();
    ISubscriptionIntermediateFuture<String> subscribe();
}