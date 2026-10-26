@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DetalheAgendamentoScreen(agendamentoId: Int, dados: DadosApp, onVoltar: () -> Unit, onPetClick: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var ag by remember { mutableStateOf<Agendamento?>(null) }
    var pet by remember { mutableStateOf<Pet?>(null) }

    fun carregarDetalhe() {
        scope.launch(Dispatchers.IO) {
            try {
                val a = dados.db.agendamentoDao().buscarPorId(agendamentoId)
                val p = if (a != null) dados.db.petDao().buscarPorId(a.petId) else null
                withContext(Dispatchers.Main) { ag = a; pet = p }
            } catch (e: Exception) { Log.e("DetalheAgendamento", "Erro", e) }
        }
    }
    LaunchedEffect(agendamentoId) { carregarDetalhe() }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        TopAppBar(title = { Text("Detalhes do agendamento", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = FundoBege))
        val a = ag
        if (a == null) {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) { Text("Buscando agendamento...", color = Color.Gray); Spacer(modifier = Modifier.height(16.dp)); Button(onClick = onVoltar, colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal)) { Text("Voltar") } }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Extrai o serviço principal (o primeiro da lista) apenas para o Ícone
                val servicoPrincipal = a.servico.split(",").firstOrNull()?.trim() ?: ""

                Row(modifier = Modifier.fillMaxWidth().background(VerdePrincipal, RoundedCornerShape(24.dp)).padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(EstiloApp.iconeServico(servicoPrincipal), contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(a.servico, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                        Text(if (a.concluido) "Concluído" else "Pendente", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                }

                SecaoDetalheAgendamento("Quando") {
                    LinhaTextoAgendamento("Data", a.data)
                    LinhaTextoAgendamento("Horário", a.horario)
                    if (a.observacoes.isNotBlank()) LinhaTextoAgendamento("Observações", a.observacoes)
                }

                val p = pet
                SecaoDetalheAgendamento("Pet") {
                    if (p == null) { Text("Este pet foi removido.", color = Color.Gray, fontSize = 13.sp) } else {
                        Row(modifier = Modifier.fillMaxWidth().background(VerdeClaro, RoundedCornerShape(12.dp)).clickable { onPetClick(p.id) }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            FotoPerfil(uri = p.fotoUri, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) { Text(p.nome, fontWeight = FontWeight.Bold, color = Color.Black); Text("${p.raca} • ${p.pesoKg.comoKg()} • porte ${p.porte()}", fontSize = 12.sp, color = Color.Gray) }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver pet", tint = Color.DarkGray)
                        }
                    }
                }

                if (p != null) {
                    SecaoDetalheAgendamento("Valor estimado") {
                        // Lógica para somar múltiplos serviços
                        val listaServicos = a.servico.split(",").map { it.trim() }
                        val precoBaseTotal = listaServicos.sumOf { Servicos.precosBase[it] ?: 0.0 }
                        val totalFinal = precoBaseTotal * p.fatorPreco()

                        LinhaTextoAgendamento("Preço base", precoBaseTotal.comoReais())
                        LinhaTextoAgendamento("Ajuste por porte (${p.porte()})", "x${p.fatorPreco().toString().replace('.', ',')}")
                        LinhaTextoAgendamento("Total", totalFinal.comoReais())
                    }
                }

                Button(onClick = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().atualizar(a.copy(concluido = !a.concluido)); withContext(Dispatchers.Main) { carregarDetalhe() } } catch (e: Exception) { Log.e("DetalheAgendamento", "Erro", e) } } }, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(16.dp)) { Text(if (a.concluido) "Reabrir agendamento" else "Marcar como concluído", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().deletar(a); withContext(Dispatchers.Main) { onVoltar() } } catch (e: Exception) { Log.e("DetalheAgendamento", "Erro", e) } } }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp)) { Text("Cancelar agendamento", color = VermelhoAlerta, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Nomes totalmente exclusivos para evitar qualquer conflito com o DetalhePetScreen
@Composable
private fun SecaoDetalheAgendamento(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            conteudo()
        }
    }
}

@Composable
private fun LinhaTextoAgendamento(rotulo: String, valor: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(rotulo, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(valor, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}