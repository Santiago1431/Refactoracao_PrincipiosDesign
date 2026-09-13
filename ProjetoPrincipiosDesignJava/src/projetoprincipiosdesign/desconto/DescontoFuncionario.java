package projetoprincipiosdesign.desconto;

public class DescontoFuncionario implements IDesconto {
    @Override
    public double aplicar(double valorTotal) {
        return valorTotal * 0.80;
    }
}
