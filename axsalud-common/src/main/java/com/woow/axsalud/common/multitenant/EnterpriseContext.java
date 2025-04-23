package com.woow.axsalud.common.multitenant;

public class EnterpriseContext {
  private static final ThreadLocal<String> CURRENT_ENTERPRISE = new ThreadLocal<>();

  public static String getCurrentEnterprise() {
    return CURRENT_ENTERPRISE.get();
  }

  public static void setCurrentEnterprise(String tenant) {
    CURRENT_ENTERPRISE.set(tenant);
  }
}
