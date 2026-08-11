package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.GroupsApi;
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
public final class GroupsRepository_Factory implements Factory<GroupsRepository> {
  private final Provider<GroupsApi> groupsApiProvider;

  public GroupsRepository_Factory(Provider<GroupsApi> groupsApiProvider) {
    this.groupsApiProvider = groupsApiProvider;
  }

  @Override
  public GroupsRepository get() {
    return newInstance(groupsApiProvider.get());
  }

  public static GroupsRepository_Factory create(Provider<GroupsApi> groupsApiProvider) {
    return new GroupsRepository_Factory(groupsApiProvider);
  }

  public static GroupsRepository newInstance(GroupsApi groupsApi) {
    return new GroupsRepository(groupsApi);
  }
}
