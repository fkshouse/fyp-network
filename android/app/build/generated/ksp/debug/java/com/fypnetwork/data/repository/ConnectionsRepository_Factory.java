package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.ConnectionsApi;
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
public final class ConnectionsRepository_Factory implements Factory<ConnectionsRepository> {
  private final Provider<ConnectionsApi> connectionsApiProvider;

  public ConnectionsRepository_Factory(Provider<ConnectionsApi> connectionsApiProvider) {
    this.connectionsApiProvider = connectionsApiProvider;
  }

  @Override
  public ConnectionsRepository get() {
    return newInstance(connectionsApiProvider.get());
  }

  public static ConnectionsRepository_Factory create(
      Provider<ConnectionsApi> connectionsApiProvider) {
    return new ConnectionsRepository_Factory(connectionsApiProvider);
  }

  public static ConnectionsRepository newInstance(ConnectionsApi connectionsApi) {
    return new ConnectionsRepository(connectionsApi);
  }
}
