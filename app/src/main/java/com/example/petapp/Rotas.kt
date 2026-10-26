package com.example.petapp

object Rotas {
    const val LOGIN = "login"
    const val CADASTRO = "cadastro"
    const val INICIO = "inicio"
    const val EXPLORAR = "explorar"
    const val PETS = "pets"
    const val AGENDA = "agenda"
    const val CONTA = "conta"
    const val EDITAR_PERFIL = "editar_perfil"

    const val DETALHE_PET = "pet/{petId}"
    const val DETALHE_AGENDAMENTO = "agendamento/{agendamentoId}"
    const val DETALHE_PETSHOP = "petshop/{petshopId}"

    fun detalhePet(id: Int) = "pet/$id"
    fun detalheAgendamento(id: Int) = "agendamento/$id"
    fun detalhePetshop(id: Int) = "petshop/$id"
}