package com.fypnetwork.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("com.fypnetwork.di.Unauthenticated")
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
public final class NetworkModule_ProvideUnauthenticatedClientFactory implements Factory<OkHttpClient> {
  private final Provider<HttpLoggingInterceptor> loggingProvider;

  public NetworkModule_ProvideUnauthenticatedClientFactory(
      Provider<HttpLoggingInterceptor> loggingProvider) {
    this.loggingProvider = loggingProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideUnauthenticatedClient(loggingProvider.get());
  }

  public static NetworkModule_ProvideUnauthenticatedClientFactory create(
      Provider<HttpLoggingInterceptor> loggingProvider) {
    return new NetworkModule_ProvideUnauthenticatedClientFactory(loggingProvider);
  }

  public static OkHttpClient provideUnauthenticatedClient(HttpLoggingInterceptor logging) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideUnauthenticatedClient(logging));
  }
}
