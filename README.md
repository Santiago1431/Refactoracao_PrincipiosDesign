# Refactoracao Principios de Design de Software
 
## Roteiro Guiado de Refatoração

"Objetivo: melhorar o projeto aplicando princípios de projeto e boas práticas de orientação a objetos.
Faça as alterações na ordem apresentada. Após cada etapa, execute novamente o projeto e verifique se ele
continua funcionando."

## Entidades/Classes de domínio
---
- Cliente  
    - nome e endereço.  
- Endereco  
    - logradouro e a cidade.  
-  Cidade
    - nome da cidade.
- Pedido
    - um cliente e uma lista de itens.
- ItemPedido
    - representa um produto, seu preço e quantidade.

## Formas de pagamento
---
"Também existem diferentes formas de pagamento — PagamentoCartao, PagamentoPix e PagamentoBoleto.
Essas classes implementam a interface IPagamento, que atualmente define operações de pagamento,
parcelamento e geração de boleto. Algumas formas de pagamento, porém, não utilizam todas essas operações"'