package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.TasksApi;
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
public final class TasksRepository_Factory implements Factory<TasksRepository> {
  private final Provider<TasksApi> tasksApiProvider;

  public TasksRepository_Factory(Provider<TasksApi> tasksApiProvider) {
    this.tasksApiProvider = tasksApiProvider;
  }

  @Override
  public TasksRepository get() {
    return newInstance(tasksApiProvider.get());
  }

  public static TasksRepository_Factory create(Provider<TasksApi> tasksApiProvider) {
    return new TasksRepository_Factory(tasksApiProvider);
  }

  public static TasksRepository newInstance(TasksApi tasksApi) {
    return new TasksRepository(tasksApi);
  }
}
