package com.fypnetwork.ui.navigation;

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
public final class NotificationBadgeViewModel_Factory implements Factory<NotificationBadgeViewModel> {
  private final Provider<NotificationsRepository> notificationsRepositoryProvider;

  public NotificationBadgeViewModel_Factory(
      Provider<NotificationsRepository> notificationsRepositoryProvider) {
    this.notificationsRepositoryProvider = notificationsRepositoryProvider;
  }

  @Override
  public NotificationBadgeViewModel get() {
    return newInstance(notificationsRepositoryProvider.get());
  }

  public static NotificationBadgeViewModel_Factory create(
      Provider<NotificationsRepository> notificationsRepositoryProvider) {
    return new NotificationBadgeViewModel_Factory(notificationsRepositoryProvider);
  }

  public static NotificationBadgeViewModel newInstance(
      NotificationsRepository notificationsRepository) {
    return new NotificationBadgeViewModel(notificationsRepository);
  }
}
