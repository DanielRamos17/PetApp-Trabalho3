package com.example.petapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ContaScreen(dados: DadosApp, onMeusPets: () -> Unit, onMinhaAgenda: () -> Unit, onEditarPerfil: () -> Unit, onSair: () -> Unit) {
    val scope = rememberCoroutineScope()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var totalPets by remember { mutableIntStateOf(0) }
    var pendentes by remember { mutableIntStateOf(0) }
    var concluidos by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val idLogado = dados.prefs.getInt("usuario_logado_id", -1)
            val u = dados.db.usuarioDao().buscarPorId(idLogado)
            val listaPets = dados.db.petDao().buscarTodos()
            val listaAgs = dados.db.agendamentoDao().buscarTodos()
            withContext(Dispatchers.Main) { usuario = u; totalPets = listaPets.size; pendentes = listaAgs.count { !it.concluido }; concluidos = listaAgs.count { it.concluido } }
        }
    }

    val primeiroNome = usuario?.nome?.substringBefore(" ") ?: "Usuário"

    Column(modifier = Modifier.fillMaxSize().background(FundoBege).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Minha conta", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(20.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoPerfil(uri = usuario?.fotoUri ?: "", modifier = Modifier.size(56.dp), fallbackIcon = Icons.Default.Person)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(primeiroNome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Text(usuario?.email ?: "Cliente PetApp", color = Color.Gray, fontSize = 13.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Estatistica("Pets", totalPets.toString(), Modifier.weight(1f))
            Estatistica("Pendentes", pendentes.toString(), Modifier.weight(1f))
            Estatistica("Concluídos", concluidos.toString(), Modifier.weight(1f))
        }
        ItemMenu("Editar Perfil", Icons.Default.Person, onEditarPerfil)
        ItemMenu("Meus pets", Icons.Default.Pets, onMeusPets)
        ItemMenu("Minha agenda", Icons.Default.DateRange, onMinhaAgenda)
        Button(onClick = onSair, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = VermelhoAlerta), shape = RoundedCornerShape(16.dp)) { Text("Sair", fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun Estatistica(rotulo: String, valor: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.background(Color.White, RoundedCornerShape(16.dp)).padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(valor, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = VerdePrincipal); Text(rotulo, fontSize = 12.sp, color = Color.Gray) }
}
@Composable
private fun ItemMenu(titulo: String, icone: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icone, contentDescription = null, tint = VerdePrincipal)
        Spacer(modifier = Modifier.width(16.dp))
        Text(titulo, fontWeight = FontWeight.SemiBold, color = Color.Black, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
    }
}