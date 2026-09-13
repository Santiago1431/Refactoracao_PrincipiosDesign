# Relatório de Refatoração — Princípios de Projeto de Software

**Disciplina:** Gestão de Ciclo de Vida de Software  
**Trabalho Acadêmico / Portfólio de Refatoração**

---

## 🎯 Objetivo

Analisar e refatorar um sistema de gerenciamento e fechamento de pedidos, eliminando violações de princípios fundamentais de projeto orientado a objetos (**SOLID**, **Demeter**, **Composição sobre Herança**) sem alterar o comportamento funcional e as regras de negócio da aplicação.

---

## 🗺️ Alinhamento com o Fluxograma / Diagrama da Arquitetura

O projeto refatorado foi comparado diretamente com o diagrama de arquitetura alvo, confirmando o cumprimento de todos os blocos conceituais e princípios:

```
+---------------------------------------------------------------------------------------+
|                                  ARQUITETURA ALVO                                     |
+---------------------------------------------------------------------------------------+
|  [ Pagamento ]  --> ISP (interfaces segregadas) & Composição                          |
|  [ Desconto ]   --> OCP/DIP (abstração de descontos via Strategy)                     |
|  [ Entrega ]    --> LSP (abstração de frete determinística e substituível)            |
|  [Persistência] --> SRP/DIP (isolamento do salvamento de arquivos/repositório)        |
|  [ Domínio ]    --> Lei de Demeter (delegação interna: Pedido -> Cliente -> Endereço) |
|  [PedidoService]--> Orquestrador de fluxo enxuto dependente de abstrações             |
+---------------------------------------------------------------------------------------+
```

### Tabela de Mapeamento: Diagrama vs. Implementação

| Bloco do Diagrama | Elemento no Diagrama | Implementação no Projeto | Princípio Aplicado | Observações |
| :--- | :--- | :--- | :--- | :--- |
| **Domínio** | `Pedido`, `Cliente`, `Endereco`, `Cidade` | `entity.Pedido`, `entity.Cliente`, `entity.Endereco`, `entity.Cidade` | **Lei de Demeter** | `Cliente` expõe `getCidadeEntrega()` delegando para `Endereco`, evitando violação de conhecimento interno. |
| **Desconto** | `<<interface>> Desconto` | `desconto.IDesconto` | **OCP / DIP** | Utiliza convenção `I` (`IDesconto`), implementada por `DescontoAluno`, `DescontoProfessor`, `DescontoFuncionario` e `SemDesconto`. |
| **Pagamento** | `<<interface>> Pagamento`, `Parcelavel`, `GeraBoleto` | `pagamento.IPagamento`, `IPagamentoParcelavel`, `IPagamentoBoleto` | **ISP / DIP** | Segregação completa: `PagamentoCartao`, `PagamentoPix` e `PagamentoBoleto` implementam apenas o que utilizam. |
| **Entrega** | `<<interface>> TipoEntrega` (`EntregaDomicilio`, `RetiradaLoja`) | `entrega.IEntrega` (`Entrega`, `EntregaRetiradaLoja`) | **LSP** | Eliminação da exceção em `EntregaRetiradaLoja`, garantindo substituibilidade e retorno numérico previsível de frete. |
| **Persistência** | `PedidoRepository` / `PedidoRepositoryArquivo` | `repository.PedidoRepository` | **SRP** | Isola o salvamento em `pedidos.txt` para fora do serviço (atende à nota do roteiro de separar o salvamento). |
| **Orquestração** | `PedidoService` | `service.PedidoService` | **SRP / Composição** | Classe central enxuta, sem herança indevida de cartão e sem acoplamento a classes concretas. |

---

## 🛠️ Princípios Trabalhados e Mudanças Realizadas

### 1. Responsabilidade Única (Single Responsibility Principle — SRP)
- **Problema:** A classe `PedidoService` acumulava múltiplas responsabilidades: calcular totais, formatar relatórios/mensagens, processar pagamentos e gravar arquivos em disco (`Files.writeString` gravando em `pedidos.txt`).
- **Solução:** A responsabilidade de persistência dos pedidos foi isolada na classe `PedidoRepository` (pacote `projetoprincipiosdesign.repository`), deixando o serviço focado exclusivamente na orquestração da regra de negócio.

---

### 2. Segregação de Interfaces (Interface Segregation Principle — ISP)
- **Problema:** A interface `IPagamento` era "gorda" (*fat interface*) e continha métodos para `pagar`, `parcelar` e `gerarBoleto`. Isso forçava implementações como `PagamentoPix` a lançar `UnsupportedOperationException` para parcelamento e `PagamentoCartao` a implementar métodos vazios para boleto.
- **Solução:** A interface foi segregada em contratos coesos e específicos:
  - `IPagamento`: método comum `void pagar(double valor);`
  - `IPagamentoParcelavel`: método `void parcelar(double valor, int parcelas);`
  - `IPagamentoBoleto`: método `void gerarBoleto(double valor);`
- **Resultado:** Cada classe implementa estritamente o que utiliza:
  - `PagamentoPix` implementa apenas `IPagamento`.
  - `PagamentoCartao` implementa `IPagamento` e `IPagamentoParcelavel`.
  - `PagamentoBoleto` implementa `IPagamento` e `IPagamentoBoleto`.

---

### 3. Inversão de Dependências (Dependency Inversion Principle — DIP)
- **Problema:** O método `finalizarPedido` instanciava diretamente classes concretas (`new PagamentoCartao()`, `new PagamentoPix()`, etc.) usando `if/else` com strings.
- **Solução:** `PedidoService` agora depende exclusivamente da interface abstrata `IPagamento`, recebendo a forma de pagamento já instanciada pela camada superior (`Main`).

---

### 4. Prefira Composição à Herança (Favor Composition over Inheritance)
- **Problema:** `PedidoService` herdava diretamente de `PagamentoCartao` (`public class PedidoService extends PagamentoCartao`), gerando um acoplamento errôneo (um serviço de pedidos **não é** uma forma de pagamento por cartão).
- **Solução:** A herança foi removida (`public class PedidoService`) e a relação com pagamentos passou a ser tratada por **composição/delegação** via a interface `IPagamento`.

---

### 5. Princípio / Lei de Demeter (Princípio do Menor Conhecimento)
- **Problema:** O método `obterCidadeEntrega` realizava chamadas encadeadas em cadeia (*train wreck*):  
  `pedido.getCliente().getEndereco().getCidade().getNome();`  
  O serviço precisava conhecer a estrutura interna de 4 níveis de classes diferentes.
- **Solução:** Implementou-se a delegação hierárquica respeitando os limites de cada objeto:
  - `Endereco.getNomeCidade()`: acessa a cidade e retorna o nome.
  - `Cliente.getCidadeEntrega()`: delega para `endereco.getNomeCidade()`.
  - `Pedido.getCidadeEntrega()`: delega para `cliente.getCidadeEntrega()`.
  - `PedidoService.obterCidadeEntrega(pedido)`: chama apenas `pedido.getCidadeEntrega()`.

---

### 6. Aberto/Fechado (Open/Closed Principle — OCP)
- **Problema:** O método `calcularTotal` continha verificações com `if/else` baseadas no tipo de cliente (`"ALUNO"`, `"PROFESSOR"`, `"FUNCIONARIO"`). Qualquer novo tipo exigiria modificar a classe de serviço.
- **Solução:** Criação da interface abstrata `IDesconto` (Padrão *Strategy*) com implementações específicas:
  - `DescontoAluno` (10% de desconto)
  - `DescontoProfessor` (15% de desconto)
  - `DescontoFuncionario` (20% de desconto)
  - `SemDesconto` (0% de desconto)
- **Resultado:** `PedidoService.calcularTotal(Pedido, IDesconto)` aplica o desconto genericamente sem precisar ser modificado para novos tipos de desconto.

---

### 7. Substituição de Liskov (Liskov Substitution Principle — LSP)
- **Problema:** A classe `EntregaRetiradaLoja` estendia `Entrega`, mas fortalecia pré-condições ao lançar uma `IllegalStateException` quando o total era menor que R$ 50,00, quebrando a expectativa de quem consumia o método `calcularFrete`.
- **Solução:** Criou-se o contrato `IEntrega`. As implementações `Entrega` e `EntregaRetiradaLoja` agora possuem comportamentos determinísticos e previsíveis sem lançar exceções inesperadas em tempo de cálculo de frete.

---

## 📂 Comparação da Estrutura do Projeto

### Antes da Refatoração
```text
src/
└── projetoprincipiosdesign/
    ├── Cidade.java
    ├── Cliente.java
    ├── Endereco.java
    ├── Entrega.java
    ├── EntregaRetiradaLoja.java
    ├── IPagamento.java
    ├── ItemPedido.java
    ├── Main.java
    ├── PagamentoBoleto.java
    ├── PagamentoCartao.java
    ├── PagamentoPix.java
    ├── Pedido.java
    └── PedidoService.java
```

### Depois da Refatoração
```text
src/
└── projetoprincipiosdesign/
    ├── Main.java
    ├── controller/
    ├── entity/
    │   ├── Cidade.java
    │   ├── Cliente.java
    │   ├── Endereco.java
    │   ├── ItemPedido.java
    │   └── Pedido.java
    ├── service/
    │   └── PedidoService.java
    ├── repository/
    │   └── PedidoRepository.java
    ├── desconto/
    │   ├── IDesconto.java
    │   ├── DescontoAluno.java
    │   ├── DescontoProfessor.java
    │   ├── DescontoFuncionario.java
    │   └── SemDesconto.java
    ├── pagamento/
    │   ├── IPagamento.java
    │   ├── IPagamentoParcelavel.java
    │   ├── IPagamentoBoleto.java
    │   ├── PagamentoCartao.java
    │   ├── PagamentoPix.java
    │   └── PagamentoBoleto.java
    └── entrega/
        ├── IEntrega.java
        ├── Entrega.java
        └── EntregaRetiradaLoja.java
```

---

## 🧪 Validação e Execução

### Como Executar
```bash
# Compilar todas as classes
javac -d out $(find src -name "*.java")

# Executar a aplicação
java -cp out projetoprincipiosdesign.Main
```

### Saída Obtida
```text
=== LOJA ACADÊMICA ===

Cidade de entrega:
Belo Horizonte

Total com desconto:
R$ 144,00

Pagamento:
Salvando pedido em arquivo...
Gerando resumo do pedido...
Cliente: Ana
Total: R$ 144,00
Pagamento no cartão: R$ 144,00
Enviando mensagem para Ana: pedido finalizado.

Programa executado com sucesso.
```

- **Verificação de Cálculo:** Subtotal (1x 120.0 + 2x 20.0 = 160.0) com 10% de desconto de Aluno resulta em **R$ 144,00** (Correto).
- **Verificação de Pagamento:** Execução do pagamento com cartão de crédito via interface (Correto).
- **Verificação de Persistência:** Registro gravado com sucesso no arquivo `pedidos.txt` via `PedidoRepository` (Correto).

---

## 💡 Reflexão Final

### 1. Quais classes ficaram com responsabilidades mais claras depois da refatoração?
- **`PedidoService`:** Deixou de ser um "objeto Deus" (*God Class*) que herdava cartão, decidia descontos por `if/else`, instanciava pagamentos e escrevia arquivos. Agora sua responsabilidade é única e clara: **orquestrar o fluxo do pedido**.
- **`IPagamento` e suas classes (`PagamentoCartao`, `PagamentoPix`, `PagamentoBoleto`):** Cada forma de pagamento agora implementa apenas os métodos que de fato suporta, eliminando métodos inúteis e exceções de operações não suportadas.
- **`PedidoRepository`:** Centralizou exclusivamente a responsabilidade de persistência e E/S em arquivo (`pedidos.txt`), desacoplando a camada de serviço de detalhes de armazenamento.
- **`Cliente`, `Endereco` e `Pedido`:** Passaram a proteger suas estruturas internas fornecendo métodos de delegação coesos em conformidade com a Lei de Demeter.

### 2. Se fosse necessário adicionar uma nova forma de pagamento ou um novo tipo de desconto, quantas classes precisariam ser alteradas agora?
- **Adicionar uma nova forma de pagamento (ex: Criptomoeda):**
  - **Classes alteradas: `0`**.
  - **Classes criadas: `1`** (apenas criar a nova classe `PagamentoCripto implements IPagamento`). A classe `PedidoService` permanece **intocada** (fechada para modificação).
- **Adicionar um novo tipo de desconto (ex: Desconto de Black Friday / Convênio):**
  - **Classes alteradas: `0`**.
  - **Classes criadas: `1`** (apenas criar a nova classe `DescontoConvenio implements IDesconto`). O `PedidoService` permanece **intocada** e pronto para receber a nova regra.
