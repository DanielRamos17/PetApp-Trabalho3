@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.petapp

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val VerdePrincipal = Color(0xFF0F5B43)
val FundoApp = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(dados: DadosApp, onNavigateToCadastro: () -> Unit, onNavigateToHome: () -> Unit) {
    val scope = rememberCoroutineScope()
    var emailOuCpf by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var lembrarMim by remember { mutableStateOf(true) }
    var erroLogin by remember { mutableStateOf("") }
    var mostrarEsqueciSenha by remember { mutableStateOf(false) }
    var emailRecuperacao by remember { mutableStateOf("") }
    var novaSenhaRecuperacao by remember { mutableStateOf("") }
    val context = LocalContext.current

    if (mostrarEsqueciSenha) {
        AlertDialog(
            onDismissRequest = { mostrarEsqueciSenha = false },
            title = { Text("Redefinir senha") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = emailRecuperacao, onValueChange = { emailRecuperacao = it }, label = { Text("Seu e-mail cadastrado") }, singleLine = true)
                    OutlinedTextField(value = novaSenhaRecuperacao, onValueChange = { novaSenhaRecuperacao = it }, label = { Text("Nova senha (mín. 6 caracteres)") }, visualTransformation = PasswordVisualTransformation(), singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (!emailRecuperacao.contains("@")) {
                        Toast.makeText(context, "Digite um e-mail válido", Toast.LENGTH_SHORT).show()
                    } else if (novaSenhaRecuperacao.length < 6) {
                        Toast.makeText(context, "A nova senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch(Dispatchers.IO) {
                            val u = dados.db.usuarioDao().buscarPorEmailOuCpf(emailRecuperacao.trim(), "")
                            if (u != null) {
                                dados.db.usuarioDao().atualizar(u.copy(senha = novaSenhaRecuperacao))
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Senha atualizada no banco de dados!", Toast.LENGTH_SHORT).show()
                                    emailRecuperacao = ""; novaSenhaRecuperacao = ""; mostrarEsqueciSenha = false
                                }
                            } else {
                                withContext(Dispatchers.Main) { Toast.makeText(context, "E-mail não encontrado.", Toast.LENGTH_SHORT).show() }
                            }
                        }
                    }
                }) { Text("Salvar nova senha", color = VerdePrincipal) }
            },
            dismissButton = { TextButton(onClick = { mostrarEsqueciSenha = false }) { Text("Cancelar") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FundoApp).padding(24.dp), horizontalAlignment = Alignment.Start) {
        Spacer(modifier = Modifier.height(40.dp))
        Image(painter = painterResource(id = R.drawable.ic_logo), contentDescription = "Logo", modifier = Modifier.size(60.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text("Bem-vindo de\nvolta", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black, lineHeight = 36.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Entre para cuidar do seu melhor amigo.", fontSize = 16.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))

        Text("E-mail ou CPF", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)


        val isCpf = emailOuCpf.isNotEmpty() && emailOuCpf.all { it.isDigit() }

        OutlinedTextField(
            value = emailOuCpf,
            onValueChange = { input ->
                if (input.all { it.isDigit() }) {
                    if (input.length <= 11) emailOuCpf = input
                } else {
                    emailOuCpf = input
                }
            },
            placeholder = { Text("Digite um dos dois") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            visualTransformation = if (isCpf) CpfVisualTransformation() else VisualTransformation.None,
            keyboardOptions = if (isCpf) KeyboardOptions(keyboardType = KeyboardType.Number) else KeyboardOptions.Default,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal, unfocusedBorderColor = Color.LightGray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Senha", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(value = senha, onValueChange = { senha = it }, placeholder = { Text("••••••") }, leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VerdePrincipal, unfocusedBorderColor = VerdePrincipal))

        if (erroLogin.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(erroLogin, color = VermelhoAlerta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = lembrarMim, onCheckedChange = { lembrarMim = it }, colors = CheckboxDefaults.colors(checkedColor = VerdePrincipal))
                Text("Lembrar de mim", fontSize = 14.sp)
            }
            Text("Esqueci a senha", color = VerdePrincipal, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.clickable { mostrarEsqueciSenha = true })
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (emailOuCpf.isBlank() || senha.isBlank()) {
                    erroLogin = "Preencha o e-mail/CPF e a senha."
                } else {
                    scope.launch(Dispatchers.IO) {
                        val cpfLimpo = emailOuCpf.trim().filter { it.isDigit() }.ifEmpty { emailOuCpf.trim() }
                        val u = dados.db.usuarioDao().autenticar(emailOuCpf.trim(), cpfLimpo, senha)
                        withContext(Dispatchers.Main) {
                            if (u != null) {
                                erroLogin = ""
                                if (lembrarMim) dados.prefs.edit().putInt("usuario_logado_id", u.id).apply()
                                else dados.prefs.edit().remove("usuario_logado_id").apply()
                                onNavigateToHome()
                            } else {
                                erroLogin = "E-mail/CPF ou senha incorretos."
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VerdePrincipal), shape = RoundedCornerShape(16.dp)
        ) { Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Bold) }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToCadastro() }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Não tem conta? ", color = Color.Gray)
            Text("Cadastre-se", color = VerdePrincipal, fontWeight = FontWeight.Bold)
        }
    }
}