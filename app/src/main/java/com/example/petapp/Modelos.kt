package com.example.petapp

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.room.Entity
import androidx.room.PrimaryKey
import coil.compose.AsyncImage
import java.util.Locale

@Entity(tableName = "usuarios")
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String, val cpf: String, val email: String, val celular: String, val senha: String,
    val fotoUri: String = "",
    val cep: String = "",
    val endereco: String = ""
)

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String, val especie: String, val sexo: String, val raca: String, val idadeAnos: Int, val pesoKg: Double, val fotoUri: String = ""
)

@Entity(tableName = "petshops")
data class Petshop(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nome: String,
    val profissionais: String,
    val servicosText: String,
    val distanciaMock: String,
    val avaliacao: Double
)

@Entity(tableName = "agendamentos")
data class Agendamento(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val petId: Int,
    val petshopId: Int,
    val servico: String,
    val data: String,
    val horario: String,
    val observacoes: String = "",
    val concluido: Boolean = false
)

@Composable
fun FotoPerfil(uri: String, modifier: Modifier = Modifier, fallbackIcon: ImageVector = Icons.Rounded.Pets) {
    if (uri.isNotBlank()) AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier.clip(CircleShape).background(Color(0xFFE8E8E8)))
    else Box(modifier = modifier.clip(CircleShape).background(Color(0xFF7CB39E)), contentAlignment = Alignment.Center) { Icon(fallbackIcon, contentDescription = null, tint = Color.White) }
}

object EstiloApp {
    val avataresUsuario = listOf(Pair(Color(0xFF7CB39E), Icons.Rounded.Person), Pair(Color(0xFF5C8CB3), Icons.Rounded.Face), Pair(Color(0xFFB37C9F), Icons.Rounded.Face3), Pair(Color(0xFFD6A069), Icons.Rounded.Face6))
    val avataresPet = listOf(Pair(Color(0xFFFF9B70), Icons.Rounded.Pets), Pair(Color(0xFF7CB39E), Icons.Rounded.CrueltyFree), Pair(Color(0xFF87CEEB), Icons.Rounded.PestControlRodent), Pair(Color(0xFFE6A8D7), Icons.Rounded.FlutterDash))
    fun iconeServico(servico: String): ImageVector = when(servico) { "Banho" -> Icons.Rounded.WaterDrop; "Tosa" -> Icons.Rounded.ContentCut; "Vacinas" -> Icons.Rounded.MedicalServices; "Consulta" -> Icons.Rounded.MonitorHeart; "Hotel" -> Icons.Rounded.Bed; "Táxi pet" -> Icons.Rounded.LocalTaxi; else -> Icons.Rounded.Pets }
}

object Servicos { val precosBase: Map<String, Double> = linkedMapOf("Banho" to 59.0, "Tosa" to 75.0, "Vacinas" to 89.0, "Consulta" to 129.0, "Hotel" to 110.0, "Táxi pet" to 25.0) }
fun Pet.porte(): String = when { pesoKg <= 10.0 -> "Pequeno"; pesoKg <= 25.0 -> "Médio"; else -> "Grande" }
fun Pet.fatorPreco(): Double = when (porte()) { "Pequeno" -> 1.0; "Médio" -> 1.25; else -> 1.5 }
fun Pet.idadeHumana(): Int = when { idadeAnos <= 0 -> 0; idadeAnos == 1 -> 15; idadeAnos == 2 -> 24; else -> 24 + (idadeAnos - 2) * (if (especie == "Gato") 4 else 5) }
fun Pet.precoDoServico(servico: String): Double = (Servicos.precosBase[servico] ?: 0.0) * fatorPreco()
fun Double.comoReais(): String = "R$ " + String.format(Locale.forLanguageTag("pt-BR"), "%.2f", this)
fun Double.comoKg(): String = if (this % 1.0 == 0.0) "${toInt()} kg" else "$this kg".replace('.', ',')

class DadosApp(context: Context) {
    val db = AppDatabase.obterBanco(context)
    val prefs = context.getSharedPreferences("petapp_prefs", Context.MODE_PRIVATE)
    var servicoPreSelecionado by mutableStateOf("")
    var petPreSelecionado by mutableStateOf(-1)

    // Criação dos 5 Petshops com dados detalhados para o Marketplace
    suspend fun inicializarPetshopsFalsos() {
        if (db.petshopDao().buscarTodos().isEmpty()) {
            db.petshopDao().inserir(Petshop(nome = "Petz Centro", profissionais = "Ana, João", servicosText = "Banho, Tosa, Táxi pet", distanciaMock = "1.2 km", avaliacao = 4.8))
            db.petshopDao().inserir(Petshop(nome = "Cãopeão Batel", profissionais = "Dr. Marcos", servicosText = "Consulta, Vacinas", distanciaMock = "3.5 km", avaliacao = 4.9))
            db.petshopDao().inserir(Petshop(nome = "Juvevê Pets", profissionais = "Julia, Roberto", servicosText = "Banho, Tosa, Hotel", distanciaMock = "4.1 km", avaliacao = 4.5))
            db.petshopDao().inserir(Petshop(nome = "Estética Animal VIP", profissionais = "Carla", servicosText = "Banho, Tosa", distanciaMock = "2.0 km", avaliacao = 4.6))
            db.petshopDao().inserir(Petshop(nome = "Clínica Vida Pet", profissionais = "Dra. Fernanda, Dr. Lucas", servicosText = "Consulta, Vacinas, Cirurgias", distanciaMock = "5.5 km", avaliacao = 4.9))
        }
    }
}