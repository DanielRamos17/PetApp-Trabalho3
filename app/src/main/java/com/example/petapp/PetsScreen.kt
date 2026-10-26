@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Female
import androidx.compose.material.icons.rounded.Male
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PetsScreen(dados: DadosApp, onPetClick: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pets by remember { mutableStateOf<List<Pet>>(emptyList()) }
    var agendamentos by remember { mutableStateOf<List<Agendamento>>(emptyList()) }

    var fotoSelecionada by rememberSaveable { mutableStateOf("") }
    var nome by rememberSaveable { mutableStateOf("") }
    var raca by rememberSaveable { mutableStateOf("") }
    var especie by rememberSaveable { mutableStateOf("") }
    var sexo by rememberSaveable { mutableStateOf("") }
    var idade by rememberSaveable { mutableStateOf("") }
    var peso by rememberSaveable { mutableStateOf("") }
    var erro by rememberSaveable { mutableStateOf("") }
    var petParaRemover by remember { mutableStateOf<Pet?>(null) }

    // Importador da Galeria
    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fotoSelecionada = it.toString()
        }
    }

    fun carregarDados() {
        scope.launch(Dispatchers.IO) {
            try {
                val listaPets = dados.db.petDao().buscarTodos()
                val listaAgs = dados.db.agendamentoDao().buscarTodos()
                withContext(Dispatchers.Main) { pets = listaPets; agendamentos = listaAgs }
            } catch (e: Exception) { Log.e("PetsScreen", "Erro", e) }
        }
    }
    LaunchedEffect(Unit) { carregarDados() }

    petParaRemover?.let { pet ->
        AlertDialog(
            onDismissRequest = { petParaRemover = null }, title = { Text("Remover ${pet.nome}?") }, text = { Text("Os agendamentos deste pet também serão removidos.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val agsDoPet = dados.db.agendamentoDao().buscarPorPetId(pet.id)
                        agsDoPet.forEach { dados.db.agendamentoDao().deletar(it) }
                        dados.db.petDao().deletar(pet)
                        withContext(Dispatchers.Main) { petParaRemover = null; carregarDados() }
                    }
                }) { Text("Remover", color = VermelhoAlerta) }
            }, dismissButton = { TextButton(onClick = { petParaRemover = null }) { Text("Cancelar") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().background(FundoBege), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Meus pets", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black) }
        item {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Adicionar pet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black, modifier = Modifier.align(Alignment.Start))

                    Text("Toque na imagem para importar da galeria", fontSize = 12.sp, color = Color.Gray)
                    Box(modifier = Modifier.clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                        FotoPerfil(uri = fotoSelecionada, modifier = Modifier.size(80.dp))
                    }

                    OutlinedTextField(value = nome, onValueChange = { nome = it }, label = { Text("Nome") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = raca, onValueChange = { raca = it }, label = { Text("Raça (opcional)") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = especie == "Cão", onClick = { especie = "Cão" }, label = { Text("Cão") })
                        FilterChip(selected = especie == "Gato", onClick = { especie = "Gato" }, label = { Text("Gato") })
                    }

                    if (especie.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = sexo == "Macho", onClick = { sexo = "Macho" }, label = { Text("Macho") }, leadingIcon = { Icon(Icons.Rounded.Male, contentDescription = null) })
                            FilterChip(selected = sexo == "Fêmea", onClick = { sexo = "Fêmea" }, label = { Text("Fêmea") }, leadingIcon = { Icon(Icons.Rounded.Female, contentDescription = null) })
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(value = idade, onValueChange = { idade = it.filter { c -> c.isDigit() }.take(2) }, label = { Text("Idade (anos)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = peso, onValueChange = { peso = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(5) }, label = { Text("Peso (kg)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.weight(1f))
                    }
                    if (erro.isNotEmpty()) Text(erro, color = VermelhoAlerta, fontSize = 12.sp)

                    Button(
                        onClick = {
                            val idadeInt = idade.toIntOrNull()
                            val pesoDouble = peso.replace(',', '.').toDoubleOrNull()
                            if (nome.isBlank()) erro = "Informe o nome do pet."
                            else if (especie.isBlank()) erro = "Selecione se é Cão ou Gato."
                            else if (sexo.isBlank()) erro = "Selecione se é Macho ou Fêmea."
                            else if (idadeInt == null) erro = "Informe uma idade válida."
                            else if (pesoDouble == null) erro = "Informe um peso válido."
                            else {
                                scope.launch(Dispatchers.IO) {
                                    dados.db.petDao().inserir(Pet(nome = nome.trim(), especie = especie, sexo = sexo, raca = raca.trim().ifBlank { "SRD" }, idadeAnos = idadeInt, pesoKg = pesoDouble, fotoUri = fotoSelecionada))
                                    withContext(Dispatchers.Main) {
                                        nome = ""; raca = ""; especie = ""; sexo = ""; idade = ""; peso = ""; fotoSelecionada = ""; erro = ""; carregarDados()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                    ) { Text("Adicionar pet", fontWeight = FontWeight.Bold) }
                }
            }
        }
        item { Text("Pets cadastrados (${pets.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black) }
        if (pets.isEmpty()) item { Text("Nenhum pet cadastrado ainda.", color = Color.Gray) }
        items(pets, key = { it.id }) { pet -> PetCard(pet = pet, pendentes = agendamentos.count { it.petId == pet.id && !it.concluido }, onClick = { onPetClick(pet.id) }, onRemover = { petParaRemover = pet }) }
    }
}

@Composable
fun PetCard(pet: Pet, pendentes: Int, onClick: () -> Unit, onRemover: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            FotoPerfil(uri = pet.fotoUri, modifier = Modifier.size(52.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text("${pet.sexo} • ${pet.raca} • ${pet.idadeAnos} ano(s)", color = Color.Gray, fontSize = 12.sp)
                Text(if (pendentes == 0) "Sem agendamentos pendentes" else "$pendentes pendente(s)", color = VerdePrincipal, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onRemover) { Icon(Icons.Default.Delete, contentDescription = "Remover", tint = VermelhoAlerta) }
        }
    }
}