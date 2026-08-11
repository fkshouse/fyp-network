package com.fypnetwork.data.repository

import com.fypnetwork.data.remote.UsersApi
import com.fypnetwork.data.remote.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsersRepository @Inject constructor(
    private val usersApi: UsersApi,
) {
    suspend fun getUser(id: String): UserDto = usersApi.getUser(id)

    suspend fun search(query: String): List<UserDto> = usersApi.search(query)

    suspend fun updateProfile(headline: String?, company: String?, bio: String?): UserDto {
        val body = buildMap {
            headline?.let { put("headline", it) }
            company?.let { put("company", it) }
            bio?.let { put("bio", it) }
        }
        return usersApi.updateMe(body)
    }

    suspend fun uploadProfilePicture(file: File): UserDto = withContext(Dispatchers.IO) {
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)
        usersApi.uploadProfilePicture(part)
    }
}
