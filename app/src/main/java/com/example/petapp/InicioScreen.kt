@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

private val coresServico = listOf(Color(0xFFE2F4EC), Color(0xFFFFEBEA), Color(0xFFFFF3DE), Color(0xFFF2E8FF), Color(0xFFE0F6F5), Color(0xFFFFEBE0))

fun formatarDataNotificacao(dataStr: String, horaStr: String): String {
    try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"))
        val dataItem = sdf.parse(dataStr) ?: return "$dataStr às $horaStr"
        val calItem = Calendar.getInstance().apply { time = dataItem }
        val calHoje = Calendar.getInstance()
        calItem.set(Calendar.HOUR_OF_DAY, 0); calItem.set(Calendar.MINUTE, 0); calItem.set(Calendar.SECOND, 0); calItem.set(Calendar.MILLISECOND, 0)
        calHoje.set(Calendar.HOUR_OF_DAY, 0); calHoje.set(Calendar.MINUTE, 0); calHoje.set(Calendar.SECOND, 0); calHoje.set(Calendar.MILLISECOND, 0)

        return when (TimeUnit.MILLISECONDS.toDays(calItem.timeInMillis - calHoje.timeInMillis)) {
            0L -> "Hoje às $horaStr"
            -1L -> "Ontem às $horaStr"
            -2L -> "Anteontem às $horaStr"
            else -> "$dataStr às $horaStr"
        }
    } catch (e: Exception) { return "$dataStr às $horaStr" }
}

@Composable
fun InicioScreen(dados: DadosApp, onServicoClick: (String) -> Unit, onPetClick: (Int) -> Unit, onPetsClick: () -> Unit, onAgendaClick: () -> Unit, onAgendamentoClick: (Int) -> Unit, onContaClick: () -> Unit) {
    val scope = rememberCoroutineScope()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var agendamentos by remember { mutableStateOf<List<Agendamento>>(emptyList()) }


    var petAtualId by rememberSaveable { mutableIntStateOf(-1) }

    var mostrarNotificacoes by remember { mutableStateOf(false) }
    var mostrarListaPets by remember { mutableStateOf(false) } // Mostra o menu de pets

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val idLogado = dados.prefs.getInt("usuario_logado_id", -1)
            val u = dados.db.usuarioDao().buscarPorId(idLogado)
            val listaPets = dados.db.petDao().buscarTodos()
            val listaAgendamentos = dados.db.agendamentoDao().buscarTodos()
            withContext(Dispatchers.Main) {
                usuario = u; pets = listaPets; agendamentos = listaAgendamentos
                // Se for a primeira vez que abre, seleciona logo o primeiro pet da lista
                if (petAtualId == -1 && listaPets.isNotEmpty()) {
                    petAtualId = listaPets.first().id
                }
            }
        }
    }

    val pendentes = agendamentos.count { !it.concluido }
    val pet = pets.find { it.id == petAtualId } ?: pets.firstOrNull()
    val primeiroNome = usuario?.nome?.substringBefore(" ") ?: "Usuário"


    if (mostrarNotificacoes) {
        ModalBottomSheet(onDismissRequest = { mostrarNotificacoes = false }, sheetState = sheetState, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Notificações", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(24.dp))
                if (agendamentos.isEmpty()) {
                    Text("Sem notificações", color = Color.Gray, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(32.dp))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        items(agendamentos.reversed(), key = { it.id }) { ag ->
                            val nomePetNotificacao = pets.find { it.id == ag.petId }?.nome ?: "Pet"
                            val servicoPrincipal = ag.servico.split(",").firstOrNull()?.trim() ?: ""
                            Row(modifier = Modifier.fillMaxWidth().background(FundoBege, RoundedCornerShape(16.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(42.dp).background(VerdeClaro, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = null, tint = VerdePrincipal, modifier = Modifier.size(20.dp)) }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text("Agendamento de $servicoPrincipal para ${nomePetNotificacao}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                                    Text(formatarDataNotificacao(ag.data, ag.horario), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    if (mostrarListaPets) {
        ModalBottomSheet(onDismissRequest = { mostrarListaPets = false }, containerColor = Color.White) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Text("Escolha o pet", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(pets, key = { it.id }) { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (p.id == pet?.id) VerdeClaro else FundoBege, RoundedCornerShape(12.dp))
                                .clickable { petAtualId = p.id; mostrarListaPets = false }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FotoPerfil(uri = p.fotoUri, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(p.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.clickable { onContaClick() }) {
                    FotoPerfil(uri = usuario?.fotoUri ?: "", modifier = Modifier.size(50.dp), fallbackIcon = Icons.Default.Person)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Olá,", fontSize = 14.sp, color = Color.Gray)
                    Text(primeiroNome, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
            Box(modifier = Modifier.size(46.dp)) {
                Box(modifier = Modifier.size(42.dp).align(Alignment.BottomStart).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFE8E8E8), CircleShape).clickable { mostrarNotificacoes = true }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Notifications, contentDescription = "Ver notificações", tint = Color.Black, modifier = Modifier.size(20.dp)) }
                if (pendentes > 0) Box(modifier = Modifier.size(18.dp).align(Alignment.TopEnd).background(VermelhoAlerta, CircleShape), contentAlignment = Alignment.Center) { Text("$pendentes", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(VerdePrincipal).clickable { if (pet != null) onPetClick(pet.id) else onPetsClick() }.padding(20.dp)) {
            if (pet == null) Text("Nenhum pet cadastrado. Toque para cadastrar.", color = Color.White, fontWeight = FontWeight.Bold)
            else {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FotoPerfil(uri = pet.fotoUri, modifier = Modifier.size(52.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(pet.nome, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("${pet.sexo} • ${pet.idadeAnos} ano(s) • ${pet.pesoKg.comoKg()}", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                            }
                        }


                        if (pets.size > 1) {
                            Row(modifier = Modifier.clip(CircleShape).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape).clickable {
                                if (pets.size == 2) {
                                    // Se tem apenas 2, troca direto para o outro!
                                    val outroPet = pets.find { it.id != pet.id }
                                    if (outroPet != null) petAtualId = outroPet.id
                                } else {

                                    mostrarListaPets = true
                                }
                            }.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("Trocar", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row {
                        val pendentesDestePet = agendamentos.count { !it.concluido && it.petId == pet.id }
                        Row(modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(6.dp)); Text("$pendentesDestePet pendente(s)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(modifier = Modifier.background(Color.White.copy(alpha = 0.15f), CircleShape).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("Porte ${pet.porte()}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Serviços", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Ver agenda", color = VerdePrincipal, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onAgendaClick() })
        }
        Spacer(modifier = Modifier.height(16.dp))
        Servicos.precosBase.entries.toList().chunked(3).forEachIndexed { linha, grupo ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                grupo.forEachIndexed { i, entrada -> ServiceCard(title = entrada.key, price = "a partir R$ ${entrada.value.toInt()}", icon = EstiloApp.iconeServico(entrada.key), bgColor = coresServico[(linha * 3 + i) % coresServico.size], modifier = Modifier.weight(1f), onClick = { onServicoClick(entrada.key) }) }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ServiceCard(title: String, price: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).border(1.dp, Color(0xFFF7F7F7), RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(52.dp).background(bgColor, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, tint = VerdePrincipal, modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(2.dp))
        Text(price, color = Color(0xFF8A8A8A), fontSize = 10.sp)
    }
}