# PetApp — Trabalho 3 (Room & Persistência Local)

Repositório contendo a evolução do **PetApp** para o Trabalho 3. O projeto consiste em um aplicativo mobile desenvolvido em Kotlin e Jetpack Compose focado em um Marketplace de Petshops, estruturado com persistência de dados local utilizando **SQLite / Room Database**.

---

## 1. Evolução: Trabalho 2 vs. Trabalho 3
* **No Trabalho 2:** O aplicativo gerenciava dados utilizando coleções em memória (`mutableStateListOf`). Embora permitisse inserções e remoções em tempo de execução, o encerramento da aplicação resultava na perda integral de todos os registros.
* **No Trabalho 3:** A camada de dados foi completamente migrada para o **Room Database**. Agora, todas as operações de cadastro de usuários, pets, petshops e agendamentos são gravadas de forma permanente no dispositivo.

---

## 2. Arquitetura e Decisões Técnicas

### 2.1. Entidades (`@Entity`)
O banco de dados conta com quatro entidades principais, estruturadas com chaves primárias autogeradas (`@PrimaryKey(autoGenerate = true)`):
* **`Usuario`**: Armazena os dados do cliente (nome, CPF, e-mail, celular, senha, foto e CEP), permitindo referenciar a localização para o marketplace de serviços.
* **`Pet`**: Gerencia o perfil dos animais (nome, espécie, sexo, raça, idade e peso), utilizados também no cálculo dinâmico de preços dos serviços de acordo com o porte.
* **`Petshop`**: Modela as unidades parceiras disponíveis na região (nome, profissionais, serviços prestados, distância simulada e avaliação).
* **`Agendamento`**: Controla os compromissos vinculados ao pet e ao petshop, suportando múltiplos serviços simultâneos, data, horário, observações e status de conclusão.

### 2.2. DAOs e Consultas (`@Dao`)
Foram criadas interfaces DAO específicas para cada entidade (`UsuarioDao`, `PetDao`, `PetshopDao`, `AgendamentoDao`):
* Todas implementam as operações fundamentais (`@Insert`, `@Query` para busca geral, `@Update` e `@Delete`), obrigatoriamente declaradas como **`suspend fun`**.
* Incluem consultas filtradas por identificador (ex: `buscarPorId(id: Int)`), permitindo que os ecrãs de detalhes busquem o registo correto diretamente do banco de dados a partir do ID recebido na navegação.

### 2.3. Configuração do Banco (`AppDatabase`)
* A classe abstrata herda de `RoomDatabase` e integra todas as entidades.
* Implementa o **padrão Singleton** utilizando `companion object`, `@Volatile` e `synchronized` para otimizar o acesso e prevenir múltiplas instâncias concorrentes.

---

## 3. Desafios de Migração e Soluções

* **Concorrência e Assincronia (Corrotinas):** Para impedir o travamento da thread principal (UI), todas as chamadas aos DAOs foram encapsuladas estritamente dentro de blocos assíncronos utilizando `CoroutineScope(Dispatchers.IO).launch { ... }`.
* **Sincronização de Estado com o Room:** Com a remoção das listas voláteis, a interface passou a carregar os dados diretamente do banco na inicialização dos ecrãs por meio de `LaunchedEffect(Unit)` chamando `dao.buscarTodos()`.
* **Tratamento de Entradas e Máscaras:** Ajuste de formatações visuais (CEP, CPF, Telefone, Datas) com limites seguros baseados em `.take()` para garantir estabilidade e prevenir exceções de índice (*IndexOutOfBounds*) ao preencher formulários rapidamente.

---

## 4. Teste de Aceitação (Persistência Real)

O comportamento esperado para a validação do trabalho segue o procedimento abaixo:
1. **Adicionar:** Cadastrar um novo pet ou registrar um agendamento na aba de agenda/marketplace.
2. **Fechar:** Encerrar o aplicativo por completo, removendo-o do histórico de aplicações recentes do dispositivo.
3. **Reabrir:** Iniciar a aplicação novamente e verificar que os dados inseridos continuam preservados na interface, comprovando o funcionamento bem-sucedido do SQLite/Room.