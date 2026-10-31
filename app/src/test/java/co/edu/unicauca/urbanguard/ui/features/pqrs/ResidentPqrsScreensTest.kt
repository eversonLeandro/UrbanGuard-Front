package co.edu.unicauca.urbanguard.ui.features.pqrs

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.SavedStateHandle
import co.edu.unicauca.urbanguard.domain.model.PqrsStatus
import co.edu.unicauca.urbanguard.domain.model.PqrsType
import co.edu.unicauca.urbanguard.domain.model.AppError
import co.edu.unicauca.urbanguard.domain.model.AppException
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsSummary
import co.edu.unicauca.urbanguard.domain.model.ResidentPqrsHistoryEntry
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.CreatePqrsUseCase
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsDetailUseCase
import co.edu.unicauca.urbanguard.domain.usecase.pqrs.GetOwnPqrsUseCase
import co.edu.unicauca.urbanguard.domain.model.UserRole
import co.edu.unicauca.urbanguard.ui.features.home.HomeScreen
import co.edu.unicauca.urbanguard.ui.features.home.HomeViewModel
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.CompletableDeferred

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ResidentPqrsScreensTest {
    @get:Rule val compose = createComposeRule()

    @Test fun `formulario muestra validaciones y tipos`() {
        val vm = CreatePqrsViewModel(SavedStateHandle(), CreatePqrsUseCase(FakeResidentRepo()), activeSession())
        compose.setContent { CreatePqrsScreen(onNavigateBack = {}, onCreated = {}, viewModel = vm) }
        vm.submit()
        compose.waitForIdle()
        assertTrue(vm.uiState.value.typeError != null)
        assertTrue(vm.uiState.value.subjectError != null)
        assertTrue(vm.uiState.value.descriptionError != null)
        compose.onNodeWithText("Revisa los campos marcados en el formulario.").assertIsDisplayed()
        compose.onNodeWithText("0/120").assertExists()
        compose.onNodeWithText("0/2000").assertExists()
        compose.onNodeWithContentDescription("Descripción").assertExists()
        compose.onNodeWithContentDescription("Enviar PQRS").assertIsDisplayed()
        compose.onNodeWithContentDescription("Tipo de PQRS: Seleccionar tipo").performClick()
        compose.onNodeWithText("Petición").assertIsDisplayed().performClick()
        assertTrue(vm.uiState.value.type == PqrsType.PETITION)
        compose.onNodeWithContentDescription("Tipo de PQRS: Petición").assertExists()
    }

    @Test fun `error de envio queda visible junto a la accion fija`() {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.ServerError(AppError.ServerError.MESSAGE)))
        }
        val vm = CreatePqrsViewModel(SavedStateHandle(), CreatePqrsUseCase(repo), activeSession())
        vm.onTypeChanged(PqrsType.PETITION)
        vm.onSubjectChanged("Iluminación del acceso")
        vm.onDescriptionChanged("La lámpara del acceso principal no enciende.")
        compose.setContent { CreatePqrsScreen(onNavigateBack = {}, onCreated = {}, viewModel = vm) }
        compose.onNodeWithContentDescription("Enviar PQRS").performClick()
        compose.waitUntil(10_000) { vm.uiState.value.submitError != null }
        compose.onNodeWithText(requireNotNull(vm.uiState.value.submitError)).assertIsDisplayed()
    }

    @Test fun `historial muestra resumen y abre detalle`() {
        val repo = FakeResidentRepo().apply {
            mine = Result.success(listOf(ResidentPqrsSummary(
                "p1", PqrsType.CLAIM, "Fuga de agua", PqrsStatus.REJECTED,
                "2026-10-30T12:00:00Z"
            )))
        }
        var opened = ""
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        compose.setContent {
            MyPqrsScreen(onNavigateBack = {}, onRadicar = {}, onOpenDetail = { opened = it }, viewModel = vm)
        }
        compose.waitUntil(10_000) { vm.uiState.value.items.isNotEmpty() }
        compose.onNodeWithText("Fuga de agua").assertIsDisplayed().performClick()
        assertTrue(opened == "p1")
    }

    @Test fun `detalle muestra descripcion y estado`() {
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")),
            GetOwnPqrsDetailUseCase(FakeResidentRepo()), activeSession()
        )
        compose.setContent { MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm) }
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onAllNodesWithText("Descripción").assertCountEquals(2)
        compose.onNodeWithText("Radicada").assertIsDisplayed()
        compose.onNodeWithText("Aún no hay actualizaciones. Aquí aparecerán los cambios de estado y las respuestas.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun `detalle espera respuesta antes de mostrar seguimiento`() {
        val gate = CompletableDeferred<Unit>()
        val repo = FakeResidentRepo().apply { detailGate = gate }
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")), GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        compose.setContent { MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm) }
        compose.waitForIdle()
        assertTrue(vm.uiState.value.isLoading)
        compose.onNodeWithText("Seguimiento").assertDoesNotExist()
        gate.complete(Unit)
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onNodeWithText("Seguimiento").performScrollTo().assertIsDisplayed()
    }

    @Test fun `detalle oculta historial al perder rol residente`() {
        val repo = FakeResidentRepo().apply { ownDetail = Result.success(detail().copy(
            history = listOf(ResidentPqrsHistoryEntry(
                "h1", PqrsStatus.SUBMITTED, PqrsStatus.IN_PROGRESS,
                "Respuesta privada", "2026-10-30T13:00:00Z"
            ))
        )) }
        val session = activeSession()
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")), GetOwnPqrsDetailUseCase(repo), session
        )
        compose.setContent { MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm) }
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onNodeWithText("Respuesta privada").performScrollTo().assertIsDisplayed()
        session.saveSession(requireNotNull(session.session.value).copy(role = UserRole.ADMIN))
        compose.waitUntil(10_000) { vm.uiState.value.error != null }
        compose.onNodeWithText("Respuesta privada").assertDoesNotExist()
        compose.onNodeWithText("Sin permisos").assertIsDisplayed()
    }

    @Test fun `detalle muestra cada transicion y respuesta en su propia tarjeta`() {
        val repo = FakeResidentRepo().apply {
            ownDetail = Result.success(detail().copy(
                status = PqrsStatus.RESOLVED,
                history = listOf(
                    ResidentPqrsHistoryEntry(
                        "h1", PqrsStatus.SUBMITTED, PqrsStatus.IN_PROGRESS,
                        "Estamos revisando el caso.", "2026-10-30T12:30:00Z"
                    ),
                    ResidentPqrsHistoryEntry(
                        "h2", PqrsStatus.IN_PROGRESS, PqrsStatus.IN_PROGRESS,
                        "Seguimos trabajando en el caso.", "2026-10-30T13:00:00Z"
                    ),
                    ResidentPqrsHistoryEntry(
                        "h3", PqrsStatus.IN_PROGRESS, PqrsStatus.RESOLVED,
                        "Se solucionó el problema.", "2026-10-30T14:00:00Z"
                    )
                )
            ))
        }
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")), GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        var redraw by mutableIntStateOf(0)
        compose.setContent {
            Column {
                Text("Vista $redraw")
                MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm)
            }
        }
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onNodeWithText("Seguimiento").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Actualización 1").performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText("Estado anterior").assertCountEquals(3)
        compose.onAllNodesWithText("Estado nuevo").assertCountEquals(3)
        compose.onNodeWithText("Estamos revisando el caso.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Actualización 2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seguimiento sin cambio de estado").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Seguimos trabajando en el caso.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Actualización 3").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Se solucionó el problema.").performScrollTo().assertIsDisplayed()
        compose.runOnIdle { redraw++ }
        compose.onNodeWithText("Vista 1").assertIsDisplayed()
        compose.onNodeWithText("Actualización 3").assertExists()
        compose.onNodeWithText("Aún no hay actualizaciones. Aquí aparecerán los cambios de estado y las respuestas.")
            .assertDoesNotExist()
    }

    @Test fun `transicion sigue legible con texto grande`() {
        val repo = FakeResidentRepo().apply {
            ownDetail = Result.success(detail().copy(history = listOf(
                ResidentPqrsHistoryEntry(
                    "h1", PqrsStatus.IN_REVIEW, PqrsStatus.IN_PROGRESS,
                    "La administración está revisando el caso.", "2026-10-30T13:00:00Z"
                )
            )))
        }
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "pqrs-1")), GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm)
            }
        }
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onNodeWithText("Estado anterior").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Estado nuevo").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("La administración está revisando el caso.")
            .performScrollTo().assertIsDisplayed()
    }

    @Test fun `historial vacio invita a radicar`() {
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(FakeResidentRepo()), activeSession())
        var radicar = 0
        compose.setContent {
            MyPqrsScreen(onNavigateBack = {}, onRadicar = { radicar++ }, onOpenDetail = {}, viewModel = vm)
        }
        compose.waitUntil(10_000) { vm.uiState.value.isGeneralEmpty }
        compose.onNodeWithText("No tienes PQRS radicadas actualmente.").assertIsDisplayed()
        compose.onNodeWithText("Radicar PQRS").assertIsDisplayed()
        val filterBottom = compose.onNodeWithText("Filtrar solicitudes")
            .fetchSemanticsNode().boundsInRoot.bottom
        val actionTop = compose.onNodeWithText("Radicar PQRS")
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue(actionTop > filterBottom)
        compose.onNodeWithText("Radicar PQRS").performClick()
        assertTrue(radicar == 1)
    }

    @Test fun `historial filtrado vacio permite limpiar filtros`() {
        val repo = FakeResidentRepo().apply {
            unfilteredMine = Result.success(listOf(ResidentPqrsSummary(
                "p1", PqrsType.CLAIM, "Fuga", PqrsStatus.SUBMITTED, "2026-10-30T12:00:00Z"
            )))
        }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        compose.setContent {
            MyPqrsScreen(onNavigateBack = {}, onRadicar = {}, onOpenDetail = {}, viewModel = vm)
        }
        compose.waitUntil(10_000) { !vm.uiState.value.isLoading }
        compose.runOnIdle { vm.onStatusSelected(PqrsStatus.CLOSED) }
        compose.waitUntil(10_000) { vm.uiState.value.isFilteredEmpty }
        compose.onNodeWithText("No hay PQRS que coincidan con los filtros seleccionados.")
            .assertIsDisplayed()
    }

    @Test fun `historial muestra error de permisos y reintento`() {
        val repo = FakeResidentRepo().apply {
            mine = Result.failure(AppException(AppError.Forbidden(AppError.Forbidden.MESSAGE)))
        }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        compose.setContent {
            MyPqrsScreen(onNavigateBack = {}, onRadicar = {}, onOpenDetail = {}, viewModel = vm)
        }
        compose.waitUntil(10_000) { vm.uiState.value.error != null }
        compose.onNodeWithText("Sin permisos").assertIsDisplayed()
        compose.onNodeWithText("Reintentar").performScrollTo().assertIsDisplayed()
    }

    @Test fun `reintento sigue accesible con texto grande y mensaje largo`() {
        val message = "No fue posible consultar las solicitudes en este momento. ".repeat(8)
        val repo = FakeResidentRepo().apply {
            mine = Result.failure(AppException(AppError.Forbidden(message)))
        }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.8f)) {
                MyPqrsScreen(onNavigateBack = {}, onRadicar = {}, onOpenDetail = {}, viewModel = vm)
            }
        }
        compose.waitUntil(10_000) { vm.uiState.value.error != null }
        compose.onNodeWithText("Reintentar").performScrollTo().assertIsDisplayed()
    }

    @Test fun `detalle inexistente muestra mensaje propio`() {
        val repo = FakeResidentRepo().apply {
            ownDetail = Result.failure(AppException(AppError.NotFound(AppError.NotFound.MESSAGE)))
        }
        val vm = MyPqrsDetailViewModel(
            SavedStateHandle(mapOf("pqrsId" to "absent")), GetOwnPqrsDetailUseCase(repo), activeSession()
        )
        compose.setContent { MyPqrsDetailScreen(onNavigateBack = {}, viewModel = vm) }
        compose.waitUntil(10_000) { vm.uiState.value.error != null }
        compose.onNodeWithText("No encontramos esta PQRS.").assertIsDisplayed()
        repo.ownDetail = Result.success(detail())
        compose.onNodeWithText("Reintentar").performClick()
        compose.waitUntil(10_000) { vm.uiState.value.detail != null }
        compose.onNodeWithText("Seguimiento").performScrollTo().assertIsDisplayed()
    }

    @Test fun `selectores envian estado y tipo al historial`() {
        val repo = FakeResidentRepo().apply {
            mine = Result.success(listOf(ResidentPqrsSummary(
                "p1", PqrsType.PETITION, "Ruido", PqrsStatus.SUBMITTED,
                "2026-10-30T12:00:00Z"
            )))
        }
        val vm = MyPqrsViewModel(GetOwnPqrsUseCase(repo), activeSession())
        compose.setContent {
            MyPqrsScreen(onNavigateBack = {}, onRadicar = {}, onOpenDetail = {}, viewModel = vm)
        }
        compose.waitUntil(10_000) { vm.uiState.value.items.isNotEmpty() }
        val filterTop = compose.onNodeWithContentDescription("Estado: Todos")
            .fetchSemanticsNode().boundsInRoot.top
        compose.onNodeWithContentDescription("Estado: Todos").performClick()
        compose.onNodeWithText("Cerrada").performClick()
        compose.waitUntil(10_000) { vm.uiState.value.statusFilter == PqrsStatus.CLOSED }
        assertEquals(
            filterTop,
            compose.onNodeWithContentDescription("Estado: Cerrada").fetchSemanticsNode().boundsInRoot.top,
            1f
        )
        compose.onNodeWithContentDescription("Tipo: Todos").performClick()
        compose.onNodeWithText("Queja").performClick()
        compose.waitUntil(10_000) { vm.uiState.value.typeFilter == PqrsType.COMPLAINT }
        assertEquals(
            filterTop,
            compose.onNodeWithContentDescription("Estado: Cerrada").fetchSemanticsNode().boundsInRoot.top,
            1f
        )
        assertTrue(repo.queries.any { it.second == PqrsStatus.CLOSED && it.third == PqrsType.COMPLAINT })
    }

    @Test fun `conflicto conserva formulario y ofrece intento nuevo`() {
        val repo = FakeResidentRepo().apply {
            nextResult = Result.failure(AppException(AppError.Conflict(AppError.Conflict.MESSAGE)))
        }
        val vm = CreatePqrsViewModel(SavedStateHandle(), CreatePqrsUseCase(repo), activeSession())
        vm.onTypeChanged(PqrsType.PETITION)
        vm.onSubjectChanged("Asunto")
        vm.onDescriptionChanged("Descripción")
        compose.setContent { CreatePqrsScreen(onNavigateBack = {}, onCreated = {}, viewModel = vm) }
        compose.runOnIdle { vm.submit() }
        compose.waitUntil(10_000) { vm.uiState.value.requiresNewAttempt }
        compose.onNodeWithText("Iniciar nuevo intento").assertExists()
        assertTrue(vm.uiState.value.subject == "Asunto")
    }

    @Test fun `tarjeta Home residente abre Mis PQRS y no bandeja admin`() {
        val session = activeSession()
        val vm = HomeViewModel(session)
        var residentOpens = 0
        var adminOpens = 0
        compose.setContent {
            HomeScreen(
                onOpenMyPqrs = { residentOpens++ },
                onOpenPqrsInbox = { adminOpens++ },
                viewModel = vm
            )
        }
        compose.waitUntil(10_000) { vm.uiState.value.role == UserRole.RESIDENT }
        compose.onNodeWithText("PQRS & Reportes").performScrollTo().performClick()
        assertTrue(residentOpens == 1)
        assertTrue(adminOpens == 0)
    }

    @Test fun `tarjeta Home admin conserva acceso a su bandeja`() {
        val session = activeSession()
        session.saveSession(requireNotNull(session.session.value).copy(role = UserRole.ADMIN))
        val vm = HomeViewModel(session)
        var residentOpens = 0
        var adminOpens = 0
        compose.setContent {
            HomeScreen(
                onOpenMyPqrs = { residentOpens++ },
                onOpenPqrsInbox = { adminOpens++ },
                viewModel = vm
            )
        }
        compose.waitUntil(10_000) { vm.uiState.value.role == UserRole.ADMIN }
        compose.onNodeWithText("PQRS & Reportes").performScrollTo().performClick()
        assertTrue(adminOpens == 1)
        assertTrue(residentOpens == 0)
    }
}
