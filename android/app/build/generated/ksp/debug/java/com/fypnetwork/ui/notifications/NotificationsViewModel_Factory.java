package com.fypnetwork.ui.notifications;

import com.fypnetwork.data.repository.NotificationsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class NotificationsViewModel_Factory implements Factory<NotificationsViewModel> {
  private final Provider<NotificationsRepository> notificationsRepositoryProvider;

  public NotificationsViewModel_Factory(
      Provider<NotificationsRepository> notificationsRepositoryProvider) {
    this.notificationsRepositoryProvider = notificationsRepositoryProvider;
  }

  @Override
  public NotificationsViewModel get() {
    return newInstance(notificationsRepositoryProvider.get());
  }

  public static NotificationsViewModel_Factory create(
      Provider<NotificationsRepository> notificationsRepositoryProvider) {
    return new NotificationsViewModel_Factory(notificationsRepositoryProvider);
  }

  public static NotificationsViewModel newInstance(
      NotificationsRepository notificationsRepository) {
    return new NotificationsViewModel(notificationsRepository);
  }
}
