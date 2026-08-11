package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.NotificationsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation"
})
public final class NotificationsRepository_Factory implements Factory<NotificationsRepository> {
  private final Provider<NotificationsApi> notificationsApiProvider;

  public NotificationsRepository_Factory(Provider<NotificationsApi> notificationsApiProvider) {
    this.notificationsApiProvider = notificationsApiProvider;
  }

  @Override
  public NotificationsRepository get() {
    return newInstance(notificationsApiProvider.get());
  }

  public static NotificationsRepository_Factory create(
      Provider<NotificationsApi> notificationsApiProvider) {
    return new NotificationsRepository_Factory(notificationsApiProvider);
  }

  public static NotificationsRepository newInstance(NotificationsApi notificationsApi) {
    return new NotificationsRepository(notificationsApi);
  }
}
