package projetoprincipiosdesign.desconto;

public class DescontoAluno implements IDesconto {
    @Override
    public double aplicar(double valorTotal) {
        return valorTotal * 0.90;
    }
}
