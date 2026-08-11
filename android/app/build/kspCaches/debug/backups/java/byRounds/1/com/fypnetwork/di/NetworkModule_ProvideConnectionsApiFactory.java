package com.fypnetwork.di;

import com.fypnetwork.data.remote.ConnectionsApi;
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
public final class NetworkModule_ProvideConnectionsApiFactory implements Factory<ConnectionsApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideConnectionsApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public ConnectionsApi get() {
    return provideConnectionsApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideConnectionsApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideConnectionsApiFactory(retrofitProvider);
  }

  public static ConnectionsApi provideConnectionsApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideConnectionsApi(retrofit));
  }
}
