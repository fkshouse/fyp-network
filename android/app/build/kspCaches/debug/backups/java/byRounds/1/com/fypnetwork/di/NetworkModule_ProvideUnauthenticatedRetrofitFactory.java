package com.fypnetwork.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import kotlinx.serialization.json.Json;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideUnauthenticatedRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> clientProvider;

  private final Provider<Json> jsonProvider;

  public NetworkModule_ProvideUnauthenticatedRetrofitFactory(Provider<OkHttpClient> clientProvider,
      Provider<Json> jsonProvider) {
    this.clientProvider = clientProvider;
    this.jsonProvider = jsonProvider;
  }

  @Override
  public Retrofit get() {
    return provideUnauthenticatedRetrofit(clientProvider.get(), jsonProvider.get());
  }

  public static NetworkModule_ProvideUnauthenticatedRetrofitFactory create(
      Provider<OkHttpClient> clientProvider, Provider<Json> jsonProvider) {
    return new NetworkModule_ProvideUnauthenticatedRetrofitFactory(clientProvider, jsonProvider);
  }

  public static Retrofit provideUnauthenticatedRetrofit(OkHttpClient client, Json json) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideUnauthenticatedRetrofit(client, json));
  }
}
