# Documentação de Desenvolvimento do Sistema Agropecuário

Este documento apresenta o dicionário de termos, a arquitetura de banco de dados e o modelo de relacionamento de tabelas do sistema, projetado para o gerenciamento de rebanho e pesagem inteligente automatizada.

---

## 1. Dicionário de Termos (Dicionário de Dados)

O sistema utiliza persistência local off-line através do **Room ORM (SQLite)**. O esquema é mapeado por cinco entidades principais descritas abaixo.

### Tabela: `animal`
Armazena as informações cadastrais e de manejo biológico e financeiro de cada espécime do rebanho.

| Campo | Tipo Room/SQLite | Descrição | Restrições / Observações |
| :--- | :--- | :--- | :--- |
| **`brinco`** *(PK)* | `TEXT` | Identificação visual primária do animal. | Obrigatório, máximo de 5 caracteres alfanuméricos. |
| **`rfid`** | `TEXT` | Código único lido pelo leitor RFID UHF J4212U. | EPC de 24 caracteres hexadecimais (96 bits). |
| **`categ`** | `TEXT` | Categoria de produção zootécnica. | Valores válidos: `borrego`, `cordeiro`, `matriz`, `reprodutor`, `castrado`, `descarte`. |
| **`dat_nasc`** | `TEXT` | Data de nascimento do animal. | Formato padrão `YYYY-MM-DD`. |
| **`peso`** | `REAL` | Peso atual em quilogramas (kg). | Atualizado dinamicamente por pesagens na balança BT. |
| **`pai`** | `TEXT?` | Brinco do reprodutor pai. | Opcional (suporta encadeamento genealógico). |
| **`mae`** | `TEXT?` | Brinco da matriz mãe. | Opcional (suporta encadeamento genealógico). |
| **`foto`** | `TEXT?` | Caminho de arquivo da imagem ou URI do cache. | Armazena a foto capturada em tempo real ou simulador. |
| **`sexo`** | `TEXT` | Sexo do animal. | `M` para Macho, `F` para Fêmea. |
| **`local_cod`** | `TEXT?` | Código do piquete onde o animal está alocado. | Chave estrangeira que aponta para a tabela `local`. |
| **`condic`** | `TEXT` | Estado de atividade atual do animal. | Padrão `ativo`. Opções: `ativo`, `inativo`, `abatido`, `morto`, `vendido`. |
| **`anim_C_inic`** | `REAL` | Custo de aquisição inicial (R$). | Essencial para o cálculo de margem operacional. |
| **`anim_custo`** | `REAL` | Custo de manejo e depreciação acumulados. | Soma cumulativa de manejos, ração e piquetes. |

---

### Tabela: `anim_peso`
Registra o histórico evolutivo de pesagens e índices zootécnicos do animal.

| Campo | Tipo Room/SQLite | Descrição | Restrições / Observações |
| :--- | :--- | :--- | :--- |
| **`id`** *(PK)* | `INTEGER` | Identificador sequencial da pesagem. | Gerado automaticamente (`autoGenerate = true`). |
| **`brinco`** | `TEXT` | Brinco do animal pesado. | Chave estrangeira que aponta para `animal.brinco`. |
| **`data_hora`** | `INTEGER` | Timestamp da leitura de peso. | Armazena o marcador temporal em milissegundos. |
| **`peso`** | `REAL` | Peso aferido na pesagem (kg). | Originado por entrada manual ou fluxo Bluetooth. |
| **`ipc`** | `TEXT` | Índice de Condição Corporal (ICC/IPC). | Escala de 1 a 5 (1=Muito magro, 2=Magro, 3=Bom, 4=Gordo, 5=Muito gordo). |

---

### Tabela: `anim_ocor`
Histórico de ocorrências sanitárias, partos, manejos ou eventos de manutenção.

| Campo | Tipo Room/SQLite | Descrição | Restrições / Observações |
| :--- | :--- | :--- | :--- |
| **`id`** *(PK)* | `INTEGER` | Identificador sequencial da ocorrência. | Gerado automaticamente (`autoGenerate = true`). |
| **`brinco`** | `TEXT` | Brinco do animal afetado. | Chave estrangeira que aponta para `animal.brinco`. |
| **`data_hora`** | `INTEGER` | Timestamp do registro do evento. | Marcador de tempo em milissegundos. |
| **`descricao`** | `TEXT` | Descrição do evento ocorrido. | Máximo de 50 caracteres para garantir legibilidade no terminal. |
| **`ocor_custo`** | `REAL` | Custo veterinário/sanitário associado. | Somado automaticamente ao `anim_custo` acumulado do animal. |

---

### Tabela: `local` (Piquetes / Invernadas)
Especificações geográficas e financeiras de cada área física da propriedade agrícola.

| Campo | Tipo Room/SQLite | Descrição | Restrições / Observações |
| :--- | :--- | :--- | :--- |
| **`local_cod`** *(PK)* | `TEXT` | Código único identificador do piquete. | Máximo de 3 caracteres alfanuméricos. |
| **`descricao`** | `TEXT` | Descrição verbal do piquete. | Máximo de 30 caracteres. |
| **`local_area`** | `REAL` | Área territorial em hectares (ha). | Utilizado para carga animal / taxa de lotação. |
| **`local_sede`** | `TEXT` | Identificador da sede física ou fazenda. | Máximo de 20 caracteres. |
| **`local_custo_fix`** | `REAL` | Amortização de benfeitorias físicas. | Ex: cercas, bebedouros fixos (R$). |
| **`local_custo_var`** | `REAL` | Custo sazonal agrícola variável. | Ex: correção de solo, adubação permanente (R$). |

---

### Tabela: `operador`
Registra quem assessora e preenche os dados em campo para auditoria local.

| Campo | Tipo Room/SQLite | Descrição | Restrições / Observações |
| :--- | :--- | :--- | :--- |
| **`oper_cod`** *(PK)* | `TEXT` | Chave curta identificadora do operador. | Máximo de 3 caracteres alfanuméricos. |
| **`oper_nome`** | `TEXT` | Nome operacional do funcionário. | Máximo de 30 caracteres. |
| **`oper_niv`** | `TEXT` | Nível hierárquico no sistema de manejo. | `A` para Administrador ou `O` para Operador do campo. |
| **`oper_senha`** | `TEXT` | Senha de criptografia simétrica simples. | Máximo de 8 caracteres. |

---

## 2. Diagrama de Relacionamento de Entidades (MER)

Abaixo está estruturado o fluxo em grafo do banco de dados (esquema conceitual usando sintaxe **Mermaid**). 

```mermaid
erDiagram
    ANIMAL {
        string brinco PK
        string rfid UNIQUE
        string categ
        string dat_nasc
        double peso
        string pai FK
        string mae FK
        string foto
        string sexo
        string local_cod FK
        string condic
        double anim_C_inic
        double anim_custo
    }
    
    LOCAL {
        string local_cod PK
        string descricao
        double local_area
        string local_sede
        double local_custo_fix
        double local_custo_var
    }
    
    ANIM_PESO {
        int id PK
        string brinco FK
        long data_hora
        double peso
        string ipc
    }
    
    ANIM_OCOR {
        int id PK
        string brinco FK
        long data_hora
        string descricao
        double ocor_custo
    }
    
    OPERADOR {
        string oper_cod PK
        string oper_nome
        string oper_niv
        string oper_senha
    }

    LOCAL ||--o{ ANIMAL : "comporta"
    ANIMAL ||--o{ ANIM_PESO : "registra historial de"
    ANIMAL ||--o{ ANIM_OCOR : "sofre"
    ANIMAL |o--o| ANIMAL : "genealogia (pai / mae)"
```

---

## 3. Arquitetura de Comunicação: RFID UHF J4212U

O módulo integrado utiliza **Bluetooth Classic (RFCOMM/SPP)** para escutar a porta serial virtualizada de um transmissor físico (como ESP32 atuando como ponte, ou comunicação direta com o cabo do módulo J4212U convertido para BT).

- **Frequência de Operação**: UHF EPCglobal Class 1 Gen 2 (ISO 18000-6C) operando entre 860 MHz e 960 MHz.
- **Protocolo de Pacote (Frame Físico J4212U)**:
  - Cabeçalho: `0xBB`
  - Tipo: `0x02` (Resposta ao host)
  - Comando: `0x22` (Retorno de leitura em modo contínuo de inventário)
  - EPC de 96 bits lido automaticamente pelo buffer físico e convertido para String Hexadecimal de 24 caracteres preenchida em tempo-real no formulário principal de cadastro sem necessidade de digitação.
- **Fallback ASCII**: No caso de firmwares customizados onde a ESP32 envia os caracteres lidos delimitados por quebra de linha (`\r\n`), o módulo monitora o buffer no estado persistente permitindo que leitores emulados carreguem o UID EPC instantaneamente.
