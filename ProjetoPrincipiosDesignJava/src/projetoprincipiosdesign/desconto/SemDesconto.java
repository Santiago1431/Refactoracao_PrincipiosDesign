package projetoprincipiosdesign.desconto;

public class SemDesconto implements IDesconto {
    @Override
    public double aplicar(double valorTotal) {
        return valorTotal;
    }
}
