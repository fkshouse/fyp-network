package com.fypnetwork.data.repository;

import com.fypnetwork.data.remote.PostsApi;
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
public final class PostsRepository_Factory implements Factory<PostsRepository> {
  private final Provider<PostsApi> postsApiProvider;

  public PostsRepository_Factory(Provider<PostsApi> postsApiProvider) {
    this.postsApiProvider = postsApiProvider;
  }

  @Override
  public PostsRepository get() {
    return newInstance(postsApiProvider.get());
  }

  public static PostsRepository_Factory create(Provider<PostsApi> postsApiProvider) {
    return new PostsRepository_Factory(postsApiProvider);
  }

  public static PostsRepository newInstance(PostsApi postsApi) {
    return new PostsRepository(postsApi);
  }
}
