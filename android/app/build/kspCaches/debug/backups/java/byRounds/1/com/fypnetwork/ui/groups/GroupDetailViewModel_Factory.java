package com.fypnetwork.ui.groups;

import androidx.lifecycle.SavedStateHandle;
import com.fypnetwork.data.repository.AuthRepository;
import com.fypnetwork.data.repository.ConnectionsRepository;
import com.fypnetwork.data.repository.GroupsRepository;
import com.fypnetwork.data.repository.TasksRepository;
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
public final class GroupDetailViewModel_Factory implements Factory<GroupDetailViewModel> {
  private final Provider<GroupsRepository> groupsRepositoryProvider;

  private final Provider<TasksRepository> tasksRepositoryProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<ConnectionsRepository> connectionsRepositoryProvider;

  private final Provider<SavedStateHandle> savedStateHandleProvider;

  public GroupDetailViewModel_Factory(Provider<GroupsRepository> groupsRepositoryProvider,
      Provider<TasksRepository> tasksRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    this.groupsRepositoryProvider = groupsRepositoryProvider;
    this.tasksRepositoryProvider = tasksRepositoryProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.connectionsRepositoryProvider = connectionsRepositoryProvider;
    this.savedStateHandleProvider = savedStateHandleProvider;
  }

  @Override
  public GroupDetailViewModel get() {
    return newInstance(groupsRepositoryProvider.get(), tasksRepositoryProvider.get(), authRepositoryProvider.get(), connectionsRepositoryProvider.get(), savedStateHandleProvider.get());
  }

  public static GroupDetailViewModel_Factory create(
      Provider<GroupsRepository> groupsRepositoryProvider,
      Provider<TasksRepository> tasksRepositoryProvider,
      Provider<AuthRepository> authRepositoryProvider,
      Provider<ConnectionsRepository> connectionsRepositoryProvider,
      Provider<SavedStateHandle> savedStateHandleProvider) {
    return new GroupDetailViewModel_Factory(groupsRepositoryProvider, tasksRepositoryProvider, authRepositoryProvider, connectionsRepositoryProvider, savedStateHandleProvider);
  }

  public static GroupDetailViewModel newInstance(GroupsRepository groupsRepository,
      TasksRepository tasksRepository, AuthRepository authRepository,
      ConnectionsRepository connectionsRepository, SavedStateHandle savedStateHandle) {
    return new GroupDetailViewModel(groupsRepository, tasksRepository, authRepository, connectionsRepository, savedStateHandle);
  }
}
