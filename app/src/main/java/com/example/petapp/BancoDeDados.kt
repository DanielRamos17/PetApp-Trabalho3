package com.example.petapp

import android.content.Context
import androidx.room.*

@Dao
interface UsuarioDao {
    @Insert suspend fun inserir(usuario: Usuario): Long
    @Update suspend fun atualizar(usuario: Usuario)
    @Delete suspend fun deletar(usuario: Usuario)
    @Query("SELECT * FROM usuarios") suspend fun buscarTodos(): List<Usuario>
    @Query("SELECT * FROM usuarios WHERE (LOWER(email) = LOWER(:identificador) OR cpf = :cpfLimpo) AND senha = :senha LIMIT 1") suspend fun autenticar(identificador: String, cpfLimpo: String, senha: String): Usuario?
    @Query("SELECT * FROM usuarios WHERE LOWER(email) = LOWER(:email) OR cpf = :cpf LIMIT 1") suspend fun buscarPorEmailOuCpf(email: String, cpf: String): Usuario?
    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Usuario?
}

@Dao
interface PetDao {
    @Insert suspend fun inserir(pet: Pet): Long
    @Update suspend fun atualizar(pet: Pet)
    @Delete suspend fun deletar(pet: Pet)
    @Query("SELECT * FROM pets ORDER BY id ASC") suspend fun buscarTodos(): List<Pet>
    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Pet?
}

@Dao
interface PetshopDao {
    @Insert suspend fun inserir(petshop: Petshop): Long
    @Query("SELECT * FROM petshops") suspend fun buscarTodos(): List<Petshop>
    @Query("SELECT * FROM petshops WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Petshop?
}

@Dao
interface AgendamentoDao {
    @Insert suspend fun inserir(agendamento: Agendamento): Long
    @Update suspend fun atualizar(agendamento: Agendamento)
    @Delete suspend fun deletar(agendamento: Agendamento)
    @Query("SELECT * FROM agendamentos ORDER BY id ASC") suspend fun buscarTodos(): List<Agendamento>
    @Query("SELECT * FROM agendamentos WHERE id = :id LIMIT 1") suspend fun buscarPorId(id: Int): Agendamento?
    @Query("SELECT * FROM agendamentos WHERE petId = :petId") suspend fun buscarPorPetId(petId: Int): List<Agendamento>
}

@Database(entities = [Usuario::class, Pet::class, Petshop::class, Agendamento::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun petDao(): PetDao
    abstract fun petshopDao(): PetshopDao
    abstract fun agendamentoDao(): AgendamentoDao

    companion object {
        @Volatile
        private var INSTANCIA: AppDatabase? = null

        fun obterBanco(context: Context): AppDatabase {
            return INSTANCIA ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "petapp.db")
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCIA = it }
            }
        }
    }
}