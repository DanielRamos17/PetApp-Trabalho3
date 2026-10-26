@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CadastroScreen(dados: DadosApp, onNavigateBackToLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var nome by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var confirmarSenha by remember { mutableStateOf("") }
    var aceiteTermos by remember { mutableStateOf(false) }
    var erro by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFFFF)).padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(onClick = onNavigateBackToLogin, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Criar conta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text(text = "Leva menos de um minuto.", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Text("Nome completo", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = nome, onValueChange = { nome = it }, placeholder = { Text("Ex: João Silva") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("CPF", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = cpf, onValueChange = { val apenasNumeros = it.filter { char -> char.isDigit() }; if (apenasNumeros.length <= 11) cpf = apenasNumeros }, placeholder = { Text("000.000.000-00") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = CpfVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("E-mail", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = email, onValueChange = { email = it }, placeholder = { Text("usuario@email.com") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Celular", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = celular, onValueChange = { val apenasNumeros = it.filter { char -> char.isDigit() }; if (apenasNumeros.length <= 11) celular = apenasNumeros }, placeholder = { Text("(00) 00000-0000") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), visualTransformation = TelefoneVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = senha, onValueChange = { senha = it }, placeholder = { Text("Mínimo 6 caracteres") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
        Spacer(modifier = Modifier.height(12.dp))

        Text("Confirmar Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = confirmarSenha, onValueChange = { confirmarSenha = it }, placeholder = { Text("Repita sua senha") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = aceiteTermos, onCheckedChange = { aceiteTermos = it }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0F5B43)))
            Text("Aceito os Termos de uso e a Política de privacidade.", fontSize = 12.sp)
        }

        if (erro.isNotEmpty()) { Spacer(modifier = Modifier.height(8.dp)); Text(erro, color = VermelhoAlerta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                when {
                    nome.isBlank() -> erro = "Informe seu nome completo."
                    cpf.length != 11 -> erro = "Informe um CPF válido de 11 dígitos."
                    !email.contains("@") -> erro = "Informe um e-mail válido."
                    celular.length < 10 -> erro = "Informe um número de celular válido."
                    senha.length < 6 -> erro = "A senha deve ter no mínimo 6 caracteres."
                    senha != confirmarSenha -> erro = "As senhas não coincidem."
                    else -> {
                        scope.launch(Dispatchers.IO) {
                            val existente = dados.db.usuarioDao().buscarPorEmailOuCpf(email.trim(), cpf)
                            if (existente != null) {
                                withContext(Dispatchers.Main) { erro = "Já existe uma conta com este e-mail ou CPF." }
                            } else {
                                // Deixamos o endereço e o cep vazios no momento do cadastro!
                                dados.db.usuarioDao().inserir(Usuario(nome = nome.trim(), cpf = cpf, email = email.trim(), celular = celular, senha = senha, cep = "", endereco = ""))
                                withContext(Dispatchers.Main) {
                                    erro = ""
                                    Toast.makeText(context, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                                    onNavigateBackToLogin()
                                }
                            }
                        }
                    }
                }
            },
            enabled = aceiteTermos, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F5B43)), shape = RoundedCornerShape(16.dp)
        ) { Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Já tenho conta · ", color = Color.Gray)
            Text("Entrar", color = Color(0xFF0F5B43), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateBackToLogin() })
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

class CpfVisualTransformation : VisualTransformation {
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
class TelefoneVisualTransformation : VisualTransformation {
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