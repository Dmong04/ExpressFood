package com.project.expressfood.auth

import android.net.Uri
import com.google.firebase.auth.FirebaseUser
import com.project.expressfood.data.remote.auth.AuthService
import com.project.expressfood.data.remote.firestore.UserFirestoreService
import com.project.expressfood.data.repository.AuthRepository
import com.project.expressfood.domain.model.User
import com.project.expressfood.domain.model.UserRole
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthRepositoryTest {

    @MockK lateinit var authService: AuthService
    @MockK lateinit var userFirestoreService: UserFirestoreService
    @MockK lateinit var firebaseUser: FirebaseUser

    private lateinit var repository: AuthRepository

    private val fakeUser = User(
        uid          = "uid_test_001",
        firstName    = "Juan",
        lastName     = "Pérez",
        phone        = "",
        profilePhoto = "https://photo.url/juan.jpg",
        role         = UserRole.CLIENT,
        address      = "",
        createdAt    = 1000L,
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { authService.authState } returns flowOf(null)
        repository = AuthRepository(authService, userFirestoreService)
    }

    // ─── currentUser ──────────────────────────────────────────────────────────

    @Test
    fun `currentUser retorna null cuando no hay sesion iniciada`() {
        every { authService.currentUser } returns null

        assertNull(repository.currentUser)
    }

    @Test
    fun `currentUser retorna el usuario cuando hay sesion activa`() {
        every { authService.currentUser } returns firebaseUser

        assertNotNull(repository.currentUser)
        assertEquals(firebaseUser, repository.currentUser)
    }

    // ─── authState ────────────────────────────────────────────────────────────

    @Test
    fun `authState emite null cuando no hay sesion`() = runTest {
        every { authService.authState } returns flowOf(null)
        repository = AuthRepository(authService, userFirestoreService)

        val result = repository.authState.first()

        assertNull(result)
    }

    @Test
    fun `authState emite el usuario cuando hay sesion activa`() = runTest {
        every { authService.authState } returns flowOf(firebaseUser)
        repository = AuthRepository(authService, userFirestoreService)

        val result = repository.authState.first()

        assertEquals(firebaseUser, result)
    }

    // ─── getOrCreateUser — usuario existente ──────────────────────────────────

    @Test
    fun `getOrCreateUser retorna usuario existente de Firestore sin crear uno nuevo`() = runTest {
        every { firebaseUser.uid } returns "uid_test_001"
        coEvery { userFirestoreService.getUser("uid_test_001") } returns fakeUser

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals("uid_test_001", result.uid)
        assertEquals("Juan", result.firstName)
        assertEquals(UserRole.CLIENT, result.role)
        coVerify(exactly = 1) { userFirestoreService.getUser("uid_test_001") }
        coVerify(exactly = 0) { userFirestoreService.createUser(any()) }
    }

    // ─── getOrCreateUser — usuario nuevo ──────────────────────────────────────

    @Test
    fun `getOrCreateUser crea usuario nuevo si no existe en Firestore`() = runTest {
        every { firebaseUser.uid } returns "uid_test_002"
        every { firebaseUser.displayName } returns "María García"
        every { firebaseUser.phoneNumber } returns null
        every { firebaseUser.photoUrl } returns null
        coEvery { userFirestoreService.getUser("uid_test_002") } returns null
        coEvery { userFirestoreService.createUser(any()) } just Runs

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals("uid_test_002", result.uid)
        assertEquals("María", result.firstName)
        assertEquals("García", result.lastName)
        assertEquals(UserRole.CLIENT, result.role)
        coVerify(exactly = 1) { userFirestoreService.getUser("uid_test_002") }
        coVerify(exactly = 1) { userFirestoreService.createUser(match { it.uid == "uid_test_002" }) }
    }

    @Test
    fun `getOrCreateUser con displayName de una sola palabra deja lastName vacio`() = runTest {
        every { firebaseUser.uid } returns "uid_test_003"
        every { firebaseUser.displayName } returns "Carlos"
        every { firebaseUser.phoneNumber } returns null
        every { firebaseUser.photoUrl } returns null
        coEvery { userFirestoreService.getUser("uid_test_003") } returns null
        coEvery { userFirestoreService.createUser(any()) } just Runs

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals("Carlos", result.firstName)
        assertEquals("", result.lastName)
    }

    @Test
    fun `getOrCreateUser con displayName vacio deja firstName y lastName vacios`() = runTest {
        every { firebaseUser.uid } returns "uid_test_004"
        every { firebaseUser.displayName } returns ""
        every { firebaseUser.phoneNumber } returns null
        every { firebaseUser.photoUrl } returns null
        coEvery { userFirestoreService.getUser("uid_test_004") } returns null
        coEvery { userFirestoreService.createUser(any()) } just Runs

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals("", result.firstName)
        assertEquals("", result.lastName)
    }

    @Test
    fun `getOrCreateUser asigna foto de perfil cuando Firebase la provee`() = runTest {
        val photoUri = mockk<Uri>()
        every { photoUri.toString() } returns "https://photo.url/foto.jpg"
        every { firebaseUser.uid } returns "uid_test_005"
        every { firebaseUser.displayName } returns "Ana López"
        every { firebaseUser.phoneNumber } returns null
        every { firebaseUser.photoUrl } returns photoUri
        coEvery { userFirestoreService.getUser("uid_test_005") } returns null
        coEvery { userFirestoreService.createUser(any()) } just Runs

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals("https://photo.url/foto.jpg", result.profilePhoto)
    }

    @Test
    fun `getOrCreateUser asigna rol CLIENT por defecto al crear usuario nuevo`() = runTest {
        every { firebaseUser.uid } returns "uid_test_006"
        every { firebaseUser.displayName } returns "Test User"
        every { firebaseUser.phoneNumber } returns null
        every { firebaseUser.photoUrl } returns null
        coEvery { userFirestoreService.getUser("uid_test_006") } returns null
        coEvery { userFirestoreService.createUser(any()) } just Runs

        val result = repository.getOrCreateUser(firebaseUser)

        assertEquals(UserRole.CLIENT, result.role)
    }

    // ─── signOut ──────────────────────────────────────────────────────────────

    @Test
    fun `signOut llama a authService signOut`() {
        every { authService.signOut() } just Runs

        repository.signOut()

        verify(exactly = 1) { authService.signOut() }
    }
}