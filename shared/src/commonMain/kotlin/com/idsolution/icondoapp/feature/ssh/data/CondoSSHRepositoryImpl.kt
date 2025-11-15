package com.example.testkmpapp.feature.ssh.data

import com.example.testkmpapp.feature.auth.domain.AuthRepository
import com.example.testkmpapp.feature.ssh.data.models.StartTunnelRequest
import com.example.testkmpapp.feature.ssh.data.models.SubmitLoginRequest
import com.example.testkmpapp.feature.ssh.domain.CondoSSHRepository
import com.example.testkmpapp.feature.ssh.domain.models.CondoSite
import com.idsolution.icondoapp.core.data.networking.DataError
import com.idsolution.icondoapp.core.data.networking.EmptyDataResult
import com.idsolution.icondoapp.core.data.networking.Result
import com.idsolution.icondoapp.feature.ssh.data.models.phonebook.PhoneBookDtoItem
import com.idsolution.icondoapp.feature.ssh.data.models.phonebook.toDomain
import com.idsolution.icondoapp.feature.ssh.data.models.sites.DoorNameDto
import com.idsolution.icondoapp.feature.ssh.data.models.sites.SitesDto
import com.idsolution.icondoapp.feature.ssh.data.models.sites.toDomain
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorName
import com.idsolution.icondoapp.feature.ssh.domain.models.DoorStatus
import com.idsolution.icondoapp.feature.ssh.domain.models.PhoneBook
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class CondoSSHRepositoryImpl(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository  // ✅ Ajouter cette dépendance
) : CondoSSHRepository {

    override suspend fun domains(): Result<List<CondoSite>, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("🌐 Getting domains")

            var response = httpClient.get(urlString = "https://api.i-dsolution.com/sites")

            println("📥 domains response status: ${response.status}")

            // ✅ Gérer le 403/401 - tenter un refresh et réessayer
            if (response.status.value == 403 || response.status.value == 401) {
                println("⚠️ ${response.status.value} received - Attempting token refresh")

                when (val refreshResult = authRepository.refreshToken()) {
                    is Result.Success -> {
                        println("✅ Token refreshed - Retrying domains")
                        // Réessayer la requête avec le nouveau token
                        response = httpClient.get(urlString = "https://api.i-dsolution.com/sites")
                        println("📥 Retry domains response status: ${response.status}")
                    }
                    is Result.Error -> {
                        println("❌ Token refresh failed")
                        return@withContext Result.Error(
                            DataError.Network.UNAUTHORIZED,
                            "Token refresh failed"
                        )
                    }
                }
            }

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val sitesDto = json.decodeFromString<SitesDto>(response.body())
                println("✅ Domains retrieved: ${sitesDto.sites.size} sites")
                Result.Success(sitesDto.toDomain())
            } else {
                println("❌ domains failed: ${response.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${response.call.request.url} : ${response.status.description}"
                )
            }
        }

    override suspend fun phonebook(siteName: String): Result<List<PhoneBook>, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("📞 Getting phonebook for: $siteName")

            var response = httpClient.get(
                urlString = "https://api.i-dsolution.com/sites/phonebook?siteName=$siteName"
            )

            println("📥 phonebook response status: ${response.status}")

            // ✅ Gérer le 403/401
            if (response.status.value == 403 || response.status.value == 401) {
                println("⚠️ ${response.status.value} received - Attempting token refresh")

                when (val refreshResult = authRepository.refreshToken()) {
                    is Result.Success -> {
                        println("✅ Token refreshed - Retrying phonebook")
                        response = httpClient.get(
                            urlString = "https://api.i-dsolution.com/sites/phonebook?siteName=$siteName"
                        )
                        println("📥 Retry phonebook response status: ${response.status}")
                    }
                    is Result.Error -> {
                        println("❌ Token refresh failed")
                        return@withContext Result.Error(
                            DataError.Network.UNAUTHORIZED,
                            "Token refresh failed"
                        )
                    }
                }
            }

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val phoneBook = json.decodeFromString<List<PhoneBookDtoItem>>(response.body())
                println("✅ Phonebook retrieved: ${phoneBook.size} entries")
                Result.Success(phoneBook.map { it.toDomain() })
            } else {
                println("❌ phonebook failed: ${response.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${response.call.request.url} : ${response.status.description}"
                )
            }
        }

    override suspend fun startTunnel(
        hostname: String,
        localPort: Int,
        username: String,
        password: String,
        sshPort: Int,
        siteName: String
    ): EmptyDataResult<DataError.Network> = withContext(Dispatchers.IO) {
        println("🔌 Starting tunnel for: $siteName")

        var response = httpClient.post(
            urlString = "https://api.i-dsolution.com/ssh/start-tunnel"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                StartTunnelRequest(
                    hostname = hostname,
                    port = localPort,
                    password = password,
                    username = username,
                    sshPort = sshPort,
                    siteName = siteName
                )
            )
        }

        println("📥 startTunnel response status: ${response.status}")

        // ✅ Gérer le 403/401
        if (response.status.value == 403 || response.status.value == 401) {
            println("⚠️ ${response.status.value} received - Attempting token refresh")

            when (val refreshResult = authRepository.refreshToken()) {
                is Result.Success -> {
                    println("✅ Token refreshed - Retrying startTunnel")
                    response = httpClient.post(
                        urlString = "https://api.i-dsolution.com/ssh/start-tunnel"
                    ) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            StartTunnelRequest(
                                hostname = hostname,
                                port = localPort,
                                password = password,
                                username = username,
                                sshPort = sshPort,
                                siteName = siteName
                            )
                        )
                    }
                    println("📥 Retry startTunnel response status: ${response.status}")
                }
                is Result.Error -> {
                    println("❌ Token refresh failed")
                    return@withContext Result.Error(
                        DataError.Network.UNAUTHORIZED,
                        "Token refresh failed"
                    )
                }
            }
        }

        if (response.status.isSuccess()) {
            println("✅ Tunnel started successfully")
            Result.Success(Unit)
        } else {
            println("❌ startTunnel failed: ${response.status}")
            Result.Error(
                DataError.Network.SERVER_ERROR,
                "${response.call.request.url} : ${response.status.description}"
            )
        }
    }

    override suspend fun submitLogin(
        username: String,
        password: String,
        siteName: String
    ): EmptyDataResult<DataError.Network> = withContext(Dispatchers.IO) {
        println("🔐 Submitting login for: $siteName")

        var response = httpClient.post(
            urlString = "https://api.i-dsolution.com/sites/submit_login"
        ) {
            contentType(ContentType.Application.Json)
            setBody(
                SubmitLoginRequest(
                    password = password,
                    username = username,
                    siteName = siteName
                )
            )
        }

        println("📥 submitLogin response status: ${response.status}")

        // ✅ Gérer le 403/401
        if (response.status.value == 403 || response.status.value == 401) {
            println("⚠️ ${response.status.value} received - Attempting token refresh")

            when (val refreshResult = authRepository.refreshToken()) {
                is Result.Success -> {
                    println("✅ Token refreshed - Retrying submitLogin")
                    response = httpClient.post(
                        urlString = "https://api.i-dsolution.com/sites/submit_login"
                    ) {
                        contentType(ContentType.Application.Json)
                        setBody(
                            SubmitLoginRequest(
                                password = password,
                                username = username,
                                siteName = siteName
                            )
                        )
                    }
                    println("📥 Retry submitLogin response status: ${response.status}")
                }
                is Result.Error -> {
                    println("❌ Token refresh failed")
                    return@withContext Result.Error(
                        DataError.Network.UNAUTHORIZED,
                        "Token refresh failed"
                    )
                }
            }
        }

        if (response.status.isSuccess()) {
            println("✅ Login submitted successfully")
            Result.Success(Unit)
        } else {
            println("❌ submitLogin failed: ${response.status}")
            Result.Error(
                DataError.Network.SERVER_ERROR,
                "${response.call.request.url} : ${response.status.value}"
            )
        }
    }

    override suspend fun unlockDoor(
        doorId: Int,
        siteName: String
    ): Result<String, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("🚪 Unlocking door $doorId for: $siteName")

            var response = httpClient.get(
                urlString = "https://api.i-dsolution.com/sites/unlockDoor?doorId=$doorId&siteName=$siteName"
            )

            println("📥 unlockDoor response status: ${response.status}")

            // ✅ Gérer le 403/401
            if (response.status.value == 403 || response.status.value == 401) {
                println("⚠️ ${response.status.value} received - Attempting token refresh")

                when (val refreshResult = authRepository.refreshToken()) {
                    is Result.Success -> {
                        println("✅ Token refreshed - Retrying unlockDoor")
                        response = httpClient.get(
                            urlString = "https://api.i-dsolution.com/sites/unlockDoor?doorId=$doorId&siteName=$siteName"
                        )
                        println("📥 Retry unlockDoor response status: ${response.status}")
                    }
                    is Result.Error -> {
                        println("❌ Token refresh failed")
                        return@withContext Result.Error(
                            DataError.Network.UNAUTHORIZED,
                            "Token refresh failed"
                        )
                    }
                }
            }

            if (response.status.isSuccess()) {
                println("✅ Door unlocked successfully")
                Result.Success("Success")
            } else {
                println("❌ unlockDoor failed: ${response.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${response.call.request.url} : ${response.status.description}"
                )
            }
        }

    override fun getDoorStatus(siteName: String): Flow<Result<List<DoorStatus>, DataError.Network>> =
        flow {
            while (true) {
                val result = withContext(Dispatchers.IO) {
                    try {
                        var response = httpClient.get("https://api.i-dsolution.com/sites/getDoorStatus") {
                            parameter("siteName", siteName)
                        }

                        // ✅ Gérer le 403/401
                        if (response.status.value == 403 || response.status.value == 401) {
                            println("⚠️ getDoorStatus: ${response.status.value} - Attempting token refresh")

                            when (val refreshResult = authRepository.refreshToken()) {
                                is Result.Success -> {
                                    println("✅ Token refreshed - Retrying getDoorStatus")
                                    response = httpClient.get("https://api.i-dsolution.com/sites/getDoorStatus") {
                                        parameter("siteName", siteName)
                                    }
                                }
                                is Result.Error -> {
                                    println("❌ Token refresh failed")
                                    return@withContext Result.Error(
                                        DataError.Network.UNAUTHORIZED,
                                        "Token refresh failed"
                                    ) as Result<List<DoorStatus>, DataError.Network>
                                }
                            }
                        }

                        if (response.status.isSuccess()) {
                            val doorStatusList = response.body<List<DoorStatus>>()
                            Result.Success(doorStatusList)
                        } else {
                            Result.Error(
                                DataError.Network.SERVER_ERROR,
                                "${response.call.request.url} : ${response.status.description}"
                            )
                        }
                    } catch (e: Exception) {
                        println("❌ getDoorStatus exception: ${e.message}")
                        Result.Error(DataError.Network.SERVER_ERROR, "${e.message}")
                    }
                }

                emit(result)
                delay(5.seconds)
            }
        }

    override suspend fun getDoorsName(siteName: String): Result<List<DoorName>, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("🚪 Getting door names for: $siteName")

            var response = httpClient.get(
                urlString = "https://api.i-dsolution.com/sites/getDoorNames?siteName=$siteName"
            )

            println("📥 getDoorsName response status: ${response.status}")

            // ✅ Gérer le 403/401
            if (response.status.value == 403 || response.status.value == 401) {
                println("⚠️ ${response.status.value} received - Attempting token refresh")

                when (val refreshResult = authRepository.refreshToken()) {
                    is Result.Success -> {
                        println("✅ Token refreshed - Retrying getDoorsName")
                        response = httpClient.get(
                            urlString = "https://api.i-dsolution.com/sites/getDoorNames?siteName=$siteName"
                        )
                        println("📥 Retry getDoorsName response status: ${response.status}")
                    }
                    is Result.Error -> {
                        println("❌ Token refresh failed")
                        return@withContext Result.Error(
                            DataError.Network.UNAUTHORIZED,
                            "Token refresh failed"
                        )
                    }
                }
            }

            if (response.status.isSuccess()) {
                val json = Json { ignoreUnknownKeys = true }
                val doorsNameDto = json.decodeFromString<List<DoorNameDto>>(response.body())
                println("✅ Door names retrieved: ${doorsNameDto.size} doors")
                Result.Success(doorsNameDto.map { it.toDomain() })
            } else {
                println("❌ getDoorsName failed: ${response.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${response.call.request.url} : ${response.status.description}"
                )
            }
        }

    override suspend fun getCamera(siteId: String): Result<String, DataError.Network> =
        withContext(Dispatchers.IO) {
            println("📷 Getting camera for site: $siteId")

            var response = httpClient.get(urlString = "https://api.i-dsolution.com/camera") {
                setBody(siteId)
            }

            println("📥 getCamera response status: ${response.status}")

            // ✅ Gérer le 403/401
            if (response.status.value == 403 || response.status.value == 401) {
                println("⚠️ ${response.status.value} received - Attempting token refresh")

                when (val refreshResult = authRepository.refreshToken()) {
                    is Result.Success -> {
                        println("✅ Token refreshed - Retrying getCamera")
                        response = httpClient.get(urlString = "https://api.i-dsolution.com/camera") {
                            setBody(siteId)
                        }
                        println("📥 Retry getCamera response status: ${response.status}")
                    }
                    is Result.Error -> {
                        println("❌ Token refresh failed")
                        return@withContext Result.Error(
                            DataError.Network.UNAUTHORIZED,
                            "Token refresh failed"
                        )
                    }
                }
            }

            if (response.status.isSuccess()) {
                println("✅ Camera retrieved successfully")
                Result.Success(response.body<String>().toString())
            } else {
                println("❌ getCamera failed: ${response.status}")
                Result.Error(
                    DataError.Network.SERVER_ERROR,
                    "${response.call.request.url} : ${response.status.description}"
                )
            }
        }
}