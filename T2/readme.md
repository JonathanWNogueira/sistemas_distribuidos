
# Trabalho 2 — Salas de Chat com Java RMI

## Descrição Geral

A aplicação consiste em uma arquitetura cliente-servidor, com suporte a múltiplos clientes e múltiplas salas de chat. Será testada junto a implementações de outros grupos, escolhidas aleatoriamente, portanto, **a conformidade com a especificação é obrigatória**.

---

## Estrutura da Aplicação

- **Servidor Central:** `ServerChat`
- **Cliente:** `UserChat`
- **Sala de Chat:** `RoomChat`

Interfaces obrigatórias a serem implementadas:

```java
public interface IServerChat extends java.rmi.Remote {
    ArrayList<String> getRooms();
    void createRoom(String roomName);
}

public interface IUserChat extends java.rmi.Remote {
    void deliverMsg(String senderName, String msg);
}

public interface IRoomChat extends java.rmi.Remote {
    void sendMsg(String usrName, String msg);
    void joinRoom(String userName, IUserChat user);
    void leaveRoom(String usrName);
    String getRoomName();
    void closeRoom();
}
```

---

## Requisitos Funcionais Absolutos

1. **Interfaces**: `ServerChat`, `UserChat` e `RoomChat` devem implementar `IServerChat`, `IUserChat` e `IRoomChat`, respectivamente.

2. **Lista de Salas no Servidor**:
   ```java
   private Map<String, IRoomChat> roomList;
   ```
   - Sem duplicação de nomes.
   - Número ilimitado de salas e usuários por sala.

3. **Lista de Usuários na Sala**:
   ```java
   private Map<String, IUserChat> userList;
   ```

4. **Conexão Inicial**:
   - O cliente deve obter a lista de salas via `getRooms()`.

5. **Entrada em Sala Existente**:
   - Obter objeto remoto via RMI Registry.
   - Entrar com `joinRoom()`.

6. **Criação de Nova Sala**:
   - Usar `createRoom(String roomName)`.
   - Vinculação não é automática — deve-se usar `joinRoom()` após a criação.

7. **Envio de Mensagens**:
   - Usar `sendMsg(String usrName, String msg)`.

8. **Recebimento de Mensagens**:
   - Implementar `deliverMsg(String senderName, String msg)`.

9. **Envio Interno das Mensagens**:
   - A sala é responsável por enviar as mensagens aos usuários.

10. **Saída de Sala**:
    - Usar `leaveRoom(String usrName)`.

11. **Fechamento de Sala**:
    - Só o servidor pode fechar com `closeRoom()`.
    - Antes disso, deve notificar os usuários com a mensagem:
      ```
      "Sala fechada pelo servidor."
      ```
    - A sala deve ser removida da lista do servidor e dos clientes.

12. **Interface Gráfica (GUI)**:
    - Deve conter, no mínimo:
      - Quadro de mensagens.
      - Seleção de salas.
      - Botões para: **SEND**, **CLOSE**, **JOIN**, **LEAVE**.

13. **Registro RMI**:
    - O servidor deve ser registrado como `"Servidor"` na porta `2020` usando o `rmiregistry` na máquina do servidor.

---

## 🛠️ Requisitos Técnicos

- Linguagem: **Java**
- Tecnologia: **Java RMI**
- Porta: `2020`
- Registro RMI: `"Servidor"`

---

## 📋 Observações Finais

- Testes serão realizados com clientes e servidores de outros grupos.
- A aplicação deve funcionar corretamente em ambiente distribuído.
- Criatividade na interface é bem-vinda, mas **todos os requisitos devem ser atendidos**.

---

