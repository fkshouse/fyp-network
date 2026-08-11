package com.fypnetwork.di

import javax.inject.Qualifier

/** Marks the OkHttpClient/Retrofit used for login/register/refresh calls,
 *  which must NOT carry the auth interceptor/authenticator (that would
 *  create a circular dependency: refreshing a token requires calling an
 *  endpoint that itself would try to refresh the token). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Unauthenticated
