package projetoprincipiosdesign.entrega;

public class Entrega implements IEntrega {
    @Override
    public double calcularFrete(double total) {
        return 15.0;
    }
}
