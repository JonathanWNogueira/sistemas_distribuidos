
# Trabalho 3 — Middleware para Estabilização de Mensagens

## Descrição Geral

Este trabalho tem como finalidade criar um middleware, chamado `StableMulticast`, para comunicação multicast utilizando **sockets UDP**.  
O middleware deve garantir **estabilização de mensagens** e descarte de mensagens do buffer de acordo com um algoritmo baseado em **vetores de relógios lógicos**.

---

## Funcionalidades Esperadas

- Envio de mensagens multicast entre instâncias via **sockets UDP unicast não confiáveis**
- Descoberta de membros do grupo via **IP multicast**
- **Estabilização** e descarte de mensagens usando vetor de vetores de relógios lógicos
- Atualização dinâmica dos membros participantes do grupo

---

## Estrutura do Middleware

### Pacote Java
```java
import StableMulticast.*;
```

### Interface obrigatória do usuário:
```java
public interface IStableMulticast {
    public void deliver(String msg);
}
```

### API do middleware:
```java
public StableMulticast(String ip, Integer port, IStableMulticast client);
public void msend(String msg, IStableMulticast client);
```

- `msend(...)` é usado para enviar uma mensagem multicast.
- `deliver(...)` é chamado automaticamente para entregar a mensagem ao usuário (via callback).

---

## Lógica do Algoritmo

### Envio (`mcsend(msg)`):
- Timestamp (vetor lógico) é criado a partir de `MCi[i][*]`
- Mensagem é enviada a todos os membros via unicast
- Relógio local é incrementado `MCi[i][i] += 1`

### Recebimento:
- Mensagem é depositada no buffer
- Vetor lógico local é atualizado com o da mensagem
- Se necessário, incrementa contador de mensagens recebidas
- Entrega a mensagem para a camada superior

### Descarte de Mensagem:
- Se `msg.VC[msg.sender] <= min(MCi[x][msg.sender])`, a mensagem é descartada do buffer local

---

## Requisitos de Interação

- Envio de mensagens unicast deve ser **controlado via teclado**:
  - Deve haver uma pergunta antes de cada envio: enviar para todos ou selecionar processos individualmente
- O **buffer** e os **vetores de relógios** devem ser exibidos continuamente no terminal
- **GUI não é necessária**

---

## Referência de Algoritmo

Baseado no artigo:

> *Fundamentals of Distributed Computing: A Practical Tour of Vector Clock Systems*  
Disponível nos slides do Moodle ou via:  
🔗 https://www.computer.org/csdl/mags/ds/2002/02/o2001.pdf

---
