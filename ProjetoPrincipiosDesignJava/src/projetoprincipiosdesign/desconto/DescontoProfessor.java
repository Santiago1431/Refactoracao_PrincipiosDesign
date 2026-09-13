package projetoprincipiosdesign.desconto;

public class DescontoProfessor implements IDesconto {
    @Override
    public double aplicar(double valorTotal) {
        return valorTotal * 0.85;
    }
}
