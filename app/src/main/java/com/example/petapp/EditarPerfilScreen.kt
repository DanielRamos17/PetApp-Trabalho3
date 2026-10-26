@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditarPerfilScreen(dados: DadosApp, onVoltar: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var usuario by remember { mutableStateOf<Usuario?>(null) }

    var cpfLeitura by remember { mutableStateOf("") }
    var nomeEditavel by remember { mutableStateOf("") }
    var celularEditavel by remember { mutableStateOf("") }
    var cepEditavel by remember { mutableStateOf("") }
    var enderecoEditavel by remember { mutableStateOf("") }
    var fotoSelecionada by remember { mutableStateOf("") }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            fotoSelecionada = it.toString()
        }
    }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val idLogado = dados.prefs.getInt("usuario_logado_id", -1)
                val u = dados.db.usuarioDao().buscarPorId(idLogado)
                withContext(Dispatchers.Main) {
                    usuario = u
                    cpfLeitura = u?.cpf ?: ""
                    nomeEditavel = u?.nome ?: ""
                    celularEditavel = u?.celular ?: ""
                    cepEditavel = u?.cep ?: ""
                    enderecoEditavel = u?.endereco ?: ""
                    fotoSelecionada = u?.fotoUri ?: ""
                }
            } catch (e: Exception) { Log.e("EditarPerfil", "Erro", e) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoBege)) {
        TopAppBar(title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onVoltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = FundoBege))

        if (usuario != null) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Toque na imagem para alterar a sua foto", fontSize = 12.sp, color = Color.Gray)
                        Box(modifier = Modifier.clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            FotoPerfil(uri = fotoSelecionada, modifier = Modifier.size(80.dp), fallbackIcon = Icons.Default.Person)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(value = cpfLeitura, onValueChange = { }, label = { Text("CPF") }, enabled = false, singleLine = true, visualTransformation = CpfVisualTransformationPerfil(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.DarkGray, disabledBorderColor = Color.LightGray, disabledLabelColor = Color.Gray), modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = nomeEditavel, onValueChange = { nomeEditavel = it }, label = { Text("Nome completo") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = celularEditavel, onValueChange = { celularEditavel = it.filter { c -> c.isDigit() }.take(11) }, label = { Text("Celular") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = TelefoneVisualTransformationPerfil(), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = cepEditavel, onValueChange = { cepEditavel = it.filter { c -> c.isDigit() }.take(8) }, label = { Text("CEP") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CepVisualTransformationPerfil(), shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                        OutlinedTextField(value = enderecoEditavel, onValueChange = { enderecoEditavel = it }, label = { Text("Endereço completo (Opcional)") }, singleLine = true, shape = RoundedCornerShape(12.dp), colors = coresCampo(), modifier = Modifier.fillMaxWidth())

                        Button(
                            onClick = {
                                if (nomeEditavel.isBlank()) Toast.makeText(context, "O nome não pode ficar vazio.", Toast.LENGTH_SHORT).show()
                                else {
                                    scope.launch(Dispatchers.IO) {
                                        val atualizado = usuario!!.copy(nome = nomeEditavel, celular = celularEditavel, cep = cepEditavel, endereco = enderecoEditavel, fotoUri = fotoSelecionada)
                                        dados.db.usuarioDao().atualizar(atualizado)
                                        withContext(Dispatchers.Main) { Toast.makeText(context, "Dados salvos no banco!", Toast.LENGTH_SHORT).show(); onVoltar() }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(14.dp)
                        ) { Text("Salvar alterações", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}


private class CepVisualTransformationPerfil : VisualTransformation {
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

private class CpfVisualTransformationPerfil : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(11)
        val out = buildString { for (i in trimmed.indices) { if (i == 3 || i == 6) append("."); if (i == 9) append("-"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 3) return offset.coerceAtMost(out.length); if (offset <= 6) return (offset + 1).coerceAtMost(out.length); if (offset <= 9) return (offset + 2).coerceAtMost(out.length); return (offset + 3).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 3) return offset.coerceIn(0, trimmed.length); if (offset <= 7) return (offset - 1).coerceIn(0, trimmed.length); if (offset <= 11) return (offset - 2).coerceIn(0, trimmed.length); return (offset - 3).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

private class TelefoneVisualTransformationPerfil : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.take(11)
        val out = buildString { for (i in trimmed.indices) { if (i == 0) append("("); if (i == 2) append(") "); if (i == 7) append("-"); append(trimmed[i]) } }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int { if (offset <= 0) return 0; if (offset <= 2) return (offset + 1).coerceAtMost(out.length); if (offset <= 7) return (offset + 3).coerceAtMost(out.length); return (offset + 4).coerceAtMost(out.length) }
            override fun transformedToOriginal(offset: Int): Int { if (offset <= 1) return 0; if (offset <= 3) return (offset - 1).coerceIn(0, trimmed.length); if (offset <= 5) return 2.coerceAtMost(trimmed.length); if (offset <= 10) return (offset - 3).coerceIn(0, trimmed.length); if (offset == 11) return 7.coerceAtMost(trimmed.length); return (offset - 4).coerceIn(0, trimmed.length) }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}