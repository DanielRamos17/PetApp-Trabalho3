@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val regexData = Regex("""^\d{2}/\d{2}/\d{4}$""")
private val regexHora = Regex("""^([01]\d|2[0-3]):[0-5]\d$""")

@Composable
fun AgendaScreen(dados: DadosApp, onAgendamentoClick: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var petshops by remember { mutableStateOf<List<Petshop>>(emptyList()) }
    var agendamentos by remember { mutableStateOf<List<Agendamento>>(emptyList()) }

    var petId by rememberSaveable { mutableIntStateOf(-1) }


    var servicosSelecionados by remember { mutableStateOf(setOf<String>()) }

    var data by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var obs by rememberSaveable { mutableStateOf("") }

    var cepBusca by rememberSaveable { mutableStateOf("") }
    var pesquisaServico by rememberSaveable { mutableStateOf("") }

    var erro by rememberSaveable { mutableStateOf("") }

    var abaSelecionada by remember { mutableIntStateOf(0) }
    val titulosAbas = listOf("Agendar", "Agendados", "Realizados")

    var petshopExpandido by rememberSaveable { mutableIntStateOf(-1) }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calHoje = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                return utcTimeMillis >= calHoje.timeInMillis
            }
        }
    )
    val calAtual = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(initialHour = calAtual.get(Calendar.HOUR_OF_DAY), initialMinute = calAtual.get(Calendar.MINUTE), is24Hour = true)

    fun carregarDados() {
        scope.launch(Dispatchers.IO) {
            try {
                val idLogado = dados.prefs.getInt("usuario_logado_id", -1)
                val u = dados.db.usuarioDao().buscarPorId(idLogado)
                val listaPets = dados.db.petDao().buscarTodos()
                val listaShops = dados.db.petshopDao().buscarTodos()
                val listaAgs = dados.db.agendamentoDao().buscarTodos()
                withContext(Dispatchers.Main) {
                    usuario = u; pets = listaPets; petshops = listaShops; agendamentos = listaAgs
                    if (petId == -1) petId = if (dados.petPreSelecionado > 0) dados.petPreSelecionado else listaPets.firstOrNull()?.id ?: -1
                    if (cepBusca.isEmpty() && !u?.cep.isNullOrBlank()) cepBusca = u!!.cep
                }
            } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) }
        }
    }

    LaunchedEffect(Unit) {

        if (dados.servicoPreSelecionado.isNotBlank()) {
            servicosSelecionados = setOf(dados.servicoPreSelecionado)
            dados.servicoPreSelecionado = ""
        } else if (servicosSelecionados.isEmpty()) {
            servicosSelecionados = setOf("Banho")
        }
        dados.petPreSelecionado = -1
        carregarDados()
    }

    val petAtual = pets.find { it.id == petId } ?: pets.firstOrNull()
    val agendamentosPendentes = agendamentos.filter { !it.concluido }
    val agendamentosConcluidos = agendamentos.filter { it.concluido }

    if (mostrarDatePicker) {
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { millis -> val formatter = SimpleDateFormat("ddMMyyyy", Locale.forLanguageTag("pt-BR")); formatter.timeZone = TimeZone.getTimeZone("UTC"); data = formatter.format(Date(millis)) }; mostrarDatePicker = false }) { Text("Confirmar", color = VerdePrincipal) } },
            dismissButton = { TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar", color = Color.Gray) } }
        ) { DatePicker(state = datePickerState) }
    }
    if (mostrarTimePicker) {
        AlertDialog(
            onDismissRequest = { mostrarTimePicker = false },
            confirmButton = { TextButton(onClick = { horario = String.format(Locale.forLanguageTag("pt-BR"), "%02d%02d", timePickerState.hour, timePickerState.minute); mostrarTimePicker = false }) { Text("Confirmar", color = VerdePrincipal) } },
            dismissButton = { TextButton(onClick = { mostrarTimePicker = false }) { Text("Cancelar", color = Color.Gray) } },
            text = { Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) { TimePicker(state = timePickerState) } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        Column(modifier = Modifier.background(Color.White).padding(top = 24.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                titulosAbas.forEachIndexed { index, titulo ->
                    val selecionado = abaSelecionada == index
                    Column(
                        modifier = Modifier.weight(1f).clickable { abaSelecionada = index; petshopExpandido = -1 }.padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = titulo, fontWeight = FontWeight.Bold, color = if (selecionado) VerdePrincipal else Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(if (selecionado) VerdePrincipal else Color.Transparent))
                    }
                }
            }
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            if (abaSelecionada == 0) {

                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cepBusca,
                            onValueChange = { val apenasNumeros = it.filter { c -> c.isDigit() }; if (apenasNumeros.length <= 8) cepBusca = apenasNumeros },
                            placeholder = { Text("Informar CEP", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp)) },
                            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            visualTransformation = CepVisualTransformationMask(),
                            shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.45f).height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray, focusedBorderColor = VerdePrincipal, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                        )
                        OutlinedTextField(
                            value = pesquisaServico, onValueChange = { pesquisaServico = it },
                            placeholder = { Text("O que seu pet precisa?", fontSize = 11.sp) },
                            trailingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                            singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(0.55f).height(50.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.LightGray, focusedBorderColor = VerdePrincipal, unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Petshops perto de si", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                }

                val petshopsFiltrados = if (pesquisaServico.isBlank()) petshops else petshops.filter { it.servicosText.contains(pesquisaServico, ignoreCase = true) }

                items(petshopsFiltrados) { ps ->
                    val expandido = petshopExpandido == ps.id
                    Card(modifier = Modifier.fillMaxWidth().clickable { petshopExpandido = if(expandido) -1 else ps.id }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(ps.nome, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${ps.avaliacao}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("A ${ps.distanciaMock} da sua localização", color = VerdePrincipal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Profissionais: ${ps.profissionais}", color = Color.DarkGray, fontSize = 13.sp)
                            Text("Serviços: ${ps.servicosText}", color = Color.Gray, fontSize = 12.sp)

                            if (expandido) {
                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = FundoBege)
                                Spacer(modifier = Modifier.height(16.dp))

                                if (pets.isEmpty()) {
                                    Text("Cadastre um pet na aba Pets antes de agendar.", color = Color.Gray, fontSize = 13.sp)
                                } else {
                                    Text("Pet", fontSize = 12.sp, color = Color.Gray)
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { pets.forEach { p -> FilterChip(selected = petAtual?.id == p.id, onClick = { petId = p.id }, label = { Text(p.nome) }) } }


                                    Text("Serviços", fontSize = 12.sp, color = Color.Gray)
                                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Servicos.precosBase.keys.forEach { s ->
                                            FilterChip(
                                                selected = servicosSelecionados.contains(s),
                                                onClick = {
                                                    servicosSelecionados = if (servicosSelecionados.contains(s)) servicosSelecionados - s else servicosSelecionados + s
                                                },
                                                label = { Text(s) },
                                                leadingIcon = { Icon(EstiloApp.iconeServico(s), contentDescription = null, modifier = Modifier.size(18.dp)) }
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(value = data, onValueChange = { data = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("Data") }, placeholder = { Text("dd/mm/aaaa") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = DataVisualTransformation(), trailingIcon = { IconButton(onClick = { mostrarDatePicker = true }) { Icon(Icons.Rounded.CalendarToday, contentDescription = null, tint = VerdePrincipal) } }, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                                        OutlinedTextField(value = horario, onValueChange = { horario = it.filter { c -> c.isDigit() }.take(4) }, label = { Text("Horário") }, placeholder = { Text("hh:mm") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = HoraVisualTransformation(), trailingIcon = { IconButton(onClick = { mostrarTimePicker = true }) { Icon(Icons.Rounded.Schedule, contentDescription = null, tint = VerdePrincipal) } }, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                                    }

                                    OutlinedTextField(value = obs, onValueChange = { obs = it }, label = { Text("Observações (opcional)") }, minLines = 2, maxLines = 4, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                                    if (erro.isNotEmpty()) Text(erro, color = VermelhoAlerta, fontSize = 12.sp)

                                    Button(
                                        onClick = {
                                            val dataFormatada = if (data.length == 8) "${data.substring(0,2)}/${data.substring(2,4)}/${data.substring(4,8)}" else data
                                            val horarioFormatado = if (horario.length == 4) "${horario.substring(0,2)}:${horario.substring(2,4)}" else horario

                                            if (petAtual == null) erro = "Escolha um pet."
                                            else if (servicosSelecionados.isEmpty()) erro = "Escolha pelo menos um serviço."
                                            else if (!regexData.matches(dataFormatada)) erro = "Data inválida."
                                            else if (!regexHora.matches(horarioFormatado)) erro = "Horário inválido."
                                            else {
                                                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("pt-BR")); sdf.isLenient = false
                                                try {
                                                    val agendamentoData = sdf.parse("$dataFormatada $horarioFormatado")
                                                    if (agendamentoData != null && agendamentoData.before(Calendar.getInstance().time)) erro = "Horário já passou!"
                                                    else {
                                                        scope.launch(Dispatchers.IO) {
                                                            try {

                                                                val servicosString = servicosSelecionados.joinToString(", ")

                                                                dados.db.agendamentoDao().inserir(Agendamento(petId = petAtual.id, petshopId = ps.id, servico = servicosString, data = dataFormatada, horario = horarioFormatado, observacoes = obs.trim()))
                                                                withContext(Dispatchers.Main) {
                                                                    data = ""; horario = ""; obs = ""; erro = ""; petshopExpandido = -1; abaSelecionada = 1
                                                                    servicosSelecionados = emptySet() // Limpa a lista
                                                                    carregarDados()
                                                                }
                                                            } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) }
                                                        }
                                                    }
                                                } catch (e: Exception) { erro = "Data ou horário inexistente." }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                                    ) { Text("Confirmar Agendamento", fontWeight = FontWeight.Bold) }
                                }
                            }
                        }
                    }
                }

            } else if (abaSelecionada == 1) {
                if (agendamentosPendentes.isEmpty()) item { Text("Não tem serviços agendados.", color = Color.Gray) }
                items(agendamentosPendentes, key = { it.id }) { ag ->
                    val nomeShop = petshops.find { it.id == ag.petshopId }?.nome ?: "Petshop"
                    AgendamentoCard(agendamento = ag, nomePet = pets.find { it.id == ag.petId }?.nome ?: "Pet", nomePetshop = nomeShop, onClick = { onAgendamentoClick(ag.id) }, onConcluir = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().atualizar(ag.copy(concluido = !ag.concluido)); carregarDados() } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) } } }, onRemover = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().deletar(ag); carregarDados() } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) } } })
                }
            } else {
                if (agendamentosConcluidos.isEmpty()) item { Text("Ainda não tem serviços realizados.", color = Color.Gray) }
                items(agendamentosConcluidos, key = { it.id }) { ag ->
                    val nomeShop = petshops.find { it.id == ag.petshopId }?.nome ?: "Petshop"
                    AgendamentoCard(agendamento = ag, nomePet = pets.find { it.id == ag.petId }?.nome ?: "Pet", nomePetshop = nomeShop, onClick = { onAgendamentoClick(ag.id) }, onConcluir = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().atualizar(ag.copy(concluido = !ag.concluido)); carregarDados() } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) } } }, onRemover = { scope.launch(Dispatchers.IO) { try { dados.db.agendamentoDao().deletar(ag); carregarDados() } catch (e: Exception) { Log.e("AgendaScreen", "Erro", e) } } })
                }
            }
        }
    }
}

@Composable
fun AgendamentoCard(agendamento: Agendamento, nomePet: String, nomePetshop: String, onClick: () -> Unit, onConcluir: () -> Unit, onRemover: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agendamento.concluido, onCheckedChange = { onConcluir() }, colors = CheckboxDefaults.colors(checkedColor = VerdePrincipal))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {

                    val servicoPrincipal = agendamento.servico.split(",").firstOrNull()?.trim() ?: ""
                    Icon(EstiloApp.iconeServico(servicoPrincipal), contentDescription = null, modifier = Modifier.size(16.dp), tint = if (agendamento.concluido) Color.Gray else Color.Black)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${agendamento.servico} • $nomePet", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = if (agendamento.concluido) Color.Gray else Color.Black, textDecoration = if (agendamento.concluido) TextDecoration.LineThrough else TextDecoration.None)
                }
                Text("em $nomePetshop", color = VerdePrincipal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${agendamento.data} às ${agendamento.horario}", color = Color.Gray, fontSize = 12.sp)
            }
            IconButton(onClick = onRemover) { Icon(Icons.Default.Delete, contentDescription = "Remover", tint = VermelhoAlerta) }
        }
    }
}

class CepVisualTransformationMask : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = buildString {
            for (i in trimmed.indices) {
                if (i == 5) append("-")
                append(trimmed[i])
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val transformed = if (offset <= 5) offset else offset + 1
                return transformed.coerceAtMost(out.length)
            }
            override fun transformedToOriginal(offset: Int): Int {
                val original = if (offset <= 6) offset else offset - 1
                return original.coerceAtMost(trimmed.length)
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

class DataVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(8)
        val out = buildString { for (i in trimmed.indices) { if (i == 2 || i == 4) append("/"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 2) return offset.coerceAtMost(out.length); if (offset <= 4) return (offset + 1).coerceAtMost(out.length); return (offset + 2).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); if (offset <= 6) return (offset - 1).coerceIn(0, trimmed.length); return (offset - 2).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
class HoraVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(4)
        val out = buildString { for (i in trimmed.indices) { if (i == 2) append(":"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 2) return offset.coerceAtMost(out.length); return (offset + 1).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); return (offset - 1).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}