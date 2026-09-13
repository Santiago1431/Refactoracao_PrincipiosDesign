package projetoprincipiosdesign.service;

import projetoprincipiosdesign.entity.ItemPedido;
import projetoprincipiosdesign.entity.Pedido;
import projetoprincipiosdesign.pagamento.PagamentoBoleto;
import projetoprincipiosdesign.pagamento.PagamentoCartao;
import projetoprincipiosdesign.pagamento.PagamentoPix;
import projetoprincipiosdesign.repository.PedidoRepository;

public class PedidoService extends PagamentoCartao {
    private PedidoRepository pedidoRepository = new PedidoRepository();

    public double calcularTotal(Pedido pedido, String tipoCliente) {
        double total = 0.0;

        for (ItemPedido item : pedido.getItens()) {
            total += item.getPreco() * item.getQuantidade();
        }

        if (tipoCliente.equals("ALUNO")) {
            total *= 0.90;
        } else if (tipoCliente.equals("PROFESSOR")) {
            total *= 0.85;
        } else if (tipoCliente.equals("FUNCIONARIO")) {
            total *= 0.80;
        }

        return total;
    }

    public String obterCidadeEntrega(Pedido pedido) {
        return pedido.getCliente().getEndereco().getCidade().getNome();
    }

    public void finalizarPedido(Pedido pedido, String formaPagamento) {
        double total = calcularTotal(pedido, "ALUNO");

        pedidoRepository.salvar(pedido, total);

        System.out.println("Gerando resumo do pedido...");
        System.out.println("Cliente: " + pedido.getCliente().getNome());
        System.out.printf("Total: R$ %.2f%n", total);

        if (formaPagamento.equals("CARTAO")) {
            PagamentoCartao pagamento = new PagamentoCartao();
            pagamento.pagar(total);
        } else if (formaPagamento.equals("PIX")) {
            PagamentoPix pagamento = new PagamentoPix();
            pagamento.pagar(total);
        } else if (formaPagamento.equals("BOLETO")) {
            PagamentoBoleto pagamento = new PagamentoBoleto();
            pagamento.gerarBoleto(total);
        }

        System.out.println(
            "Enviando mensagem para " + pedido.getCliente().getNome() + ": pedido finalizado."
        );
    }
}
