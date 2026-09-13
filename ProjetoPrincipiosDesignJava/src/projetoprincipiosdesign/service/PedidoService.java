package projetoprincipiosdesign.service;

import projetoprincipiosdesign.desconto.IDesconto;
import projetoprincipiosdesign.entity.ItemPedido;
import projetoprincipiosdesign.entity.Pedido;
import projetoprincipiosdesign.pagamento.IPagamento;
import projetoprincipiosdesign.pagamento.PagamentoBoleto;
import projetoprincipiosdesign.pagamento.PagamentoCartao;
import projetoprincipiosdesign.pagamento.PagamentoPix;
import projetoprincipiosdesign.repository.PedidoRepository;

public class PedidoService {
    private PedidoRepository pedidoRepository = new PedidoRepository();

    public double calcularTotal(Pedido pedido, IDesconto desconto) {
        double total = 0.0;

        for (ItemPedido item : pedido.getItens()) {
            total += item.getPreco() * item.getQuantidade();
        }

        return desconto.aplicar(total);
    }

    public String obterCidadeEntrega(Pedido pedido) {
        return pedido.getCidadeEntrega();
    }

    public void finalizarPedido(Pedido pedido, IDesconto desconto, String formaPagamento) {
        double total = calcularTotal(pedido, desconto);

        pedidoRepository.salvar(pedido, total);

        System.out.println("Gerando resumo do pedido...");
        System.out.println("Cliente: " + pedido.getCliente().getNome());
        System.out.printf("Total: R$ %.2f%n", total);

        if (formaPagamento.equals("CARTAO")) {
            IPagamento pagamento = new PagamentoCartao();
            pagamento.pagar(total);
        } else if (formaPagamento.equals("PIX")) {
            IPagamento pagamento = new PagamentoPix();
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
