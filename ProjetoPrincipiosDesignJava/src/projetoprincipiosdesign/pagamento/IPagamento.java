package projetoprincipiosdesign.pagamento;

public interface IPagamento {
    void pagar(double valor);
    void parcelar(double valor, int parcelas);
    void gerarBoleto(double valor);
}
