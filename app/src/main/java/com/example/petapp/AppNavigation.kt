package com.example.petapp

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private data class Aba(val rota: String, val rotulo: String, val icone: ImageVector)
private val abas = listOf(
    Aba(Rotas.INICIO, "Início", Icons.Default.Home),
    Aba(Rotas.PETS, "Pets", Icons.Default.Pets),
    Aba(Rotas.AGENDA, "Agenda", Icons.Default.DateRange),
    Aba(Rotas.CONTA, "Conta", Icons.Default.Person)
)

fun NavController.irParaAba(rota: String) { navigate(rota) { popUpTo(Rotas.INICIO); launchSingleTop = true } }

@Composable
fun PetAppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val dados = remember { DadosApp(context) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val rotaAtual = backStackEntry?.destination?.route
    val mostrarBarra = rotaAtual != null && rotaAtual != Rotas.LOGIN && rotaAtual != Rotas.CADASTRO

    val abaSelecionada = when (rotaAtual) {
        Rotas.DETALHE_PET -> Rotas.PETS
        Rotas.DETALHE_AGENDAMENTO -> Rotas.AGENDA
        Rotas.EDITAR_PERFIL -> Rotas.CONTA
        else -> rotaAtual
    }

    LaunchedEffect(Unit) { launch(Dispatchers.IO) { dados.inicializarPetshopsFalsos() } }

    Scaffold(
        bottomBar = {
            if (mostrarBarra) {
                NavigationBar(containerColor = Color.White) {
                    abas.forEach { aba ->
                        NavigationBarItem(
                            selected = abaSelecionada == aba.rota, onClick = { navController.irParaAba(aba.rota) },
                            icon = { Icon(aba.icone, contentDescription = aba.rotulo) }, label = { Text(aba.rotulo, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = VerdePrincipal, selectedTextColor = VerdePrincipal, indicatorColor = VerdeClaro, unselectedIconColor = Color.Gray, unselectedTextColor = Color.Gray)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Rotas.LOGIN, modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
            composable(Rotas.LOGIN) { LoginScreen(dados = dados, onNavigateToCadastro = { navController.navigate(Rotas.CADASTRO) }, onNavigateToHome = { navController.navigate(Rotas.INICIO) { popUpTo(Rotas.LOGIN) { inclusive = true } } }) }
            composable(Rotas.CADASTRO) { CadastroScreen(dados = dados, onNavigateBackToLogin = { navController.popBackStack() }) }
            composable(Rotas.INICIO) { InicioScreen(dados = dados, onServicoClick = { servico -> dados.servicoPreSelecionado = servico; navController.irParaAba(Rotas.AGENDA) }, onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) }, onPetsClick = { navController.irParaAba(Rotas.PETS) }, onAgendaClick = { navController.irParaAba(Rotas.AGENDA) }, onAgendamentoClick = { id -> navController.navigate(Rotas.detalheAgendamento(id)) }, onContaClick = { navController.irParaAba(Rotas.CONTA) }) }

            composable(Rotas.PETS) { PetsScreen(dados = dados, onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) }) }
            composable(Rotas.AGENDA) { AgendaScreen(dados = dados, onAgendamentoClick = { id -> navController.navigate(Rotas.detalheAgendamento(id)) }) }
            composable(Rotas.CONTA) { ContaScreen(dados = dados, onMeusPets = { navController.irParaAba(Rotas.PETS) }, onMinhaAgenda = { navController.irParaAba(Rotas.AGENDA) }, onEditarPerfil = { navController.navigate(Rotas.EDITAR_PERFIL) }, onSair = { dados.prefs.edit().remove("usuario_logado_id").apply(); navController.navigate(Rotas.LOGIN) { popUpTo(Rotas.INICIO) { inclusive = true } } }) }
            composable(Rotas.EDITAR_PERFIL) { EditarPerfilScreen(dados = dados, onVoltar = { navController.popBackStack() }) }

            composable(route = Rotas.DETALHE_PET, arguments = listOf(navArgument("petId") { type = NavType.IntType })) { entry ->
                DetalhePetScreen(petId = entry.arguments?.getInt("petId") ?: -1, dados = dados, onVoltar = { navController.popBackStack() }, onAgendamentoClick = { id -> navController.navigate(Rotas.detalheAgendamento(id)) }, onAgendarClick = { petId -> dados.petPreSelecionado = petId; navController.irParaAba(Rotas.AGENDA) })
            }
            composable(route = Rotas.DETALHE_AGENDAMENTO, arguments = listOf(navArgument("agendamentoId") { type = NavType.IntType })) { entry ->
                DetalheAgendamentoScreen(agendamentoId = entry.arguments?.getInt("agendamentoId") ?: -1, dados = dados, onVoltar = { navController.popBackStack() }, onPetClick = { id -> navController.navigate(Rotas.detalhePet(id)) })
            }
        }
    }
}