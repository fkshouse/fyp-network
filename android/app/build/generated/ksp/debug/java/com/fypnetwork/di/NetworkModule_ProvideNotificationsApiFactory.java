package com.fypnetwork.di;

import com.fypnetwork.data.remote.NotificationsApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideNotificationsApiFactory implements Factory<NotificationsApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideNotificationsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public NotificationsApi get() {
    return provideNotificationsApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideNotificationsApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideNotificationsApiFactory(retrofitProvider);
  }

  public static NotificationsApi provideNotificationsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideNotificationsApi(retrofit));
  }
}
