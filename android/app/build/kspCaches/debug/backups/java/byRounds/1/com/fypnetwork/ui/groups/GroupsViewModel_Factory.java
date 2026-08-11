package com.fypnetwork.ui.groups;

import com.fypnetwork.data.repository.ConnectionsRepository;
import com.fypnetwork.data.repository.GroupsRepository;
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
public final class GroupsViewModel_Factory implements Factory<GroupsViewModel> {
  private final Provider<GroupsRepository> groupsRepositoryProvider;

  private final Provider<ConnectionsRepository> connectionsRepositoryProvider;

  public GroupsViewModel_Factory(Provider<GroupsRepository> groupsRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider) {
    this.groupsRepositoryProvider = groupsRepositoryProvider;
    this.connectionsRepositoryProvider = connectionsRepositoryProvider;
  }

  @Override
  public GroupsViewModel get() {
    return newInstance(groupsRepositoryProvider.get(), connectionsRepositoryProvider.get());
  }

  public static GroupsViewModel_Factory create(Provider<GroupsRepository> groupsRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider) {
    return new GroupsViewModel_Factory(groupsRepositoryProvider, connectionsRepositoryProvider);
  }

  public static GroupsViewModel newInstance(GroupsRepository groupsRepository,
      ConnectionsRepository connectionsRepository) {
    return new GroupsViewModel(groupsRepository, connectionsRepository);
  }
}
