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
public final class NetworkModule_ProvideAuthenticatedClientFactory implements Factory<OkHttpClient> {
  private final Provider<HttpLoggingInterceptor> loggingProvider;

  private final Provider<AuthInterceptor> authInterceptorProvider;

  private final Provider<TokenAuthenticator> authenticatorProvider;

  public NetworkModule_ProvideAuthenticatedClientFactory(
      Provider<HttpLoggingInterceptor> loggingProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> authenticatorProvider) {
    this.loggingProvider = loggingProvider;
    this.authInterceptorProvider = authInterceptorProvider;
    this.authenticatorProvider = authenticatorProvider;
  }

  @Override
  public OkHttpClient get() {
    return provideAuthenticatedClient(loggingProvider.get(), authInterceptorProvider.get(), authenticatorProvider.get());
  }

  public static NetworkModule_ProvideAuthenticatedClientFactory create(
      Provider<HttpLoggingInterceptor> loggingProvider,
      Provider<AuthInterceptor> authInterceptorProvider,
      Provider<TokenAuthenticator> authenticatorProvider) {
    return new NetworkModule_ProvideAuthenticatedClientFactory(loggingProvider, authInterceptorProvider, authenticatorProvider);
  }

  public static OkHttpClient provideAuthenticatedClient(HttpLoggingInterceptor logging,
      AuthInterceptor authInterceptor, TokenAuthenticator authenticator) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAuthenticatedClient(logging, authInterceptor, authenticator));
  }
}
