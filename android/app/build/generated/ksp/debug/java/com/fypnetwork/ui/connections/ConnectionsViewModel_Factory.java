package com.fypnetwork.ui.connections;

import com.fypnetwork.data.repository.ConnectionsRepository;
import com.fypnetwork.data.repository.UsersRepository;
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
public final class ConnectionsViewModel_Factory implements Factory<ConnectionsViewModel> {
  private final Provider<ConnectionsRepository> connectionsRepositoryProvider;

  private final Provider<UsersRepository> usersRepositoryProvider;

  public ConnectionsViewModel_Factory(Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<UsersRepository> usersRepositoryProvider) {
    this.connectionsRepositoryProvider = connectionsRepositoryProvider;
    this.usersRepositoryProvider = usersRepositoryProvider;
  }

  @Override
  public ConnectionsViewModel get() {
    return newInstance(connectionsRepositoryProvider.get(), usersRepositoryProvider.get());
  }

  public static ConnectionsViewModel_Factory create(
      Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<UsersRepository> usersRepositoryProvider) {
    return new ConnectionsViewModel_Factory(connectionsRepositoryProvider, usersRepositoryProvider);
  }

  public static ConnectionsViewModel newInstance(ConnectionsRepository connectionsRepository,
      UsersRepository usersRepository) {
    return new ConnectionsViewModel(connectionsRepository, usersRepository);
  }
}
